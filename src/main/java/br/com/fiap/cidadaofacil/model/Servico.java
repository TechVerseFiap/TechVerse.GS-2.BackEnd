package br.com.fiap.cidadaofacil.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Entidade central: serviço público com fonte oficial - tabela T_CF_SERVICO
@Entity
@Table(name = "T_CF_SERVICO")
public class Servico {
    public static final int DIAS_VALIDADE_VERIFICACAO = 180;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_SERVICO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CATEGORIA", nullable = false)
    private Categoria categoria;

    @Column(name = "NM_SERVICO", nullable = false, length = 100)
    private String nome;

    @Column(name = "DS_SERVICO", nullable = false, length = 500)
    private String descricao;

    @Column(name = "DS_PUBLICO_ALVO", length = 255)
    private String publicoAlvo;

    @Column(name = "NM_ORGAO_RESPONSAVEL", nullable = false, length = 120)
    private String orgaoResponsavel;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_CANAL", nullable = false, length = 25)
    private CanalAtendimento canal;

    @Column(name = "DS_URL_FONTE", nullable = false, length = 255)
    private String urlFonte;

    @Column(name = "DT_VERIFICACAO", nullable = false)
    private LocalDate dataVerificacao;

    @Column(name = "DS_PALAVRAS_CHAVE", length = 400)
    private String palavrasChave;

    @OneToMany(mappedBy = "servico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Requisito> requisitos = new ArrayList<>();

    @OneToMany(mappedBy = "servico", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    private List<Etapa> etapas = new ArrayList<>();

    @OneToMany(mappedBy = "servico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServicoDocumento> documentos = new ArrayList<>();

    public Servico() {
    }

    // Regras do domínio: validade da fonte, palavras-chave e inclusão de filhos
    public boolean precisaReverificacao() {
        if (dataVerificacao == null) {
            return true;
        }
        return ChronoUnit.DAYS.between(dataVerificacao, LocalDate.now()) > DIAS_VALIDADE_VERIFICACAO;
    }

    public List<String> listaPalavrasChave() {
        if (palavrasChave == null || palavrasChave.isBlank()) {
            return List.of();
        }
        return Arrays.stream(palavrasChave.split(","))
                .map(String::trim)
                .filter(p -> !p.isEmpty())
                .toList();
    }

    public void adicionarRequisito(Requisito requisito) {
        requisito.setServico(this);
        requisitos.add(requisito);
    }

    public void adicionarEtapa(Etapa etapa) {
        etapa.setServico(this);
        etapas.add(etapa);
    }

    public void adicionarDocumento(Documento documento, String observacao) {
        documentos.add(new ServicoDocumento(this, documento, observacao));
    }

    // Getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getPublicoAlvo() { return publicoAlvo; }
    public void setPublicoAlvo(String publicoAlvo) { this.publicoAlvo = publicoAlvo; }

    public String getOrgaoResponsavel() { return orgaoResponsavel; }
    public void setOrgaoResponsavel(String orgaoResponsavel) { this.orgaoResponsavel = orgaoResponsavel; }

    public CanalAtendimento getCanal() { return canal; }
    public void setCanal(CanalAtendimento canal) { this.canal = canal; }

    public String getUrlFonte() { return urlFonte; }
    public void setUrlFonte(String urlFonte) { this.urlFonte = urlFonte; }

    public LocalDate getDataVerificacao() { return dataVerificacao; }
    public void setDataVerificacao(LocalDate dataVerificacao) { this.dataVerificacao = dataVerificacao; }

    public String getPalavrasChave() { return palavrasChave; }
    public void setPalavrasChave(String palavrasChave) { this.palavrasChave = palavrasChave; }

    public List<Requisito> getRequisitos() { return requisitos; }
    public List<Etapa> getEtapas() { return etapas; }
    public List<ServicoDocumento> getDocumentos() { return documentos; }
}
