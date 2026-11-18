package br.com.fiap.cidadaofacil.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

// Chave composta de RECOMENDACAO (id_interacao + id_servico)
@Embeddable
public class RecomendacaoId implements Serializable {
    @Column(name = "ID_INTERACAO")
    private Long interacaoId;

    @Column(name = "ID_SERVICO")
    private Long servicoId;

    public RecomendacaoId() {
    }

    public Long getInteracaoId() { return interacaoId; }
    public Long getServicoId() { return servicoId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecomendacaoId outro)) return false;
        return Objects.equals(interacaoId, outro.interacaoId) && Objects.equals(servicoId, outro.servicoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interacaoId, servicoId);
    }
}
