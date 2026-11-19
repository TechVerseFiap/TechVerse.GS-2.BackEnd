package br.com.fiap.cidadaofacil.service;

import br.com.fiap.cidadaofacil.dto.CatalogoGovBrDTO;
import br.com.fiap.cidadaofacil.model.Canal;
import br.com.fiap.cidadaofacil.model.Categoria;
import br.com.fiap.cidadaofacil.model.Documento;
import br.com.fiap.cidadaofacil.model.EsferaGoverno;
import br.com.fiap.cidadaofacil.model.Etapa;
import br.com.fiap.cidadaofacil.model.Fonte;
import br.com.fiap.cidadaofacil.model.Orgao;
import br.com.fiap.cidadaofacil.model.Requisito;
import br.com.fiap.cidadaofacil.model.Servico;
import br.com.fiap.cidadaofacil.model.ServicoDocumento;
import br.com.fiap.cidadaofacil.model.StatusServico;
import br.com.fiap.cidadaofacil.model.TipoCanal;
import br.com.fiap.cidadaofacil.model.TipoFonte;
import br.com.fiap.cidadaofacil.repository.CategoriaRepository;
import br.com.fiap.cidadaofacil.repository.DocumentoRepository;
import br.com.fiap.cidadaofacil.repository.FonteRepository;
import br.com.fiap.cidadaofacil.repository.GovBrRepository;
import br.com.fiap.cidadaofacil.repository.OrgaoRepository;
import br.com.fiap.cidadaofacil.repository.ServicoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

// Service: monta o catálogo (serviços, órgãos, requisitos, etapas, documentos e canais) a partir do gov.br.
// Cada serviço é identificado pela URL oficial (FONTE.DS_URL): rodar de novo atualiza em vez de duplicar.
@Service
public class CatalogoGovBrService {
    private static final Logger log = LoggerFactory.getLogger(CatalogoGovBrService.class);
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final int MAX_REQUISITOS = 12;
    private static final int MAX_CANAIS_POR_TIPO = 3;

    // Nossas categorias e as categorias do gov.br voltadas ao cidadão que alimentam cada uma.
    // Ficaram de fora categorias com serviços de instituições específicas (ex.: ingresso em cada universidade federal)
    // ou voltados a gestores (ex.: habilitação de hospitais), que não ajudam o público do MVP.
    record Mapeamento(String categoria, String descricao, List<String> caminhosGovBr) {
    }

    static final List<Mapeamento> MAPA = List.of(
            new Mapeamento("Trabalho", "Emprego, carteira de trabalho e apoio a quem perdeu o emprego.", List.of(
                    "trabalho-e-previdencia/trabalho-e-emprego/para-o-cidadao",
                    "trabalho-e-previdencia/trabalho-e-emprego/para-todos")),
            new Mapeamento("Educação", "Cursos gratuitos e qualificação profissional.", List.of(
                    "educacao-e-pesquisa/capacitacoes/para-todos",
                    "educacao-e-pesquisa/ensino-tecnico-profissional/apoio-especializado",
                    "educacao-e-pesquisa/gestao-educacional/eja")),
            new Mapeamento("Documentos", "Emissão de documentos pessoais.", List.of(
                    "transito-e-transportes/terrestre/transito",
                    "financas-impostos-e-gestao-publica/cpf-cnpj-e-outros-cadastros/cpf",
                    "justica-e-seguranca/nacionalidade-estadia-e-outros-direitos/nacionalidade",
                    "forcas-armadas-e-defesa-civil/documentacao-servico-militar-e-defesa-civil/servico-militar",
                    "trabalho-e-previdencia/previdencia/certidoes-e-outros-documentos",
                    "educacao-e-pesquisa/outras/certidoes-e-outros-documentos")),
            new Mapeamento("Benefícios", "Programas sociais e cadastros para acesso a benefícios.", List.of(
                    "assistencia-social/programas-sociais/assistencia-direta",
                    "trabalho-e-previdencia/previdencia/beneficios",
                    "assistencia-social/rede-de-assistencia-e-protecao-social/protecao-social",
                    "assistencia-social/subsistencia/distribuicao-de-alimentos",
                    "assistencia-social/programas-sociais/financiamento-da-casa-propria")),
            new Mapeamento("Saúde", "Vacinação, atendimento e informações do SUS.", List.of(
                    "saude-e-vigilancia-sanitaria/prevencao/vacinacao",
                    "saude-e-vigilancia-sanitaria/rede-de-atendimento-a-saude/urgencias-e-emergencias",
                    "saude-e-vigilancia-sanitaria/rede-de-atendimento-a-saude/informacoes-e-sistemas")));

