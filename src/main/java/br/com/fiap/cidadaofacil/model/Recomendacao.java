package br.com.fiap.cidadaofacil.model;

import jakarta.persistence.*;

// Entidade: serviço recomendado em uma interação, com justificativa - tabela T_CF_RECOMENDACAO
@Entity
@Table(name = "T_CF_RECOMENDACAO")
public class Recomendacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_RECOMENDACAO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_INTERACAO", nullable = false)
    private Interacao interacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_SERVICO", nullable = false)
    private Servico servico;

    @Column(name = "DS_JUSTIFICATIVA", nullable = false, length = 300)
    private String justificativa;

    @Column(name = "NR_PONTUACAO", nullable = false)
    private Integer pontuacao;

    public Recomendacao() {
    }

    public Recomendacao(Servico servico, String justificativa, Integer pontuacao) {
        this.servico = servico;
        this.justificativa = justificativa;
        this.pontuacao = pontuacao;
    }

    // Getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Interacao getInteracao() { return interacao; }
    public void setInteracao(Interacao interacao) { this.interacao = interacao; }

    public Servico getServico() { return servico; }
    public void setServico(Servico servico) { this.servico = servico; }

    public String getJustificativa() { return justificativa; }
    public void setJustificativa(String justificativa) { this.justificativa = justificativa; }

    public Integer getPontuacao() { return pontuacao; }
    public void setPontuacao(Integer pontuacao) { this.pontuacao = pontuacao; }
}
