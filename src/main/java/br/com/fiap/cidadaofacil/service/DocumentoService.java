package br.com.fiap.cidadaofacil.service;

import br.com.fiap.cidadaofacil.repository.DocumentoRepository;
import br.com.fiap.cidadaofacil.repository.ServicoDocumentoRepository;
import br.com.fiap.cidadaofacil.dto.DocumentoDTO;
import br.com.fiap.cidadaofacil.exception.ConflitoException;
import br.com.fiap.cidadaofacil.exception.RecursoNaoEncontradoException;
import br.com.fiap.cidadaofacil.model.Documento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Service: regras de negócio de documentos
@Service
public class DocumentoService {
    private final DocumentoRepository documentoRepository;
    private final ServicoDocumentoRepository servicoDocumentoRepository;

    public DocumentoService(DocumentoRepository documentoRepository, ServicoDocumentoRepository servicoDocumentoRepository) {
        this.documentoRepository = documentoRepository;
        this.servicoDocumentoRepository = servicoDocumentoRepository;
    }

    @Transactional(readOnly = true)
    public List<DocumentoDTO.Response> listar() {
        return documentoRepository.findAllByOrderByNomeAsc().stream()
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
        if (documentoRepository.existsByNomeIgnoreCase(nome)) {
            throw new ConflitoException("Já existe um documento com o nome '" + nome + "'.");
        }
        return DocumentoDTO.Response.de(documentoRepository.save(new Documento(nome, CategoriaService.descricaoOuNome(req.descricao(), nome))));
    }

    @Transactional
    public DocumentoDTO.Response atualizar(Long id, DocumentoDTO.Request req) {
        Documento documento = buscarEntidade(id);
        String nome = req.nome().trim();
        if (documentoRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ConflitoException("Já existe outro documento com o nome '" + nome + "'.");
        }
        documento.setNome(nome);
        documento.setDescricao(CategoriaService.descricaoOuNome(req.descricao(), nome));
        return DocumentoDTO.Response.de(documentoRepository.save(documento));
    }

    // Exclusão bloqueada se o documento está vinculado a algum serviço
    @Transactional
    public void excluir(Long id) {
        Documento documento = buscarEntidade(id);
        if (servicoDocumentoRepository.existsByDocumentoId(id)) {
            throw new ConflitoException("O documento '" + documento.getNome()
                    + "' está vinculado a pelo menos um serviço. Desvincule antes de excluir.");
        }
        documentoRepository.delete(documento);
    }

    private Documento buscarEntidade(Long id) {
        return documentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Documento", id));
    }
}