    // Palavras que aparecem em quase todo nome de serviço e não ajudam o assistente a diferenciar
    private static final Set<String> PALAVRAS_COMUNS = Set.of(
            "para", "pelo", "pela", "com", "sem", "sobre", "entre", "seus", "suas", "meu", "minha",
            "obter", "solicitar", "consultar", "emitir", "realizar", "acessar", "cadastrar", "inscrever",
            "receber", "requerer", "pedir", "fazer", "atualizar", "alterar", "cancelar", "renovar", "registrar",
            "apresentar", "participar", "acompanhar", "verificar", "informar", "comunicar", "agendar",
            "servico", "servicos", "online", "digital", "nacional", "federal", "programa", "informacoes",
            "brasil", "brasileiro", "brasileira", "governo", "orgao", "publico", "publica", "dados", "pessoa",
            "dos", "das", "nos", "nas", "por", "seu", "sua", "uma", "que", "aos", "nao");

    private final GovBrRepository govBr;
    private final ServicoRepository servicoRepository;
    private final CategoriaRepository categoriaRepository;
    private final OrgaoRepository orgaoRepository;
    private final DocumentoRepository documentoRepository;
    private final FonteRepository fonteRepository;
    private final TransactionTemplate transacao;
    private final int limitePorCategoria;
    private final int diasValidade;

    // Caches da execução: nome (minúsculo) -> id, para não consultar o banco a cada documento/órgão
    private final Map<String, Long> documentos = new HashMap<>();
    private final Map<String, Long> orgaos = new HashMap<>();
    private final Set<String> siglas = new HashSet<>();

    public CatalogoGovBrService(GovBrRepository govBr, ServicoRepository servicoRepository,
                                CategoriaRepository categoriaRepository, OrgaoRepository orgaoRepository,
                                DocumentoRepository documentoRepository, FonteRepository fonteRepository,
                                TransactionTemplate transacao,
                                @Value("${govbr.max-servicos-por-categoria:0}") int limitePorCategoria,
                                @Value("${govbr.dias-validade:7}") int diasValidade) {
        this.govBr = govBr;
        this.servicoRepository = servicoRepository;
        this.categoriaRepository = categoriaRepository;
        this.orgaoRepository = orgaoRepository;
        this.documentoRepository = documentoRepository;
        this.fonteRepository = fonteRepository;
        this.transacao = transacao;
        this.limitePorCategoria = limitePorCategoria;
        this.diasValidade = diasValidade;
    }

    // O catálogo precisa ser atualizado se ainda não foi importado ou se a consulta mais antiga passou da validade
    public boolean precisaAtualizar() {
        if (fonteRepository.countByUrlStartingWith(GovBrRepository.PREFIXO_SERVICO) < 20) {
            return true;
        }
        LocalDate maisAntiga = fonteRepository.dataConsultaMaisAntiga(GovBrRepository.PREFIXO_SERVICO);
        return maisAntiga == null || maisAntiga.isBefore(hoje().minusDays(diasValidade));
    }

    public synchronized CatalogoGovBrDTO.Resultado sincronizar(Consumer<String> progresso) {
        LocalDateTime inicio = LocalDateTime.now(FUSO);
        carregarCaches();

        // 1. Lista os serviços de cada categoria do gov.br (um serviço fica na primeira categoria em que aparece)
        Map<String, Long> slugs = new LinkedHashMap<>();
        Map<String, Integer> porCategoria = new LinkedHashMap<>();
        for (Mapeamento m : MAPA) {
            Long categoriaId = categoria(m).getId();
            int antes = slugs.size();
            for (String caminho : m.caminhosGovBr()) {
                progresso.accept("Listando serviços de " + m.categoria() + " no gov.br...");
                for (String slug : govBr.listarServicos(caminho, limitePorCategoria)) {
                    slugs.putIfAbsent(slug, categoriaId);
                }
            }
            porCategoria.put(m.categoria(), slugs.size() - antes);
        }

        // 2. Lê e grava cada serviço (pula os conferidos há menos de N dias: a execução pode ser retomada)
        int novos = 0;
        int atualizados = 0;
        int pulados = 0;
        int erros = 0;
        int i = 0;
        for (Map.Entry<String, Long> item : slugs.entrySet()) {
            progresso.accept("Catálogo gov.br: serviço " + (++i) + " de " + slugs.size());
            String url = GovBrRepository.PREFIXO_SERVICO + item.getKey();
            if (conferidoRecentemente(url)) {
                pulados++;
                continue;
            }
            try {
                Optional<CatalogoGovBrDTO.ServicoLido> lido = govBr.buscarServico(item.getKey());
                if (lido.isEmpty()) {
                    erros++;
                    continue;
                }
                boolean novo = Boolean.TRUE.equals(transacao.execute(st -> gravar(lido.get(), item.getValue())));
                if (novo) {
                    novos++;
                } else {
                    atualizados++;
                }
            } catch (RuntimeException e) {
                // Um serviço com problema não interrompe os demais; os caches são recarregados do banco
                erros++;
                log.warn("Serviço {} do gov.br não foi importado: {}", item.getKey(), e.getMessage());
                carregarCaches();
            }
        }
        CatalogoGovBrDTO.Resultado resultado = new CatalogoGovBrDTO.Resultado(inicio, LocalDateTime.now(FUSO),
                slugs.size(), novos, atualizados, pulados, erros, porCategoria);
        log.info("Catálogo do gov.br sincronizado: {}", resultado);
        return resultado;
    }

