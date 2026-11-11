package br.com.fiap.cidadaofacil.exception;

// Erro 404: registro não encontrado
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String recurso, Long id) {
        super(recurso + " com id " + id + " não foi encontrado(a).");
    }
}
