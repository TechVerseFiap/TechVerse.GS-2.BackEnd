package br.com.fiap.cidadaofacil.service;

import br.com.fiap.cidadaofacil.dto.CatalogoGovBrDTO;
import br.com.fiap.cidadaofacil.dto.PanoramaDTO;
import br.com.fiap.cidadaofacil.dto.SincronizacaoDTO;
import br.com.fiap.cidadaofacil.exception.ConflitoException;
import br.com.fiap.cidadaofacil.exception.ServicoIndisponivelException;
import br.com.fiap.cidadaofacil.model.Indicador;
import br.com.fiap.cidadaofacil.model.IndicadorMunicipio;
import br.com.fiap.cidadaofacil.model.IndicadorMunicipioId;
import br.com.fiap.cidadaofacil.model.Municipio;
import br.com.fiap.cidadaofacil.model.Periodicidade;
import br.com.fiap.cidadaofacil.repository.BaseDosDadosRepository;
import br.com.fiap.cidadaofacil.repository.IndicadorMunicipioRepository;
import br.com.fiap.cidadaofacil.repository.IndicadorRepository;
import br.com.fiap.cidadaofacil.repository.MunicipioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

// Service: sincronização das fontes oficiais com o Oracle, em segundo plano:
// 1) catálogo de serviços do gov.br (CatalogoGovBrService); 2) indicadores do panorama da Base dos Dados (SQL no BigQuery).
// As telas continuam lendo do Oracle, que fica rápido para o cidadão; as fontes oficiais alimentam o banco.
@Service
public class SincronizacaoService {
    private static final Logger log = LoggerFactory.getLogger(SincronizacaoService.class);
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private static final String TB_MUNICIPIOS = "`basedosdados.br_bd_diretorios_brasil.municipio`";
    private static final String TB_POPULACAO = "`basedosdados.br_ibge_populacao.municipio`";
    private static final String TB_CAGED = "`basedosdados.br_me_caged.microdados_movimentacao`";
    private static final String TB_BOLSA_FAMILIA = "`basedosdados.br_cgu_beneficios_cidadao.novo_bolsa_familia`";
    private static final String TB_CNES = "`basedosdados.br_ms_cnes.estabelecimento`";

    // Duas consultas por indicador: (1) qual é o período mais recente; (2) o valor de cada município nesse período.
    // Separar as duas faz o BigQuery ler só a partição daquele ano/mês, o que deixa a consulta rápida e barata.
    // Toda agregação (soma, contagem) é feita no BigQuery: nenhum dado pessoal chega à API.
    private record Consulta(String sqlUltimoPeriodo, String sqlValores) {
    }

    private static final String ULTIMO_MES_BOLSA_FAMILIA = "SELECT ano_competencia, mes_competencia FROM "
            + TB_BOLSA_FAMILIA + " WHERE sigla_uf = @uf ORDER BY 1 DESC, 2 DESC LIMIT 1";

