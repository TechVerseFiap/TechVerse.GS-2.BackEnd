package br.com.fiap.cidadaofacil.model;

// Resultado de uma pergunta ao assistente; cada valor tem o código gravado em INTERACAO.ST_RESULTADO
public enum ResultadoInteracao {
    ENCONTRADO("COM_RESULTADO"),
    PEDIU_ESCLARECIMENTO("PEDIU_ESCLARECIMENTO"),
    NAO_ENCONTRADO("SEM_RESULTADO");

    private final String codigoBanco;

    ResultadoInteracao(String codigoBanco) {
        this.codigoBanco = codigoBanco;
    }

    public String getCodigoBanco() {
        return codigoBanco;
    }

    public static ResultadoInteracao doCodigo(String codigo) {
        for (ResultadoInteracao r : values()) {
            if (r.codigoBanco.equals(codigo)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Resultado de interação desconhecido: " + codigo);
    }
}
