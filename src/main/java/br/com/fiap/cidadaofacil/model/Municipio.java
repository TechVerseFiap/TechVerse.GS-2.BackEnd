package br.com.fiap.cidadaofacil.model;

import jakarta.persistence.*;

// Entidade: município do panorama, identificado pelo código IBGE - tabela MUNICIPIO
@Entity
@Table(name = "MUNICIPIO")
public class Municipio {
    @Id
    @Column(name = "ID_MUNICIPIO")
    private Long codigoIbge;

    @Column(name = "NM_MUNICIPIO", nullable = false, length = 100)
    private String nome;

    @Column(name = "SG_UF", nullable = false, length = 2)
    private String uf;

    public Municipio() {
    }

    public Municipio(Long codigoIbge, String nome, String uf) {
        this.codigoIbge = codigoIbge;
        this.nome = nome;
        this.uf = uf;
    }

    // Getters
    public Long getCodigoIbge() { return codigoIbge; }
    public String getNome() { return nome; }
    public String getUf() { return uf; }
}
