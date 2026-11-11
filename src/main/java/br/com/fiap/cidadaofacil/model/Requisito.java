package br.com.fiap.cidadaofacil.model;

import jakarta.persistence.*;

// Entidade: critério publicado pela fonte oficial - tabela T_CF_REQUISITO
@Entity
@Table(name = "T_CF_REQUISITO")
public class Requisito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_REQUISITO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_SERVICO", nullable = false)
    private Servico servico;

    @Column(name = "DS_REQUISITO", nullable = false, length = 300)
    private String descricao;

    public Requisito() {
    }

    public Requisito(String descricao) {
        this.descricao = descricao;
    }

    // Getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Servico getServico() { return servico; }
    public void setServico(Servico servico) { this.servico = servico; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
