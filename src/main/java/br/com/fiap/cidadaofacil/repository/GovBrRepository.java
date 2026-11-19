package br.com.fiap.cidadaofacil.repository;

import br.com.fiap.cidadaofacil.dto.CatalogoGovBrDTO;
import br.com.fiap.cidadaofacil.exception.ServicoIndisponivelException;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

// Repository: lê o catálogo público de serviços do gov.br (https://www.gov.br/pt-br/servicos).
// O gov.br não tem API aberta para o catálogo, então lemos as páginas HTML: os dados principais vêm do JSON-LD
// (schema.org) que a própria página publica, e requisitos, documentos e canais vêm das seções da página.
@Repository
public class GovBrRepository {
    public static final String PREFIXO_SERVICO = "https://www.gov.br/pt-br/servicos/";
    private static final String PREFIXO_CATEGORIA = "https://www.gov.br/pt-br/categorias/";
    private static final int ITENS_POR_PAGINA = 10;
    private static final int MAX_PAGINAS = 60;

    // Links fixos do menu lateral que aparecem em todas as páginas de categoria
    private static final Set<String> IGNORAR = Set.of("buscar-servicos-por", "listar", "servicos-por-publico-alvo");
    private static final Pattern SLUG = Pattern.compile("^https://www\\.gov\\.br/pt-br/servicos/([a-z0-9-]+)/?$");
    private static final Pattern SINONIMO = Pattern.compile("\"\\s*([^\"]+?)\\s*\"");
    private static final Pattern MARCADOR = Pattern.compile("^([-•·–*]|[a-z0-9]{1,2}[).])\\s*");
    private static final Pattern NAO_E_DOCUMENTO = Pattern.compile(
            "^(em caso|se |caso |obs|atenção|importante|você |para |não |ou$|e$)", Pattern.CASE_INSENSITIVE);

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final String agente;
    private final long pausaMs;

    public GovBrRepository(@Value("${govbr.user-agent:CidadaoFacil/1.0 (projeto academico FIAP)}") String agente,
                           @Value("${govbr.pausa-ms:300}") long pausaMs) {
        this.agente = agente;
        this.pausaMs = pausaMs;
    }

    // Slugs dos serviços de uma categoria do gov.br, seguindo a paginação (10 por página) até o limite (0 = todos)
    public List<String> listarServicos(String caminhoCategoria, int limite) {
        Set<String> slugs = new LinkedHashSet<>();
        for (int pagina = 0; pagina < MAX_PAGINAS; pagina++) {
            Document html = baixar(PREFIXO_CATEGORIA + caminhoCategoria + "?b_start:int=" + pagina * ITENS_POR_PAGINA);
            if (html == null) {
                break;
            }
            int antes = slugs.size();
            slugs.addAll(lerSlugs(html));
            boolean acabou = slugs.size() == antes;
            if (limite > 0 && slugs.size() >= limite) {
                return new ArrayList<>(slugs).subList(0, limite);
            }
            if (acabou) {
                break;
            }
        }
        return new ArrayList<>(slugs);
    }

    // Lê a página do serviço; vazio se a página não existe mais (404)
    public Optional<CatalogoGovBrDTO.ServicoLido> buscarServico(String slug) {
        Document html = baixar(PREFIXO_SERVICO + slug);
        return html == null ? Optional.empty() : Optional.ofNullable(lerServico(slug, html));
    }

