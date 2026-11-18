package br.com.fiap.cidadaofacil.model;

import jakarta.persistence.*;

// Entidade: passo do atendimento, com ordem única por serviço - tabela ETAPA
@Entity
@Table(name = "ETAPA",
        uniqueConstraints = @UniqueConstraint(name = "UQ_ETAPA_1", columnNames = {"ID_SERVICO", "NR_ORDEM"}))
public class Etapa {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqEtapa")
    @SequenceGenerator(name = "sqEtapa", sequenceName = "SQ_ETAPA", allocationSize = 1)
    @Column(name = "ID_ETAPA")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_SERVICO", nullable = false)
    private Servico servico;

    @Column(name = "NR_ORDEM", nullable = false)
    private Integer ordem;

    @Column(name = "DS_ETAPA", nullable = false, length = 600)
    private String descricao;

    public Etapa() {
    }

    public Etapa(Integer ordem, String descricao) {
        this.ordem = ordem;
        this.descricao = descricao;
    }

    // Getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Servico getServico() { return servico; }
    public void setServico(Servico servico) { this.servico = servico; }

    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
