package br.com.fiap.cidadaofacil.model;

import jakarta.persistence.*;

// Entidade: tipo de documento exigido (não guarda documentos de pessoas) - tabela T_CF_DOCUMENTO
@Entity
@Table(name = "T_CF_DOCUMENTO")
public class Documento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DOCUMENTO")
    private Long id;

    @Column(name = "NM_DOCUMENTO", nullable = false, unique = true, length = 100)
    private String nome;

    @Column(name = "DS_DOCUMENTO", length = 300)
    private String descricao;

    public Documento() {
    }

    public Documento(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    // Getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
