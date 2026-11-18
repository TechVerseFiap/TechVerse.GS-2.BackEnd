package br.com.fiap.cidadaofacil.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

// Chave composta de SERVICO_DOCUMENTO (id_servico + id_documento)
@Embeddable
public class ServicoDocumentoId implements Serializable {
    @Column(name = "ID_SERVICO")
    private Long servicoId;

    @Column(name = "ID_DOCUMENTO")
    private Long documentoId;

    public ServicoDocumentoId() {
    }

    public ServicoDocumentoId(Long servicoId, Long documentoId) {
        this.servicoId = servicoId;
        this.documentoId = documentoId;
    }

    public Long getServicoId() { return servicoId; }
    public Long getDocumentoId() { return documentoId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServicoDocumentoId outro)) return false;
        return Objects.equals(servicoId, outro.servicoId) && Objects.equals(documentoId, outro.documentoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(servicoId, documentoId);
    }
}
