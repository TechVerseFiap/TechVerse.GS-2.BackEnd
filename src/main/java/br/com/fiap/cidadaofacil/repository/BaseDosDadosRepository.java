package br.com.fiap.cidadaofacil.repository;

import br.com.fiap.cidadaofacil.exception.ServicoIndisponivelException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Repository: acesso à Base dos Dados, que publica os dados do governo no Google BigQuery.
// A consulta é SQL de verdade, enviada para a API REST do BigQuery (jobs.query); o custo vai para o projeto da equipe.
@Repository
public class BaseDosDadosRepository {
    private static final String URL_BIGQUERY = "https://bigquery.googleapis.com/bigquery/v2";
    private static final String ESCOPO_BIGQUERY = "https://www.googleapis.com/auth/bigquery";
    private static final int ESPERA_MS = 60_000;

    private final GoogleCredentials credenciais;
    private final String projetoCobranca;
    private final RestClient http;

    public BaseDosDadosRepository(@Value("${basedosdados.credenciais:}") String credenciaisJson,
                                  @Value("${basedosdados.projeto-cobranca:}") String projetoCobranca) {
        this.credenciais = lerCredenciais(credenciaisJson);
        // Sem projeto informado, usa o project_id que vem dentro do JSON da conta de serviço
        if (projetoCobranca.isBlank() && credenciais instanceof ServiceAccountCredentials conta) {
            this.projetoCobranca = conta.getProjectId();
        } else {
            this.projetoCobranca = projetoCobranca;
        }

        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(10));
        fabrica.setReadTimeout(Duration.ofSeconds(90));
        this.http = RestClient.builder().baseUrl(URL_BIGQUERY).requestFactory(fabrica).build();
    }

    public boolean estaConfigurado() {
        return credenciais != null && projetoCobranca != null && !projetoCobranca.isBlank();
    }

    // Executa um SELECT na Base dos Dados e devolve as linhas como texto (o BigQuery devolve tudo como string).
    // Os parâmetros entram como @nome no SQL: o valor nunca é concatenado na consulta.
    public List<List<String>> consultar(String sql, Map<String, Object> parametros) {
        if (!estaConfigurado()) {
            throw new ServicoIndisponivelException("A integração com a Base dos Dados não está configurada. "
                    + "Defina a variável BASEDOSDADOS_CREDENTIALS com a chave da conta de serviço do Google Cloud.");
        }
        try {
            Map<String, Object> corpo = new HashMap<>();
            corpo.put("query", sql);
            corpo.put("useLegacySql", false);
            corpo.put("timeoutMs", ESPERA_MS);
            corpo.put("parameterMode", "NAMED");
            corpo.put("queryParameters", parametros.entrySet().stream().map(p -> Map.of(
                    "name", p.getKey(),
                    "parameterType", Map.of("type", p.getValue() instanceof Number ? "INT64" : "STRING"),
                    "parameterValue", Map.of("value", p.getValue().toString()))).toList());

            Resposta resposta = http.post()
                    .uri("/projects/{projeto}/queries", projetoCobranca)
                    .header("Authorization", "Bearer " + token())
                    .body(corpo)
                    .retrieve()
                    .body(Resposta.class);

            // Consulta grande: o BigQuery responde antes de terminar; buscamos o resultado (e as próximas páginas)
            List<List<String>> linhas = new ArrayList<>();
            while (resposta != null) {
                if (resposta.jobComplete()) {
                    linhas.addAll(converter(resposta.rows()));
                    if (resposta.pageToken() == null) {
                        return linhas;
                    }
                }
                resposta = buscarResultado(resposta.jobReference(), resposta.jobComplete() ? resposta.pageToken() : null);
            }
            return linhas;
        } catch (RestClientException | IOException e) {
            throw new ServicoIndisponivelException("Não foi possível consultar a Base dos Dados: " + e.getMessage());
        }
    }

    private Resposta buscarResultado(Trabalho trabalho, String pagina) throws IOException {
        return http.get()
                .uri(uri -> {
                    uri.path("/projects/{projeto}/queries/{id}")
                            .queryParam("location", trabalho.location())
                            .queryParam("timeoutMs", ESPERA_MS);
                    if (pagina != null) {
                        uri.queryParam("pageToken", pagina);
                    }
                    return uri.build(trabalho.projectId(), trabalho.jobId());
                })
                .header("Authorization", "Bearer " + token())
                .retrieve()
                .body(Resposta.class);
    }

    // Token de acesso do Google: a biblioteca renova sozinha quando expira (dura 1 hora)
    private String token() throws IOException {
        credenciais.refreshIfExpired();
        return credenciais.getAccessToken().getTokenValue();
    }

    private static List<List<String>> converter(List<Linha> linhas) {
        if (linhas == null) {
            return List.of();
        }
        return linhas.stream()
                .map(l -> l.f().stream().map(c -> c.v() == null ? null : c.v().toString()).toList())
                .toList();
    }

    private static GoogleCredentials lerCredenciais(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return GoogleCredentials.fromStream(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)))
                    .createScoped(List.of(ESCOPO_BIGQUERY));
        } catch (IOException e) {
            throw new IllegalStateException("BASEDOSDADOS_CREDENTIALS não contém um JSON de conta de serviço válido.", e);
        }
    }

    // Formato da resposta do BigQuery: cada linha tem uma lista "f" de células, e cada célula tem o valor "v"
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Resposta(boolean jobComplete, Trabalho jobReference, List<Linha> rows, String pageToken) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Trabalho(String projectId, String jobId, String location) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Linha(List<Celula> f) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Celula(Object v) {
    }
}
