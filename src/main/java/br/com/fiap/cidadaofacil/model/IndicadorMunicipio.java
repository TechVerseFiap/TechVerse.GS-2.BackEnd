package br.com.fiap.cidadaofacil.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

// Entidade: valor agregado de um indicador em um município e período - tabela INDICADOR_MUNICIPIO
@Entity
@Table(name = "INDICADOR_MUNICIPIO")
public class IndicadorMunicipio {
    @EmbeddedId
    private IndicadorMunicipioId id;

    @MapsId("municipioId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_MUNICIPIO")
    private Municipio municipio;

    @MapsId("indicadorId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_INDICADOR")
    private Indicador indicador;

    @Column(name = "VL_INDICADOR", nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    public IndicadorMunicipio() {
    }

    // Getters
    public IndicadorMunicipioId getId() { return id; }
    public Municipio getMunicipio() { return municipio; }
    public Indicador getIndicador() { return indicador; }
    public BigDecimal getValor() { return valor; }
}
