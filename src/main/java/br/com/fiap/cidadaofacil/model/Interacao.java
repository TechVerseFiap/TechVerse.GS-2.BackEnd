package br.com.fiap.cidadaofacil.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Entidade: pergunta ao assistente, sem guardar o texto do cidadão - tabela T_CF_INTERACAO
@Entity
@Table(name = "T_CF_INTERACAO")
public class Interacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_INTERACAO")
    private Long id;

    @Column(name = "DS_ASSUNTO", nullable = false, length = 60)
    private String assunto;

    @Column(name = "DT_INTERACAO", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_RESULTADO", nullable = false, length = 25)
    private ResultadoInteracao resultado;

    @OneToMany(mappedBy = "interacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Recomendacao> recomendacoes = new ArrayList<>();

    public Interacao() {
    }

    public Interacao(String assunto, ResultadoInteracao resultado) {
        this.assunto = assunto;
        this.resultado = resultado;
        this.dataHora = LocalDateTime.now();
    }

    public void adicionarRecomendacao(Recomendacao recomendacao) {
        recomendacao.setInteracao(this);
        recomendacoes.add(recomendacao);
    }

    // Getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAssunto() { return assunto; }
    public void setAssunto(String assunto) { this.assunto = assunto; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public ResultadoInteracao getResultado() { return resultado; }
    public void setResultado(ResultadoInteracao resultado) { this.resultado = resultado; }

    public List<Recomendacao> getRecomendacoes() { return recomendacoes; }
}
