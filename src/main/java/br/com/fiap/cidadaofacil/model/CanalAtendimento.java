package br.com.fiap.cidadaofacil.model;

// Resumo dos canais de um serviço exibido pela API (derivado da tabela CANAL)
public enum CanalAtendimento {
    DIGITAL,
    PRESENCIAL,
    DIGITAL_E_PRESENCIAL;

    public boolean incluiDigital() {
        return this != PRESENCIAL;
    }

    public boolean incluiPresencial() {
        return this != DIGITAL;
    }
}
