package br.com.fiap.cidadaofacil.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Entidade: pergunta ao assistente, sem guardar o texto do cidadão - tabela INTERACAO
@Entity
@Table(name = "INTERACAO")
public class Interacao {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqInteracao")
    @SequenceGenerator(name = "sqInteracao", sequenceName = "SQ_INTERACAO", allocationSize = 1)
    @Column(name = "ID_INTERACAO")
    private Long id;

    @Column(name = "DS_ASSUNTO", nullable = false, length = 100)
    private String assunto;

    @Column(name = "DH_INTERACAO", nullable = false)
    private LocalDateTime dataHora;

    @Convert(converter = ResultadoInteracaoConverter.class)
    @Column(name = "ST_RESULTADO", nullable = false, length = 20)
    private ResultadoInteracao resultado;

    @OneToMany(mappedBy = "interacao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("posicao ASC")
    private List<Recomendacao> recomendacoes = new ArrayList<>();

    public Interacao() {
    }

    public Interacao(String assunto, ResultadoInteracao resultado) {
        this.assunto = assunto;
        this.resultado = resultado;
        this.dataHora = LocalDateTime.now();
    }

    // A posição da recomendação segue a ordem de inclusão (1, 2, 3...)
    public void adicionarRecomendacao(Recomendacao recomendacao) {
        recomendacao.setInteracao(this);
        recomendacao.setPosicao(recomendacoes.size() + 1);
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