    // Uma requisição por vez, com pausa, para não sobrecarregar o portal
    private Document baixar(String url) {
        try {
            Thread.sleep(pausaMs);
            return Jsoup.connect(url).userAgent(agente).timeout(30_000).maxBodySize(0).get();
        } catch (HttpStatusException e) {
            if (e.getStatusCode() == 404) {
                return null;
            }
            throw new ServicoIndisponivelException("O gov.br respondeu " + e.getStatusCode() + " para " + url);
        } catch (IOException e) {
            throw new ServicoIndisponivelException("Não foi possível acessar o gov.br: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServicoIndisponivelException("Leitura do gov.br interrompida.");
        }
    }

    // ---------- Leitura do HTML (métodos estáticos para poder testar com páginas salvas) ----------

    public static List<String> lerSlugs(Document html) {
        Set<String> slugs = new LinkedHashSet<>();
        for (Element a : html.select("a[href]")) {
            var m = SLUG.matcher(a.absUrl("href").isEmpty() ? a.attr("href") : a.absUrl("href"));
            if (m.matches() && !IGNORAR.contains(m.group(1))) {
                slugs.add(m.group(1));
            }
        }
        return new ArrayList<>(slugs);
    }

    public static CatalogoGovBrDTO.ServicoLido lerServico(String slug, Document html) {
        // Dados principais do JSON-LD; algumas páginas não têm esse bloco válido, e aí vêm do HTML
        Map<String, Object> ld = lerJsonLd(html);
        String nomeBruto = texto(ld.get("name"));
        if (nomeBruto.isEmpty()) {
            Element titulo = html.selectFirst(".visao-servico .titulo");
            nomeBruto = titulo == null ? "" : limpar(titulo.text());
        }
        // Alguns nomes trazem sinônimos entre aspas: Alistar-se no serviço militar " Alistamento" , " Alistar..."
        // O nome fica só com a parte antes das aspas e os sinônimos viram palavras-chave
        List<String> sinonimos = new ArrayList<>();
        var aspas = SINONIMO.matcher(nomeBruto);
        while (aspas.find()) {
            sinonimos.add(limpar(aspas.group(1)));
        }
        String nome = nomeBruto.contains("\"")
                ? nomeBruto.substring(0, nomeBruto.indexOf('"')).replaceAll("[\\s,;-]+$", "").trim()
                : nomeBruto;
        if (nome.isEmpty()) {
            return null;
        }
        String descricao = limpar(Jsoup.parse(texto(ld.get("description"))).text());
        if (descricao.isEmpty()) {
            descricao = String.join(" ", paragrafos(conteudoDaSecao(html, "dados_basicos"))); // seção "O que é?"
        }
        String orgao = ld.get("provider") instanceof Map<?, ?> p ? texto(p.get("name")) : "";

        // "Quem pode utilizar este serviço?": o primeiro parágrafo descreve o público, os demais são requisitos
        List<String> paragrafos = paragrafos(conteudoDaSecao(html, "solicitantes"));
        String publico = paragrafos.isEmpty() ? "" : semDoisPontos(paragrafos.get(0));
        if (publico.length() < 25) {
            publico = audiencia(ld); // frase curta demais (ex.: "Cidadão que"): usa o público do JSON-LD
        }
        List<String> requisitos = paragrafos.size() > 1 ? paragrafos.subList(1, paragrafos.size()) : List.of();

        List<String> etapas = new ArrayList<>();
        List<CatalogoGovBrDTO.DocumentoLido> documentos = new ArrayList<>();
        Set<String> digitais = new LinkedHashSet<>();
        Set<String> presenciais = new LinkedHashSet<>();

        // Botão "Iniciar": o link direto para fazer o pedido
        Element iniciar = html.selectFirst("a.btn-solicitar[href]");
        if (iniciar != null && iniciar.absUrl("href").startsWith("http")) {
            digitais.add(iniciar.absUrl("href"));
        }

        Element secaoEtapas = conteudoDaSecao(html, "etapas-servico");
        if (secaoEtapas != null) {
            for (Element li : secaoEtapas.select("> ol > li")) {
                Element nomeEtapa = li.selectFirst("> span");
                Element resumo = li.selectFirst("> div:not(.subtitulo-etapa)");
                String titulo = nomeEtapa == null ? "" : limpar(nomeEtapa.text());
                String detalhe = resumo == null ? "" : limpar(resumo.text());
                String etapa = titulo.isEmpty() ? detalhe : detalhe.isEmpty() ? titulo : titulo + ": " + detalhe;
                if (!etapa.isEmpty()) {
                    etapas.add(etapa);
                }
                for (Element sub : li.select("> div.subtitulo-etapa")) {
                    Element t = sub.selectFirst("p.titulo-documento");
                    String tipo = t == null ? "" : t.text().toLowerCase();
                    if (tipo.startsWith("canais")) {
                        lerCanais(sub, digitais, presenciais);
                    } else if (tipo.startsWith("documenta")) {
                        lerDocumentos(sub, titulo, documentos);
                    }
                }
            }
        }

        List<String> palavras = new ArrayList<>(sinonimos);
        if (ld.get("keywords") instanceof List<?> lista) {
            lista.stream().map(GovBrRepository::texto).filter(s -> !s.isEmpty()).forEach(palavras::add);
        }
        return new CatalogoGovBrDTO.ServicoLido(slug, PREFIXO_SERVICO + slug, nome, descricao, orgao, publico,
                requisitos, etapas, documentos, new ArrayList<>(digitais), new ArrayList<>(presenciais), palavras);
    }

    // Canais: "Web" vira canal digital (com o link) e "Presencial" vira canal presencial (com o texto do endereço)
    private static void lerCanais(Element sub, Set<String> digitais, Set<String> presenciais) {
        for (Element icone : sub.select("i[class*=fa-]")) {
            Element bloco = icone.parent();
            if (bloco == null) {
                continue;
            }
            String rotulo = limpar(bloco.ownText()).toLowerCase();
            if (rotulo.startsWith("web")) {
                for (Element a : bloco.select("a[href]")) {
                    if (a.absUrl("href").startsWith("http")) {
                        digitais.add(a.absUrl("href"));
                    }
                }
            } else if (rotulo.startsWith("presencial")) {
                String endereco = limpar(bloco.text().replaceFirst("(?i)^\\s*presencial\\s*:?", ""));
                if (!endereco.isEmpty()) {
                    presenciais.add(endereco);
                }
            }
        }
    }

    // Documentação: o título "em comum para todos os casos" indica obrigatório; "casos específicos" indica opcional
    private static void lerDocumentos(Element sub, String etapa, List<CatalogoGovBrDTO.DocumentoLido> documentos) {
        boolean obrigatorio = true;
        for (Element filho : sub.children()) {
            if (filho.hasClass("info-etapa")) {
                String cabecalho = filho.text().toLowerCase();
                if (!cabecalho.isBlank()) {
                    obrigatorio = !cabecalho.contains("específic") && !cabecalho.contains("especific");
                }
            } else if (filho.tagName().equals("ul")) {
                for (Element li : filho.select("li")) {
                    List<String> linhas = li.select("p").isEmpty() ? List.of(li.text()) : li.select("p").eachText();
                    for (String linha : linhas) {
                        String doc = semDoisPontos(limpar(MARCADOR.matcher(limpar(linha)).replaceFirst("")));
                        boolean pareceDocumento = doc.length() >= 3 && doc.length() <= 160
                                && !limpar(linha).endsWith(":") && !NAO_E_DOCUMENTO.matcher(doc).find();
                        if (pareceDocumento) {
                            documentos.add(new CatalogoGovBrDTO.DocumentoLido(doc, doc, obrigatorio, etapa));
                        }
                    }
                }
            }
        }
    }

    // As seções da página são um link com id (ex.: "solicitantes") seguido de uma div com o conteúdo
    private static Element conteudoDaSecao(Document html, String id) {
        Element link = html.getElementById(id);
        Element conteudo = link == null ? null : link.nextElementSibling();
        return conteudo != null && conteudo.hasClass("conteudo") ? conteudo : null;
    }

    private static List<String> paragrafos(Element conteudo) {
        if (conteudo == null) {
            return List.of();
        }
        // Só os blocos "folha" (p ou li sem outro p/li dentro); o select do jsoup inclui o próprio elemento
        return conteudo.select("p, li").stream()
                .filter(e -> e.select("p, li").size() == 1)
                .map(e -> limpar(e.text()))
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
    }

    private static String audiencia(Map<String, Object> ld) {
        if (ld.get("audience") instanceof List<?> lista) {
            return String.join(", ", lista.stream()
                    .map(a -> a instanceof Map<?, ?> m ? texto(m.get("name")) : "")
                    .filter(s -> !s.isEmpty()).toList());
        }
        return "";
    }

    // O JSON-LD do serviço é o bloco schema.org do tipo HowTo / GovernmentService
    @SuppressWarnings("unchecked")
    private static Map<String, Object> lerJsonLd(Document html) {
        for (Element script : html.select("script[type=application/ld+json]")) {
            try {
                Map<String, Object> ld = JSON.readValue(script.data(), Map.class);
                if ("GovernmentService".equals(ld.get("additionalType")) && ld.containsKey("step")) {
                    return ld;
                }
            } catch (RuntimeException e) {
                // Bloco com JSON inválido: ignora e tenta o próximo
            }
        }
        return Map.of();
    }

    private static String texto(Object valor) {
        return valor == null ? "" : limpar(valor.toString());
    }

    static String limpar(String texto) {
        return texto == null ? "" : texto.replace(' ', ' ').replaceAll("\\s+", " ").trim();
    }

    private static String semDoisPontos(String texto) {
        return texto.endsWith(":") ? texto.substring(0, texto.length() - 1).trim() : texto;
    }
}
