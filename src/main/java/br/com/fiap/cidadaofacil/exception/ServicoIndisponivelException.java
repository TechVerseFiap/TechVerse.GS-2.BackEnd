package br.com.fiap.cidadaofacil.exception;

// Erro 503: serviço externo (Base dos Dados / BigQuery) não configurado ou fora do ar
public class ServicoIndisponivelException extends RuntimeException {
    public ServicoIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
