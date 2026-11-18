package br.com.fiap.cidadaofacil.bo;

import br.com.fiap.cidadaofacil.dao.DocumentoDAO;
import br.com.fiap.cidadaofacil.dao.FonteDAO;
import br.com.fiap.cidadaofacil.dao.OrgaoDAO;
import br.com.fiap.cidadaofacil.dao.RecomendacaoDAO;
import br.com.fiap.cidadaofacil.dao.ServicoDAO;
import br.com.fiap.cidadaofacil.dao.ServicoDocumentoDAO;
import br.com.fiap.cidadaofacil.dto.DocumentoDTO;
import br.com.fiap.cidadaofacil.dto.ServicoDTO;
import br.com.fiap.cidadaofacil.exception.ConflitoException;
import br.com.fiap.cidadaofacil.exception.RecursoNaoEncontradoException;
import br.com.fiap.cidadaofacil.exception.RegraNegocioException;
import br.com.fiap.cidadaofacil.model.Documento;
import br.com.fiap.cidadaofacil.model.Fonte;
import br.com.fiap.cidadaofacil.model.Orgao;
import br.com.fiap.cidadaofacil.model.Servico;
import br.com.fiap.cidadaofacil.model.ServicoDocumento;
import br.com.fiap.cidadaofacil.model.StatusServico;
import br.com.fiap.cidadaofacil.model.TipoFonte;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

// BO: regras de negócio de serviços, com órgão, fonte oficial, canais e documentos
@Service
public class ServicoBO {
    static final String PUBLICO_PADRAO = "Público geral. Confira os critérios na fonte oficial.";
    static final String ENDERECO_PRESENCIAL_PADRAO = "Atendimento presencial: consulte o órgão responsável.";

    private final ServicoDAO servicoDAO;
    private final DocumentoDAO documentoDAO;
    private final ServicoDocumentoDAO servicoDocumentoDAO;
    private final RecomendacaoDAO recomendacaoDAO;
    private final OrgaoDAO orgaoDAO;
    private final FonteDAO fonteDAO;
    private final CategoriaBO categoriaBO;

    public ServicoBO(ServicoDAO servicoDAO, DocumentoDAO documentoDAO, ServicoDocumentoDAO servicoDocumentoDAO,
                     RecomendacaoDAO recomendacaoDAO, OrgaoDAO orgaoDAO, FonteDAO fonteDAO, CategoriaBO categoriaBO) {
        this.servicoDAO = servicoDAO;
        this.documentoDAO = documentoDAO;
        this.servicoDocumentoDAO = servicoDocumentoDAO;
        this.recomendacaoDAO = recomendacaoDAO;
        this.orgaoDAO = orgaoDAO;
        this.fonteDAO = fonteDAO;
        this.categoriaBO = categoriaBO;
    }

    // Consultas com filtros opcionais de categoria e texto
    @Transactional(readOnly = true)
    public List<ServicoDTO.Resumo> listar(Long categoriaId, String busca) {
        if (categoriaId != null) {
            categoriaBO.buscarEntidade(categoriaId);
        }
        List<Servico> servicos;
        if (busca != null && !busca.isBlank()) {
            servicos = servicoDAO.buscarPorTexto(busca.trim(), categoriaId);
        } else if (categoriaId != null) {
            servicos = servicoDAO.findByCategoriaIdOrderByNomeAsc(categoriaId);
        } else {
            servicos = servicoDAO.findAllByOrderByNomeAsc();
        }
        return servicos.stream().map(ServicoDTO.Resumo::de).toList();
    }

    @Transactional(readOnly = true)
    public ServicoDTO.Detalhe detalhar(Long id) {
        return ServicoDTO.Detalhe.de(buscarEntidade(id));
    }

    // Criação e atualização
    @Transactional
    public ServicoDTO.Detalhe criar(ServicoDTO.Request req) {
        Servico servico = new Servico();
        preencher(servico, req);
        return ServicoDTO.Detalhe.de(servicoDAO.save(servico));
    }

    @Transactional
    public ServicoDTO.Detalhe atualizar(Long id, ServicoDTO.Request req) {
        Servico servico = buscarEntidade(id);
        preencher(servico, req);
        return ServicoDTO.Detalhe.de(servicoDAO.save(servico));
    }

    // Exclusão bloqueada se o serviço já está no histórico do assistente
    @Transactional
    public void excluir(Long id) {
        Servico servico = buscarEntidade(id);
        if (recomendacaoDAO.existsByServicoId(id)) {
            throw new ConflitoException("O serviço '" + servico.getNome()
                    + "' já foi recomendado pelo assistente e faz parte do histórico. "
                    + "Para tirá-lo do catálogo, marque-o como inativo.");
        }
        servicoDAO.delete(servico);
    }

    // Vínculo de documentos com o serviço
    @Transactional(readOnly = true)
    public List<DocumentoDTO.DoServico> listarDocumentos(Long servicoId) {
        buscarEntidade(servicoId);
        return servicoDocumentoDAO.findByServicoId(servicoId).stream()
                .map(DocumentoDTO.DoServico::de)
                .toList();
    }

