package br.com.fiap.cidadaofacil.model;

import jakarta.persistence.*;

// Entidade: canal de atendimento de um serviço (URL ou local) - tabela CANAL
@Entity
@Table(name = "CANAL")
public class Canal {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqCanal")
    @SequenceGenerator(name = "sqCanal", sequenceName = "SQ_CANAL", allocationSize = 1)
    @Column(name = "ID_CANAL")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_SERVICO", nullable = false)
    private Servico servico;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_CANAL", nullable = false, length = 10)
    private TipoCanal tipo;

    @Column(name = "DS_ENDERECO", nullable = false, length = 500)
    private String endereco;

    public Canal() {
    }

    public Canal(TipoCanal tipo, String endereco) {
        this.tipo = tipo;
        this.endereco = endereco;
    }

    // Getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Servico getServico() { return servico; }
    public void setServico(Servico servico) { this.servico = servico; }

    public TipoCanal getTipo() { return tipo; }
    public void setTipo(TipoCanal tipo) { this.tipo = tipo; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
}
