package br.com.fiap.cidadaofacil.model;

import jakarta.persistence.*;

// Entidade: órgão responsável pelo atendimento - tabela ORGAO
@Entity
@Table(name = "ORGAO")
public class Orgao {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqOrgao")
    @SequenceGenerator(name = "sqOrgao", sequenceName = "SQ_ORGAO", allocationSize = 1)
    @Column(name = "ID_ORGAO")
    private Long id;

    @Column(name = "NM_ORGAO", nullable = false, length = 150)
    private String nome;

    @Column(name = "SG_ORGAO", nullable = false, unique = true, length = 20)
    private String sigla;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_ESFERA", nullable = false, length = 10)
    private EsferaGoverno esfera;

    public Orgao() {
    }

    public Orgao(String nome, String sigla, EsferaGoverno esfera) {
        this.nome = nome;
        this.sigla = sigla;
        this.esfera = esfera;
    }

    // Getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getSigla() { return sigla; }
    public void setSigla(String sigla) { this.sigla = sigla; }

    public EsferaGoverno getEsfera() { return esfera; }
    public void setEsfera(EsferaGoverno esfera) { this.esfera = esfera; }
}