    @Transactional
    public DocumentoDTO.DoServico vincularDocumento(Long servicoId, DocumentoDTO.Vinculo req) {
        Servico servico = buscarEntidade(servicoId);
        Documento documento = documentoDAO.findById(req.documentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Documento", req.documentoId()));
        if (servicoDocumentoDAO.existsByServicoIdAndDocumentoId(servicoId, req.documentoId())) {
            throw new ConflitoException("O documento '" + documento.getNome() + "' já está vinculado a este serviço.");
        }
        boolean obrigatorio = req.obrigatorio() == null || req.obrigatorio();
        ServicoDocumento vinculo = new ServicoDocumento(servico, documento, obrigatorio, req.observacao());
        return DocumentoDTO.DoServico.de(servicoDocumentoDAO.save(vinculo));
    }

    @Transactional
    public DocumentoDTO.DoServico atualizarVinculoDocumento(Long servicoId, Long documentoId, DocumentoDTO.Vinculo req) {
        buscarEntidade(servicoId);
        ServicoDocumento vinculo = buscarVinculo(servicoId, documentoId);
        vinculo.setObservacao(req.observacao());
        if (req.obrigatorio() != null) {
            vinculo.setObrigatorio(req.obrigatorio());
        }
        return DocumentoDTO.DoServico.de(servicoDocumentoDAO.save(vinculo));
    }

    @Transactional
    public void desvincularDocumento(Long servicoId, Long documentoId) {
        Servico servico = buscarEntidade(servicoId);
        ServicoDocumento vinculo = buscarVinculo(servicoId, documentoId);
        servico.getDocumentos().remove(vinculo);
        servicoDocumentoDAO.delete(vinculo);
    }

    // Apoio: busca a entidade ou lança 404
    @Transactional(readOnly = true)
    public Servico buscarEntidade(Long id) {
        return servicoDAO.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço", id));
    }

    private ServicoDocumento buscarVinculo(Long servicoId, Long documentoId) {
        return servicoDocumentoDAO.findByServicoIdAndDocumentoId(servicoId, documentoId)
                .orElseThrow(() -> new RegraNegocioException(
                        "O documento " + documentoId + " não está vinculado ao serviço " + servicoId + "."));
    }

    // Aplica os dados da requisição seguindo as regras de negócio
    private void preencher(Servico servico, ServicoDTO.Request req) {
        String url = req.urlFonte().trim();
        validarFonte(url);

        servico.setCategoria(categoriaBO.buscarEntidade(req.categoriaId()));
        servico.setOrgao(resolverOrgao(req));
        servico.setNome(req.nome().trim());
        servico.setDescricao(req.descricao().trim());
        servico.setPublicoAlvo(req.publicoAlvo() == null || req.publicoAlvo().isBlank()
                ? PUBLICO_PADRAO : req.publicoAlvo().trim());
        servico.setFonte(registrarFonte(url, req.dataVerificacao(), servico.getNome()));
        servico.definirCanais(req.canal(), url, ENDERECO_PRESENCIAL_PADRAO);
        servico.setPalavrasChave(normalizarPalavrasChave(req.palavrasChave()));
        servico.setStatus(req.ativo() == null || req.ativo() ? StatusServico.ATIVO : StatusServico.INATIVO);
    }

    // Órgão por id; se não vier, procura pelo nome ou sigla já cadastrados
    private Orgao resolverOrgao(ServicoDTO.Request req) {
        if (req.orgaoId() != null) {
            return orgaoDAO.findById(req.orgaoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Órgão", req.orgaoId()));
        }
        String texto = req.orgaoResponsavel() == null ? "" : req.orgaoResponsavel().trim();
        if (texto.isEmpty()) {
            throw new RegraNegocioException("Informe o órgão responsável (orgaoId).");
        }
        return orgaoDAO.findFirstByNomeIgnoreCaseOrSiglaIgnoreCase(texto, texto)
                .orElseThrow(() -> new RegraNegocioException("O órgão '" + texto
                        + "' não está cadastrado. Cadastre-o em /api/orgaos e informe o orgaoId."));
    }

    // Reaproveita a fonte pela URL (atualizando a data de verificação) ou cria uma nova fonte oficial
    private Fonte registrarFonte(String url, java.time.LocalDate dataVerificacao, String nomeServico) {
        Fonte fonte = fonteDAO.findByUrl(url).orElseGet(() -> new Fonte(
                limitar("Fonte oficial - " + nomeServico, 150), url, TipoFonte.OFICIAL, dataVerificacao));
        fonte.setTipo(TipoFonte.OFICIAL);
        fonte.setDataConsulta(dataVerificacao);
        return fonteDAO.save(fonte);
    }

    // Regra central: toda orientação precisa de fonte oficial com https
    private void validarFonte(String url) {
        String u = url.toLowerCase();
        if (!u.startsWith("https://")) {
            throw new RegraNegocioException("A URL da fonte oficial deve começar com https://");
        }
        if (u.length() <= "https://".length() + 3 || !u.substring(8).contains(".")) {
            throw new RegraNegocioException("A URL da fonte oficial informada não é válida.");
        }
    }

    // Padroniza palavras-chave: minúsculas e sem repetição
    private String normalizarPalavrasChave(String palavras) {
        if (palavras == null || palavras.isBlank()) {
            return null;
        }
        return String.join(",", Arrays.stream(palavras.split(","))
                .map(p -> p.trim().toLowerCase())
                .filter(p -> !p.isEmpty())
                .distinct()
                .toList());
    }

    private static String limitar(String texto, int max) {
        return texto.length() <= max ? texto : texto.substring(0, max);
    }
}
