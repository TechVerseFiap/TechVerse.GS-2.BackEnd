package br.com.fiap.cidadaofacil.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Entidade central: serviço público com órgão, fonte oficial e canais - tabela SERVICO
@Entity
@Table(name = "SERVICO")
public class Servico {
    public static final int DIAS_VALIDADE_VERIFICACAO = 180;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sqServico")
    @SequenceGenerator(name = "sqServico", sequenceName = "SQ_SERVICO", allocationSize = 1)
    @Column(name = "ID_SERVICO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CATEGORIA", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_ORGAO", nullable = false)
    private Orgao orgao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_FONTE", nullable = false)
    private Fonte fonte;

    @Column(name = "NM_SERVICO", nullable = false, length = 120)
    private String nome;

    @Column(name = "DS_SERVICO", nullable = false, length = 1000)
    private String descricao;

    @Column(name = "DS_PUBLICO", nullable = false, length = 300)
    private String publicoAlvo;

    @Column(name = "DS_PALAVRAS_CHAVE", length = 400)
    private String palavrasChave;

    @Enumerated(EnumType.STRING)
    @Column(name = "ST_SERVICO", nullable = false, length = 8)
    private StatusServico status = StatusServico.ATIVO;

    @Column(name = "FL_DEMO", nullable = false, length = 1)
    private String demo = "N";

    @OneToMany(mappedBy = "servico", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Requisito> requisitos = new ArrayList<>();

    @OneToMany(mappedBy = "servico", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    private List<Etapa> etapas = new ArrayList<>();

    @OneToMany(mappedBy = "servico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServicoDocumento> documentos = new ArrayList<>();

    @OneToMany(mappedBy = "servico", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("tipo ASC")
    private List<Canal> canais = new ArrayList<>();

    public Servico() {
    }

    // Regras do domínio: validade da fonte, palavras-chave, canais e inclusão de filhos
    public LocalDate getDataVerificacao() {
        return fonte == null ? null : fonte.getDataConsulta();
    }

    public String getUrlFonte() {
        return fonte == null ? null : fonte.getUrl();
    }

    public boolean precisaReverificacao() {
        LocalDate data = getDataVerificacao();
        if (data == null) {
            return true;
        }
        return ChronoUnit.DAYS.between(data, LocalDate.now()) > DIAS_VALIDADE_VERIFICACAO;
    }

    public boolean isAtivo() {
        return status == StatusServico.ATIVO;
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

    // Resume os canais cadastrados em DIGITAL, PRESENCIAL ou DIGITAL_E_PRESENCIAL
    public CanalAtendimento getCanal() {
        boolean digital = canais.stream().anyMatch(c -> c.getTipo() == TipoCanal.DIGITAL);
        boolean presencial = canais.stream().anyMatch(c -> c.getTipo() == TipoCanal.PRESENCIAL);
        if (digital && presencial) {
            return CanalAtendimento.DIGITAL_E_PRESENCIAL;
        }
        return presencial ? CanalAtendimento.PRESENCIAL : CanalAtendimento.DIGITAL;
    }

    // Mantém os canais coerentes com o resumo escolhido, preservando os endereços já cadastrados
    public void definirCanais(CanalAtendimento resumo, String enderecoDigital, String enderecoPresencial) {
        sincronizarCanal(TipoCanal.DIGITAL, resumo.incluiDigital(), enderecoDigital);
        sincronizarCanal(TipoCanal.PRESENCIAL, resumo.incluiPresencial(), enderecoPresencial);
    }

    private void sincronizarCanal(TipoCanal tipo, boolean deveExistir, String endereco) {
        if (!deveExistir) {
            canais.removeIf(c -> c.getTipo() == tipo);
        } else if (canais.stream().noneMatch(c -> c.getTipo() == tipo)) {
            adicionarCanal(new Canal(tipo, endereco));
        }
    }

    public void adicionarCanal(Canal canal) {
        canal.setServico(this);
        canais.add(canal);
    }

    public void adicionarRequisito(Requisito requisito) {
        requisito.setServico(this);
        requisitos.add(requisito);
    }

    public void adicionarEtapa(Etapa etapa) {
        etapa.setServico(this);
        etapas.add(etapa);
    }

    // Getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public Orgao getOrgao() { return orgao; }
    public void setOrgao(Orgao orgao) { this.orgao = orgao; }

    public Fonte getFonte() { return fonte; }
    public void setFonte(Fonte fonte) { this.fonte = fonte; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getPublicoAlvo() { return publicoAlvo; }
    public void setPublicoAlvo(String publicoAlvo) { this.publicoAlvo = publicoAlvo; }

    public String getPalavrasChave() { return palavrasChave; }
    public void setPalavrasChave(String palavrasChave) { this.palavrasChave = palavrasChave; }

    public StatusServico getStatus() { return status; }
    public void setStatus(StatusServico status) { this.status = status; }

    public String getDemo() { return demo; }
    public void setDemo(String demo) { this.demo = demo; }

    public List<Requisito> getRequisitos() { return requisitos; }
    public List<Etapa> getEtapas() { return etapas; }
    public List<ServicoDocumento> getDocumentos() { return documentos; }
    public List<Canal> getCanais() { return canais; }
}
