package br.com.fiap.cidadaofacil.bo;

import br.com.fiap.cidadaofacil.dao.DocumentoDAO;
import br.com.fiap.cidadaofacil.dao.RecomendacaoDAO;
import br.com.fiap.cidadaofacil.dao.ServicoDAO;
import br.com.fiap.cidadaofacil.dao.ServicoDocumentoDAO;
import br.com.fiap.cidadaofacil.dto.DocumentoDTO;
import br.com.fiap.cidadaofacil.dto.ServicoDTO;
import br.com.fiap.cidadaofacil.exception.ConflitoException;
import br.com.fiap.cidadaofacil.exception.RecursoNaoEncontradoException;
import br.com.fiap.cidadaofacil.exception.RegraNegocioException;
import br.com.fiap.cidadaofacil.model.Documento;
import br.com.fiap.cidadaofacil.model.Servico;
import br.com.fiap.cidadaofacil.model.ServicoDocumento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// BO: regras de negócio de serviços e seus documentos
@Service
public class ServicoBO {
    private final ServicoDAO servicoDAO;
    private final DocumentoDAO documentoDAO;
    private final ServicoDocumentoDAO servicoDocumentoDAO;
    private final RecomendacaoDAO recomendacaoDAO;
    private final CategoriaBO categoriaBO;

    public ServicoBO(ServicoDAO servicoDAO, DocumentoDAO documentoDAO, ServicoDocumentoDAO servicoDocumentoDAO,
                     RecomendacaoDAO recomendacaoDAO, CategoriaBO categoriaBO) {
        this.servicoDAO = servicoDAO;
        this.documentoDAO = documentoDAO;
        this.servicoDocumentoDAO = servicoDocumentoDAO;
        this.recomendacaoDAO = recomendacaoDAO;
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
                    + "' já foi recomendado pelo assistente e faz parte do histórico. Não pode ser excluído.");
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
        ServicoDocumento vinculo = servicoDocumentoDAO.save(new ServicoDocumento(servico, documento, req.observacao()));
        return DocumentoDTO.DoServico.de(vinculo);
    }

    @Transactional
    public DocumentoDTO.DoServico atualizarVinculoDocumento(Long servicoId, Long documentoId, DocumentoDTO.Vinculo req) {
        buscarEntidade(servicoId);
        ServicoDocumento vinculo = servicoDocumentoDAO.findByServicoIdAndDocumentoId(servicoId, documentoId)
                .orElseThrow(() -> new RegraNegocioException(
                        "O documento " + documentoId + " não está vinculado ao serviço " + servicoId + "."));
        vinculo.setObservacao(req.observacao());
        return DocumentoDTO.DoServico.de(servicoDocumentoDAO.save(vinculo));
    }

    @Transactional
    public void desvincularDocumento(Long servicoId, Long documentoId) {
        Servico servico = buscarEntidade(servicoId);
        ServicoDocumento vinculo = servicoDocumentoDAO.findByServicoIdAndDocumentoId(servicoId, documentoId)
                .orElseThrow(() -> new RegraNegocioException(
                        "O documento " + documentoId + " não está vinculado ao serviço " + servicoId + "."));
        servico.getDocumentos().remove(vinculo);
        servicoDocumentoDAO.delete(vinculo);
    }

    // Apoio: busca a entidade ou lança 404
    @Transactional(readOnly = true)
    public Servico buscarEntidade(Long id) {
        return servicoDAO.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço", id));
    }

    // Aplica os dados da requisição aplicando as regras de negócio
    private void preencher(Servico servico, ServicoDTO.Request req) {
        validarFonte(req.urlFonte());

        servico.setCategoria(categoriaBO.buscarEntidade(req.categoriaId()));
        servico.setNome(req.nome().trim());
        servico.setDescricao(req.descricao().trim());
        servico.setPublicoAlvo(req.publicoAlvo());
        servico.setOrgaoResponsavel(req.orgaoResponsavel().trim());
        servico.setCanal(req.canal());
        servico.setUrlFonte(req.urlFonte().trim());
        servico.setDataVerificacao(req.dataVerificacao());
        servico.setPalavrasChave(normalizarPalavrasChave(req.palavrasChave()));
    }

    // Regra central: toda orientação precisa de fonte oficial com https
    private void validarFonte(String url) {
        String u = url.trim().toLowerCase();
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
        return String.join(",", java.util.Arrays.stream(palavras.split(","))
                .map(p -> p.trim().toLowerCase())
                .filter(p -> !p.isEmpty())
                .distinct()
                .toList());
    }
}
