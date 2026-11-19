package br.com.fiap.cidadaofacil.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

// Chave composta de INDICADOR_MUNICIPIO (município + indicador + período)
@Embeddable
public class IndicadorMunicipioId implements Serializable {
    @Column(name = "ID_MUNICIPIO")
    private Long municipioId;

    @Column(name = "ID_INDICADOR")
    private Long indicadorId;

    @Column(name = "DT_REFERENCIA")
    private LocalDate referencia;

    public IndicadorMunicipioId() {
    }

    public IndicadorMunicipioId(Long municipioId, Long indicadorId, LocalDate referencia) {
        this.municipioId = municipioId;
        this.indicadorId = indicadorId;
        this.referencia = referencia;
    }

    public Long getMunicipioId() { return municipioId; }
    public Long getIndicadorId() { return indicadorId; }
    public LocalDate getReferencia() { return referencia; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndicadorMunicipioId outro)) return false;
        return Objects.equals(municipioId, outro.municipioId) && Objects.equals(indicadorId, outro.indicadorId)
                && Objects.equals(referencia, outro.referencia);
    }

    @Override
    public int hashCode() {
        return Objects.hash(municipioId, indicadorId, referencia);
    }
}
