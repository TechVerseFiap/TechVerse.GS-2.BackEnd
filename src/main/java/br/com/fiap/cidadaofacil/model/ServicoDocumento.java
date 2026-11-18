package br.com.fiap.cidadaofacil.model;

import jakarta.persistence.*;

// Entidade associativa N:N entre serviço e documento - tabela SERVICO_DOCUMENTO
@Entity
@Table(name = "SERVICO_DOCUMENTO")
public class ServicoDocumento {
    @EmbeddedId
    private ServicoDocumentoId id = new ServicoDocumentoId();

    @MapsId("servicoId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_SERVICO")
    private Servico servico;

    @MapsId("documentoId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_DOCUMENTO")
    private Documento documento;

    @Column(name = "FL_OBRIGATORIO", nullable = false, length = 1)
    private String obrigatorio = "S";

    @Column(name = "DS_OBSERVACAO", length = 400)
    private String observacao;

    public ServicoDocumento() {
    }

    public ServicoDocumento(Servico servico, Documento documento, boolean obrigatorio, String observacao) {
        this.id = new ServicoDocumentoId(servico.getId(), documento.getId());
        this.servico = servico;
        this.documento = documento;
        this.obrigatorio = obrigatorio ? "S" : "N";
        this.observacao = observacao;
    }

    public boolean isObrigatorio() {
        return "S".equals(obrigatorio);
    }

    public void setObrigatorio(boolean obrigatorio) {
        this.obrigatorio = obrigatorio ? "S" : "N";
    }

    // Getters e setters
    public ServicoDocumentoId getId() { return id; }

    public Servico getServico() { return servico; }
    public void setServico(Servico servico) { this.servico = servico; }

    public Documento getDocumento() { return documento; }
    public void setDocumento(Documento documento) { this.documento = documento; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