    // Grava um serviço lido do gov.br; devolve true se ele é novo no catálogo
    private boolean gravar(CatalogoGovBrDTO.ServicoLido lido, Long categoriaId) {
        Fonte fonte = fonteRepository.findByUrl(lido.url())
                .orElseGet(() -> new Fonte(limitar("gov.br - " + lido.nome(), 150), lido.url(), TipoFonte.OFICIAL, hoje()));
        fonte.setTipo(TipoFonte.OFICIAL);
        fonte.setDataConsulta(hoje());
        fonte = fonteRepository.save(fonte);

        Optional<Servico> existente = servicoRepository.findFirstByFonteId(fonte.getId());
        Servico servico = existente.orElseGet(Servico::new);
        if (existente.isEmpty()) {
            servico.setCategoria(categoriaRepository.getReferenceById(categoriaId));
            servico.setFonte(fonte);
            servico.setNome(limitar(lido.nome(), 120));
            servico.setDescricao(resumir(lido.descricao().isEmpty() ? lido.nome() : lido.descricao(), 1000));
            servico.setPublicoAlvo(limitar(lido.publico().isEmpty() ? ServicoService.PUBLICO_PADRAO : lido.publico(), 300));
            servico.setStatus(StatusServico.ATIVO);
        }
        // Serviço que já existia: mantém nome e descrição (podem ter sido reescritos em linguagem simples pela equipe)
        servico.setPalavrasChave(palavrasChave(lido, servico.getPalavrasChave()));
        servico.setOrgao(orgaoRepository.getReferenceById(orgao(lido.orgao())));

        // Troca requisitos, etapas, documentos e canais pelos do gov.br.
        // O flush apaga os antigos antes de inserir os novos (ordem da etapa e canal são únicos no banco).
        servico.getRequisitos().clear();
        servico.getEtapas().clear();
        servico.getDocumentos().clear();
        servico.getCanais().clear();
        Servico salvo = servicoRepository.saveAndFlush(servico);

        lido.requisitos().stream().limit(MAX_REQUISITOS)
                .forEach(r -> salvo.adicionarRequisito(new Requisito(limitar(r, 600))));
        for (int ordem = 1; ordem <= lido.etapas().size() && ordem <= 999; ordem++) {
            salvo.adicionarEtapa(new Etapa(ordem, limitar(lido.etapas().get(ordem - 1), 600)));
        }
        adicionarCanais(salvo, lido);
        Set<Long> vinculados = new HashSet<>();
        for (CatalogoGovBrDTO.DocumentoLido d : lido.documentos()) {
            Long documentoId = documento(d);
            if (vinculados.add(documentoId)) {
                String observacao = d.etapa().isEmpty() ? null : limitar("Pedido na etapa: " + d.etapa(), 400);
                salvo.getDocumentos().add(new ServicoDocumento(salvo,
                        documentoRepository.getReferenceById(documentoId), d.obrigatorio(), observacao));
            }
        }
        servicoRepository.save(salvo);
        return existente.isEmpty();
    }

    // Canal digital: o botão "Iniciar" e os links "Web"; sem link, usa a própria página do gov.br
    private void adicionarCanais(Servico servico, CatalogoGovBrDTO.ServicoLido lido) {
        Set<String> digitais = new LinkedHashSet<>();
        lido.linksDigitais().forEach(l -> digitais.add(limitar(l, 500)));
        if (digitais.isEmpty()) {
            digitais.add(lido.url());
        }
        digitais.stream().limit(MAX_CANAIS_POR_TIPO).forEach(l -> servico.adicionarCanal(new Canal(TipoCanal.DIGITAL, l)));
        Set<String> presenciais = new LinkedHashSet<>();
        lido.enderecosPresenciais().forEach(e -> presenciais.add(limitar(e, 500)));
        presenciais.stream().limit(MAX_CANAIS_POR_TIPO)
                .forEach(e -> servico.adicionarCanal(new Canal(TipoCanal.PRESENCIAL, e)));
    }

    // Documento pelo nome (sem diferenciar maiúsculas); cria se ainda não existe
    private Long documento(CatalogoGovBrDTO.DocumentoLido d) {
        String nome = d.nome().length() <= 100 ? d.nome() : d.nome().substring(0, 97).trim() + "...";
        return documentos.computeIfAbsent(nome.toLowerCase(),
                k -> documentoRepository.save(new Documento(nome, limitar(d.descricao(), 400))).getId());
    }

