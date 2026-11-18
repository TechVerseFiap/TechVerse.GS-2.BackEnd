package br.com.fiap.cidadaofacil.model;

import jakarta.persistence.*;

import java.time.LocalDate;

// Entidade: referência oficial que fundamenta serviços e indicadores - tabela FONTE
@Entity
@Table(name = "FONTE")
public class Fonte {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqFonte")
    @SequenceGenerator(name = "sqFonte", sequenceName = "SQ_FONTE", allocationSize = 1)
    @Column(name = "ID_FONTE")
    private Long id;

    @Column(name = "NM_TITULO", nullable = false, length = 150)
    private String titulo;

    @Column(name = "DS_URL", nullable = false, unique = true, length = 500)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_FONTE", nullable = false, length = 8)
    private TipoFonte tipo;

    @Column(name = "DT_CONSULTA")
    private LocalDate dataConsulta;

    public Fonte() {
    }

    public Fonte(String titulo, String url, TipoFonte tipo, LocalDate dataConsulta) {
        this.titulo = titulo;
        this.url = url;
        this.tipo = tipo;
        this.dataConsulta = dataConsulta;
    }

    // Getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public TipoFonte getTipo() { return tipo; }
    public void setTipo(TipoFonte tipo) { this.tipo = tipo; }

    public LocalDate getDataConsulta() { return dataConsulta; }
    public void setDataConsulta(LocalDate dataConsulta) { this.dataConsulta = dataConsulta; }
}
