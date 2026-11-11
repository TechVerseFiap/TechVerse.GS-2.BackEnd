package br.com.fiap.cidadaofacil.bo;

import br.com.fiap.cidadaofacil.dao.DocumentoDAO;
import br.com.fiap.cidadaofacil.dao.ServicoDocumentoDAO;
import br.com.fiap.cidadaofacil.dto.DocumentoDTO;
import br.com.fiap.cidadaofacil.exception.ConflitoException;
import br.com.fiap.cidadaofacil.exception.RecursoNaoEncontradoException;
import br.com.fiap.cidadaofacil.model.Documento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// BO: regras de negócio de documentos
@Service
public class DocumentoBO {
    private final DocumentoDAO documentoDAO;
    private final ServicoDocumentoDAO servicoDocumentoDAO;

    public DocumentoBO(DocumentoDAO documentoDAO, ServicoDocumentoDAO servicoDocumentoDAO) {
        this.documentoDAO = documentoDAO;
        this.servicoDocumentoDAO = servicoDocumentoDAO;
    }

    @Transactional(readOnly = true)
    public List<DocumentoDTO.Response> listar() {
        return documentoDAO.findAllByOrderByNomeAsc().stream()
                .map(DocumentoDTO.Response::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentoDTO.Response buscarPorId(Long id) {
        return DocumentoDTO.Response.de(buscarEntidade(id));
    }

    @Transactional
    public DocumentoDTO.Response criar(DocumentoDTO.Request req) {
        String nome = req.nome().trim();
        if (documentoDAO.existsByNomeIgnoreCase(nome)) {
            throw new ConflitoException("Já existe um documento com o nome '" + nome + "'.");
        }
        return DocumentoDTO.Response.de(documentoDAO.save(new Documento(nome, req.descricao())));
    }

    @Transactional
    public DocumentoDTO.Response atualizar(Long id, DocumentoDTO.Request req) {
        Documento documento = buscarEntidade(id);
        String nome = req.nome().trim();
        if (documentoDAO.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ConflitoException("Já existe outro documento com o nome '" + nome + "'.");
        }
        documento.setNome(nome);
        documento.setDescricao(req.descricao());
        return DocumentoDTO.Response.de(documentoDAO.save(documento));
    }

    // Exclusão bloqueada se o documento está vinculado a algum serviço
    @Transactional
    public void excluir(Long id) {
        Documento documento = buscarEntidade(id);
        if (servicoDocumentoDAO.existsByDocumentoId(id)) {
            throw new ConflitoException("O documento '" + documento.getNome()
                    + "' está vinculado a pelo menos um serviço. Desvincule antes de excluir.");
        }
        documentoDAO.delete(documento);
    }

    private Documento buscarEntidade(Long id) {
        return documentoDAO.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Documento", id));
    }
}