    // Órgão pelo nome; cria com uma sigla derivada do nome (sigla é única no banco)
    private Long orgao(String nome) {
        String nomeOrgao = nome == null || nome.isBlank() ? "Governo Federal" : limitar(nome, 150);
        return orgaos.computeIfAbsent(nomeOrgao.toLowerCase(), k -> {
            String sigla = siglaUnica(nomeOrgao);
            siglas.add(sigla);
            return orgaoRepository.save(new Orgao(nomeOrgao, sigla, EsferaGoverno.FEDERAL)).getId();
        });
    }

    // Sigla: a que vier entre parênteses no nome ou as iniciais das palavras ("Ministério do Trabalho e Emprego" -> MTE)
    static String sigla(String nome) {
        var parenteses = java.util.regex.Pattern.compile("\\(([A-Za-z]{2,15})\\)").matcher(nome);
        if (parenteses.find()) {
            return parenteses.group(1).toUpperCase();
        }
        StringBuilder sb = new StringBuilder();
        for (String palavra : AssistenteService.normalizar(nome).split(" ")) {
            if (palavra.length() > 2 && !Set.of("dos", "das", "para", "com").contains(palavra)) {
                sb.append(Character.toUpperCase(palavra.charAt(0)));
            }
        }
        String s = sb.isEmpty() ? "ORG" : sb.toString();
        return s.length() > 16 ? s.substring(0, 16) : s;
    }

    private String siglaUnica(String nome) {
        String base = sigla(nome);
        String sigla = base;
        for (int n = 2; siglas.contains(sigla.toUpperCase()); n++) {
            sigla = base + n;
        }
        return sigla;
    }

    // Palavras-chave: as que já existiam + as do gov.br + as palavras que diferenciam o nome e o endereço do serviço.
    // O endereço traz siglas que não estão no nome (ex.: ...-habilitacao-digital-cnh-e), por isso aceita 3 letras.
    static String palavrasChave(CatalogoGovBrDTO.ServicoLido lido, String atuais) {
        Set<String> palavras = new LinkedHashSet<>();
        if (atuais != null && !atuais.isBlank()) {
            for (String p : atuais.split(",")) {
                if (!p.isBlank()) {
                    palavras.add(p.trim().toLowerCase());
                }
            }
        }
        lido.palavrasChave().forEach(p -> palavras.add(p.trim().toLowerCase()));
        String texto = AssistenteService.normalizar(lido.nome() + " " + lido.slug().replace('-', ' '));
        for (String palavra : texto.split(" ")) {
            if (palavra.length() >= 3 && !PALAVRAS_COMUNS.contains(palavra)) {
                palavras.add(palavra);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String p : palavras) {
            if (sb.length() + p.length() + 1 > 400) {
                break;
            }
            sb.append(sb.isEmpty() ? "" : ",").append(p);
        }
        return sb.isEmpty() ? null : sb.toString();
    }

    private boolean conferidoRecentemente(String url) {
        return fonteRepository.findByUrl(url)
                .filter(f -> f.getDataConsulta() != null && !f.getDataConsulta().isBefore(hoje().minusDays(diasValidade)))
                .map(f -> servicoRepository.countByFonteId(f.getId()) > 0)
                .orElse(false);
    }

    private Categoria categoria(Mapeamento m) {
        String chave = AssistenteService.normalizar(m.categoria());
        return categoriaRepository.findAll().stream()
                .filter(c -> AssistenteService.normalizar(c.getNome()).equals(chave))
                .findFirst()
                .orElseGet(() -> categoriaRepository.save(new Categoria(m.categoria(), m.descricao())));
    }

    private void carregarCaches() {
        documentos.clear();
        orgaos.clear();
        siglas.clear();
        documentoRepository.findAll().forEach(d -> documentos.put(d.getNome().toLowerCase(), d.getId()));
        orgaoRepository.findAll().forEach(o -> {
            orgaos.put(o.getNome().toLowerCase(), o.getId());
            siglas.add(o.getSigla().toUpperCase());
        });
    }

    // Descrição: corta no fim de uma frase para caber na coluna
    static String resumir(String texto, int max) {
        if (texto.length() <= max) {
            return texto;
        }
        String corte = texto.substring(0, max);
        int fimFrase = corte.lastIndexOf(". ");
        return fimFrase > max / 2 ? corte.substring(0, fimFrase + 1) : corte.substring(0, max - 3).trim() + "...";
    }

    private static String limitar(String texto, int max) {
        return texto.length() <= max ? texto : texto.substring(0, max - 3).trim() + "...";
    }

    private static LocalDate hoje() {
        return LocalDate.now(FUSO);
    }
}
