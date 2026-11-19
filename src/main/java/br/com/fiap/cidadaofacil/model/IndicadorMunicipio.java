package br.com.fiap.cidadaofacil.model;

import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;

// Entidade: valor agregado de um indicador em um município e período - tabela INDICADOR_MUNICIPIO.
// Implementa Persistable porque a chave é montada pela aplicação: assim o save() faz só o INSERT,
// sem um SELECT antes de cada linha (importante na sincronização com a Base dos Dados).
@Entity
@Table(name = "INDICADOR_MUNICIPIO")
public class IndicadorMunicipio implements Persistable<IndicadorMunicipioId> {
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

    // Novo até ser gravado ou lido do banco
    @Transient
    private boolean novo = true;

    public IndicadorMunicipio() {
    }

    public IndicadorMunicipio(IndicadorMunicipioId id, Municipio municipio, Indicador indicador, BigDecimal valor) {
        this.id = id;
        this.municipio = municipio;
        this.indicador = indicador;
        this.valor = valor;
    }

    @PostLoad
    @PostPersist
    void marcarComoExistente() {
        this.novo = false;
    }

    @Override
    public boolean isNew() { return novo; }

    // Getters e setter do valor (atualizado quando a fonte revisa o número)
    @Override
    public IndicadorMunicipioId getId() { return id; }
    public Municipio getMunicipio() { return municipio; }
    public Indicador getIndicador() { return indicador; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
}