    private static final Map<String, Consulta> CONSULTAS = Map.of(
            "POPULACAO", new Consulta(
                    "SELECT MAX(ano), 1 FROM " + TB_POPULACAO + " WHERE sigla_uf = @uf",
                    "SELECT id_municipio, SUM(populacao) FROM " + TB_POPULACAO
                            + " WHERE sigla_uf = @uf AND ano = @ano GROUP BY id_municipio"),
            // Saldo do ano completo mais recente (o último ano que já tem dezembro)
            "SALDO_EMPREGO_FORMAL", new Consulta(
                    "SELECT MAX(ano), 1 FROM " + TB_CAGED + " WHERE sigla_uf = @uf AND mes = 12",
                    "SELECT id_municipio, SUM(saldo_movimentacao) FROM " + TB_CAGED
                            + " WHERE sigla_uf = @uf AND ano = @ano GROUP BY id_municipio"),
            "FAMILIAS_BOLSA_FAMILIA", new Consulta(ULTIMO_MES_BOLSA_FAMILIA,
                    "SELECT id_municipio, COUNT(DISTINCT nis_favorecido) FROM " + TB_BOLSA_FAMILIA
                            + " WHERE sigla_uf = @uf AND ano_competencia = @ano AND mes_competencia = @mes"
                            + " GROUP BY id_municipio"),
            "VALOR_BOLSA_FAMILIA", new Consulta(ULTIMO_MES_BOLSA_FAMILIA,
                    "SELECT id_municipio, ROUND(SUM(valor_parcela), 2) FROM " + TB_BOLSA_FAMILIA
                            + " WHERE sigla_uf = @uf AND ano_competencia = @ano AND mes_competencia = @mes"
                            + " GROUP BY id_municipio"),
            "ESTABELECIMENTOS_SUS", new Consulta(
                    "SELECT ano, mes FROM " + TB_CNES + " WHERE sigla_uf = @uf ORDER BY 1 DESC, 2 DESC LIMIT 1",
                    "SELECT id_municipio, COUNT(DISTINCT id_estabelecimento_cnes) FROM " + TB_CNES
                            + " WHERE sigla_uf = @uf AND ano = @ano AND mes = @mes AND indicador_vinculo_sus = 1"
                            + " GROUP BY id_municipio"));

    private final BaseDosDadosRepository baseDosDados;
    private final MunicipioRepository municipioRepository;
    private final IndicadorRepository indicadorRepository;
    private final IndicadorMunicipioRepository valorRepository;
    private final PanoramaService panoramaService;
    private final CatalogoGovBrService catalogoService;
    private final TransactionTemplate transacao;
    private final String uf;
    private final boolean sincronizarAoIniciar;
    private final int diasValidade;
    private final boolean catalogoAoIniciar;

    // Uma sincronização por vez; guarda etapa, progresso e o último resultado de cada parte para o painel
    private final AtomicBoolean emAndamento = new AtomicBoolean(false);
    private volatile String etapaAtual;
    private volatile String progresso;
    private volatile CatalogoGovBrDTO.Resultado ultimoCatalogo;
    private volatile String erroCatalogo;
    private volatile SincronizacaoDTO.Resultado ultimoResultado;
    private volatile String ultimoErro;

    public SincronizacaoService(BaseDosDadosRepository baseDosDados, MunicipioRepository municipioRepository,
                                IndicadorRepository indicadorRepository, IndicadorMunicipioRepository valorRepository,
                                PanoramaService panoramaService, CatalogoGovBrService catalogoService,
                                TransactionTemplate transacao,
                                @Value("${basedosdados.uf:SP}") String uf,
                                @Value("${basedosdados.sincronizar-ao-iniciar:true}") boolean sincronizarAoIniciar,
                                @Value("${basedosdados.dias-validade:7}") int diasValidade,
                                @Value("${govbr.sincronizar-ao-iniciar:true}") boolean catalogoAoIniciar) {
        this.baseDosDados = baseDosDados;
        this.municipioRepository = municipioRepository;
        this.indicadorRepository = indicadorRepository;
        this.valorRepository = valorRepository;
        this.panoramaService = panoramaService;
        this.catalogoService = catalogoService;
        this.transacao = transacao;
        this.uf = uf.trim().toUpperCase();
        this.sincronizarAoIniciar = sincronizarAoIniciar;
        this.diasValidade = diasValidade;
        this.catalogoAoIniciar = catalogoAoIniciar;
    }

    public SincronizacaoDTO.Status status() {
        return new SincronizacaoDTO.Status(baseDosDados.estaConfigurado(), emAndamento.get(), etapaAtual, progresso,
                uf, ultimoCatalogo, erroCatalogo, ultimoResultado, ultimoErro);
    }

