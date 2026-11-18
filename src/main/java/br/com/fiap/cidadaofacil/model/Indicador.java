package br.com.fiap.cidadaofacil.model;

import jakarta.persistence.*;

// Entidade: indicador público exibido no panorama, com a sua fonte - tabela INDICADOR
@Entity
@Table(name = "INDICADOR")
public class Indicador {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqIndicador")
    @SequenceGenerator(name = "sqIndicador", sequenceName = "SQ_INDICADOR", allocationSize = 1)
    @Column(name = "ID_INDICADOR")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_FONTE", nullable = false)
    private Fonte fonte;

    @Column(name = "CD_INDICADOR", nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(name = "NM_INDICADOR", nullable = false, length = 120)
    private String nome;

    @Column(name = "DS_INDICADOR", nullable = false, length = 400)
    private String descricao;

    @Column(name = "DS_UNIDADE", nullable = false, length = 30)
    private String unidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_PERIODICIDADE", nullable = false, length = 6)
    private Periodicidade periodicidade;

    public Indicador() {
    }

    // Getters
    public Long getId() { return id; }
    public Fonte getFonte() { return fonte; }
    public String getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public String getUnidade() { return unidade; }
    public Periodicidade getPeriodicidade() { return periodicidade; }
}
