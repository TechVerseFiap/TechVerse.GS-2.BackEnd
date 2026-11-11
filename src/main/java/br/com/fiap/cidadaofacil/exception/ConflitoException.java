package br.com.fiap.cidadaofacil.exception;

// Erro 409: conflito com dados existentes
public class ConflitoException extends RuntimeException {
    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