    // Ao subir a API: atualiza em segundo plano o que estiver desatualizado (a API já responde enquanto isso)
    @EventListener(ApplicationReadyEvent.class)
    public void aoIniciar() {
        boolean catalogo = catalogoAoIniciar && catalogoService.precisaAtualizar();
        LocalDate maisAntiga = indicadorRepository.dataConsultaMaisAntiga();
        boolean indicadores = sincronizarAoIniciar && baseDosDados.estaConfigurado()
                && (maisAntiga == null || !maisAntiga.isAfter(LocalDate.now(FUSO).minusDays(diasValidade)));
        if (catalogo || indicadores) {
            log.info("Sincronização automática: catálogo gov.br = {}, indicadores Base dos Dados = {}", catalogo, indicadores);
            iniciar(catalogo, indicadores);
        }
    }

    // POST /api/sincronizacao: roda as duas partes em segundo plano; o painel acompanha pelo GET
    public SincronizacaoDTO.Status iniciar() {
        return iniciar(true, true);
    }

    private SincronizacaoDTO.Status iniciar(boolean catalogo, boolean indicadores) {
        if (!emAndamento.compareAndSet(false, true)) {
            throw new ConflitoException("Já existe uma sincronização em andamento.");
        }
        etapaAtual = "Preparando";
        Thread.ofVirtual().name("sincronizacao").start(() -> executar(catalogo, indicadores));
        return status();
    }

    private void executar(boolean catalogo, boolean indicadores) {
        try {
            if (catalogo) {
                etapaAtual = "Catálogo de serviços do gov.br";
                try {
                    ultimoCatalogo = catalogoService.sincronizar(p -> progresso = p);
                    erroCatalogo = null;
                } catch (RuntimeException e) {
                    erroCatalogo = e.getMessage();
                    log.warn("Sincronização do catálogo do gov.br falhou: {}", e.getMessage());
                }
            }
            if (indicadores) {
                etapaAtual = "Indicadores da Base dos Dados";
                progresso = "Consultando o BigQuery da Base dos Dados...";
                try {
                    executarIndicadores();
                } catch (RuntimeException e) {
                    log.warn("Sincronização com a Base dos Dados falhou: {}", e.getMessage());
                }
            }
        } finally {
            etapaAtual = null;
            progresso = null;
            emAndamento.set(false);
        }
    }

    // Só os indicadores, de forma síncrona (usado nos testes)
    public SincronizacaoDTO.Resultado sincronizarIndicadores() {
        if (!emAndamento.compareAndSet(false, true)) {
            throw new ConflitoException("Já existe uma sincronização em andamento.");
        }
        try {
            return executarIndicadores();
        } finally {
            emAndamento.set(false);
        }
    }

    private SincronizacaoDTO.Resultado executarIndicadores() {
        try {
            LocalDateTime inicio = LocalDateTime.now(FUSO);
            log.info("Sincronizando o panorama ({}) com a Base dos Dados...", uf);

            int municipiosNovos = sincronizarMunicipios();
            Set<Long> municipios = new HashSet<>(municipioRepository.listarCodigos());

            List<SincronizacaoDTO.IndicadorSincronizado> itens = new ArrayList<>();
            for (Indicador indicador : indicadorRepository.findAllByOrderByIdAsc()) {
                Consulta consulta = CONSULTAS.get(indicador.getCodigo());
                if (consulta != null) {
                    itens.add(sincronizarIndicador(indicador, consulta, municipios));
                }
            }

            panoramaService.limparCache();
            ultimoResultado = new SincronizacaoDTO.Resultado(uf, inicio, LocalDateTime.now(FUSO), municipiosNovos, itens);
            ultimoErro = null;
            log.info("Sincronização concluída: {}", itens);
            return ultimoResultado;
        } catch (RuntimeException e) {
            ultimoErro = e.getMessage();
            throw e;
        }
    }

