package br.com.fiap.cidadaofacil.exception;

// Erro 422: regra de negócio violada
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
