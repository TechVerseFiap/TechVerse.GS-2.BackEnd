package br.com.fiap.cidadaofacil.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

// Entidade: agrupa os serviços (Trabalho, Educação...) - tabela T_CF_CATEGORIA
@Entity
@Table(name = "T_CF_CATEGORIA")
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CATEGORIA")
    private Long id;

    @Column(name = "NM_CATEGORIA", nullable = false, unique = true, length = 60)
    private String nome;

    @Column(name = "DS_CATEGORIA", length = 255)
    private String descricao;

    @OneToMany(mappedBy = "categoria")
    private List<Servico> servicos = new ArrayList<>();

    public Categoria() {
    }

    public Categoria(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public boolean possuiServicos() {
        return servicos != null && !servicos.isEmpty();
    }

    // Getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public List<Servico> getServicos() { return servicos; }
}
