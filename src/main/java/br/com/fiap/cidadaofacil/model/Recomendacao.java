package br.com.fiap.cidadaofacil.model;

import jakarta.persistence.*;

// Entidade: serviço recomendado em uma interação, com posição e justificativa - tabela RECOMENDACAO
@Entity
@Table(name = "RECOMENDACAO")
public class Recomendacao {
    @EmbeddedId
    private RecomendacaoId id = new RecomendacaoId();

    @MapsId("interacaoId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_INTERACAO")
    private Interacao interacao;

    @MapsId("servicoId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_SERVICO")
    private Servico servico;

    @Column(name = "NR_POSICAO", nullable = false)
    private Integer posicao;

    @Column(name = "DS_JUSTIFICATIVA", nullable = false, length = 600)
    private String justificativa;

    // Pontuação calculada pelo assistente na hora da resposta (não é gravada)
    @Transient
    private Integer pontuacao;

    public Recomendacao() {
    }

    public Recomendacao(Servico servico, String justificativa, Integer pontuacao) {
        this.servico = servico;
        this.justificativa = justificativa;
        this.pontuacao = pontuacao;
    }

    // Getters e setters
    public RecomendacaoId getId() { return id; }

    public Interacao getInteracao() { return interacao; }
    public void setInteracao(Interacao interacao) { this.interacao = interacao; }

    public Servico getServico() { return servico; }
    public void setServico(Servico servico) { this.servico = servico; }

    public Integer getPosicao() { return posicao; }
    public void setPosicao(Integer posicao) { this.posicao = posicao; }

    public String getJustificativa() { return justificativa; }
    public void setJustificativa(String justificativa) { this.justificativa = justificativa; }

    public Integer getPontuacao() { return pontuacao; }
    public void setPontuacao(Integer pontuacao) { this.pontuacao = pontuacao; }
}
