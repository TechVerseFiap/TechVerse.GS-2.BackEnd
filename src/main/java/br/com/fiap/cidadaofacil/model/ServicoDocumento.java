package br.com.fiap.cidadaofacil.model;

import jakarta.persistence.*;

// Entidade associativa N:N entre serviço e documento - tabela T_CF_SERVICO_DOCUMENTO
@Entity
@Table(name = "T_CF_SERVICO_DOCUMENTO",
        uniqueConstraints = @UniqueConstraint(name = "UK_CF_SERV_DOC", columnNames = {"ID_SERVICO", "ID_DOCUMENTO"}))
public class ServicoDocumento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_SERVICO_DOCUMENTO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_SERVICO", nullable = false)
    private Servico servico;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_DOCUMENTO", nullable = false)
    private Documento documento;

    @Column(name = "DS_OBSERVACAO", length = 255)
    private String observacao;

    public ServicoDocumento() {
    }

    public ServicoDocumento(Servico servico, Documento documento, String observacao) {
        this.servico = servico;
        this.documento = documento;
        this.observacao = observacao;
    }

    // Getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Servico getServico() { return servico; }
    public void setServico(Servico servico) { this.servico = servico; }

    public Documento getDocumento() { return documento; }
    public void setDocumento(Documento documento) { this.documento = documento; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