    // Cadastra os municípios da UF que ainda não existem no Oracle (a chave é o código IBGE)
    private int sincronizarMunicipios() {
        List<List<String>> linhas = baseDosDados.consultar(
                "SELECT id_municipio, nome, sigla_uf FROM " + TB_MUNICIPIOS + " WHERE sigla_uf = @uf",
                Map.of("uf", uf));
        return transacao.execute(status -> {
            Set<Long> existentes = new HashSet<>(municipioRepository.listarCodigos());
            int novos = 0;
            for (List<String> linha : linhas) {
                Long codigo = Long.valueOf(linha.get(0));
                if (existentes.add(codigo)) {
                    municipioRepository.save(new Municipio(codigo, linha.get(1), linha.get(2)));
                    novos++;
                }
            }
            return novos;
        });
    }

    private SincronizacaoDTO.IndicadorSincronizado sincronizarIndicador(Indicador indicador, Consulta consulta,
                                                                        Set<Long> municipios) {
        // 1. Período mais recente disponível na Base dos Dados
        List<List<String>> resultado = baseDosDados.consultar(consulta.sqlUltimoPeriodo(), Map.of("uf", uf));
        List<String> periodo = resultado.isEmpty() ? null : resultado.get(0);
        if (periodo == null || periodo.get(0) == null) {
            throw new ServicoIndisponivelException("A Base dos Dados não retornou nenhum período de "
                    + indicador.getCodigo() + " para a UF " + uf + ".");
        }
        int ano = Integer.parseInt(periodo.get(0));
        int mes = Integer.parseInt(periodo.get(1));
        LocalDate referencia = LocalDate.of(ano, mes, 1);

        // 2. Valor de cada município nesse período (indicadores anuais não usam o mês)
        Map<String, Object> parametros = new HashMap<>(Map.of("uf", uf, "ano", ano));
        if (indicador.getPeriodicidade() == Periodicidade.MENSAL) {
            parametros.put("mes", mes);
        }
        List<List<String>> linhas = baseDosDados.consultar(consulta.sqlValores(), parametros);

        // 3. Grava no Oracle em uma transação por indicador: insere o que é novo e atualiza o que a fonte revisou
        return transacao.execute(status -> {
            Map<IndicadorMunicipioId, IndicadorMunicipio> existentes = new HashMap<>();
            for (IndicadorMunicipio v : valorRepository.buscarPorIndicador(indicador.getId())) {
                existentes.put(v.getId(), v);
            }
            int novas = 0;
            int atualizadas = 0;
            int ignoradas = 0;
            for (List<String> linha : linhas) {
                Long codigo = linha.get(0) == null ? null : Long.valueOf(linha.get(0));
                // Linhas sem município conhecido (código vazio ou "ignorado" na fonte) não entram: a FK exige o município
                if (codigo == null || linha.get(1) == null || !municipios.contains(codigo)) {
                    ignoradas++;
                    continue;
                }
                BigDecimal valor = new BigDecimal(linha.get(1));
                IndicadorMunicipioId id = new IndicadorMunicipioId(codigo, indicador.getId(), referencia);
                IndicadorMunicipio atual = existentes.get(id);
                if (atual == null) {
                    valorRepository.save(new IndicadorMunicipio(id, municipioRepository.getReferenceById(codigo),
                            indicadorRepository.getReferenceById(indicador.getId()), valor));
                    novas++;
                } else if (atual.getValor().compareTo(valor) != 0) {
                    atual.setValor(valor);
                    atualizadas++;
                }
            }
            // A data de consulta da fonte mostra ao cidadão quando o número foi conferido na Base dos Dados
            indicadorRepository.findById(indicador.getId())
                    .ifPresent(i -> i.getFonte().setDataConsulta(LocalDate.now(FUSO)));

            return new SincronizacaoDTO.IndicadorSincronizado(indicador.getCodigo(),
                    PanoramaDTO.formatarPeriodo(indicador.getPeriodicidade(), referencia),
                    linhas.size(), novas, atualizadas, ignoradas);
        });
    }
}
