-- Gerado a partir dos scripts da disciplina de Database (Tech Verse).
-- Não edite aqui: altere scripts/01_DDL.sql e 02_DML.sql e gere de novo.
-- Cidadão Fácil | Tech Verse | FIAP Global Solution 2026
-- Oracle Database 12c ou superior. Executar em esquema vazio de projeto.
-- Versão 2 (integração com a API Java): sequências para a geração de IDs pela
-- aplicação, palavras-chave do serviço, resultado intermediário da interação e
-- tabelas do panorama do município (dados públicos da Base dos Dados).
-- A carga (02_DML.sql) informa IDs explicitamente abaixo de 1000; as sequências
-- começam em 1000 para que a API nunca colida com os IDs da carga.
-- Não remove tabelas existentes. Executar apenas uma vez.

CREATE TABLE CATEGORIA (
    id_categoria NUMBER(10) NOT NULL,
    nm_categoria VARCHAR(80) NOT NULL,
    ds_categoria VARCHAR(300) NOT NULL,
    CONSTRAINT pk_categoria PRIMARY KEY (id_categoria),
    CONSTRAINT uq_categoria_1 UNIQUE (nm_categoria),
    CONSTRAINT ck_categoria_categoria CHECK (id_categoria > 0)
);

CREATE TABLE ORGAO (
    id_orgao NUMBER(10) NOT NULL,
    nm_orgao VARCHAR(150) NOT NULL,
    sg_orgao VARCHAR(20) NOT NULL,
    tp_esfera VARCHAR(10) NOT NULL,
    CONSTRAINT pk_orgao PRIMARY KEY (id_orgao),
    CONSTRAINT uq_orgao_1 UNIQUE (sg_orgao),
    CONSTRAINT ck_org_esfera CHECK (tp_esfera IN ('FEDERAL','ESTADUAL','MUNICIPAL')),
    CONSTRAINT ck_orgao_orgao CHECK (id_orgao > 0)
);

CREATE TABLE FONTE (
    id_fonte NUMBER(10) NOT NULL,
    nm_titulo VARCHAR(150) NOT NULL,
    ds_url VARCHAR(500) NOT NULL,
    tp_fonte VARCHAR(8) NOT NULL,
    dt_consulta DATE,
    CONSTRAINT pk_fonte PRIMARY KEY (id_fonte),
    CONSTRAINT uq_fonte_1 UNIQUE (ds_url),
    CONSTRAINT ck_fon_tipo CHECK (tp_fonte IN ('OFICIAL','SIMULADA')),
    CONSTRAINT ck_fon_data CHECK (tp_fonte = 'SIMULADA' OR dt_consulta IS NOT NULL),
    CONSTRAINT ck_fonte_fonte CHECK (id_fonte > 0)
);

CREATE TABLE SERVICO (
    id_servico NUMBER(10) NOT NULL,
    id_categoria NUMBER(10) NOT NULL,
    id_orgao NUMBER(10) NOT NULL,
    id_fonte NUMBER(10) NOT NULL,
    nm_servico VARCHAR(120) NOT NULL,
    ds_servico VARCHAR(1000) NOT NULL,
    ds_publico VARCHAR(300) NOT NULL,
    ds_palavras_chave VARCHAR(400),
    st_servico VARCHAR(8) NOT NULL,
    fl_demo CHAR(1) NOT NULL,
    CONSTRAINT pk_servico PRIMARY KEY (id_servico),
    CONSTRAINT uq_servico_1 UNIQUE (id_orgao, nm_servico),
    CONSTRAINT ck_ser_status CHECK (st_servico IN ('ATIVO','INATIVO')),
    CONSTRAINT ck_ser_demo CHECK (fl_demo IN ('S','N')),
    CONSTRAINT ck_servico_servico CHECK (id_servico > 0),
    CONSTRAINT ck_servico_categoria CHECK (id_categoria > 0),
    CONSTRAINT ck_servico_orgao CHECK (id_orgao > 0),
    CONSTRAINT ck_servico_fonte CHECK (id_fonte > 0)
);

CREATE TABLE DOCUMENTO (
    id_documento NUMBER(10) NOT NULL,
    nm_documento VARCHAR(100) NOT NULL,
    ds_finalidade VARCHAR(400) NOT NULL,
    CONSTRAINT pk_documento PRIMARY KEY (id_documento),
    CONSTRAINT uq_documento_1 UNIQUE (nm_documento),
    CONSTRAINT ck_documento_documento CHECK (id_documento > 0)
);

CREATE TABLE REQUISITO (
    id_requisito NUMBER(10) NOT NULL,
    id_servico NUMBER(10) NOT NULL,
    ds_requisito VARCHAR(600) NOT NULL,
    CONSTRAINT pk_requisito PRIMARY KEY (id_requisito),
    CONSTRAINT ck_requisito_requisito CHECK (id_requisito > 0),
    CONSTRAINT ck_requisito_servico CHECK (id_servico > 0)
);

CREATE TABLE SERVICO_DOCUMENTO (
    id_servico NUMBER(10) NOT NULL,
    id_documento NUMBER(10) NOT NULL,
    fl_obrigatorio CHAR(1) NOT NULL,
    ds_observacao VARCHAR(400),
    CONSTRAINT pk_servico_documento PRIMARY KEY (id_servico, id_documento),
    CONSTRAINT ck_sd_obrig CHECK (fl_obrigatorio IN ('S','N')),
    CONSTRAINT ck_servico_do_servico CHECK (id_servico > 0),
    CONSTRAINT ck_servico_do_documento CHECK (id_documento > 0)
);

CREATE TABLE ETAPA (
    id_etapa NUMBER(10) NOT NULL,
    id_servico NUMBER(10) NOT NULL,
    nr_ordem NUMBER(3) NOT NULL,
    ds_etapa VARCHAR(600) NOT NULL,
    CONSTRAINT pk_etapa PRIMARY KEY (id_etapa),
    CONSTRAINT uq_etapa_1 UNIQUE (id_servico, nr_ordem),
    CONSTRAINT ck_eta_ordem CHECK (nr_ordem > 0),
    CONSTRAINT ck_etapa_etapa CHECK (id_etapa > 0),
    CONSTRAINT ck_etapa_servico CHECK (id_servico > 0)
);

CREATE TABLE CANAL (
    id_canal NUMBER(10) NOT NULL,
    id_servico NUMBER(10) NOT NULL,
    tp_canal VARCHAR(10) NOT NULL,
    ds_endereco VARCHAR(500) NOT NULL,
    CONSTRAINT pk_canal PRIMARY KEY (id_canal),
    CONSTRAINT uq_canal_1 UNIQUE (id_servico, tp_canal, ds_endereco),
    CONSTRAINT ck_can_tipo CHECK (tp_canal IN ('DIGITAL','PRESENCIAL')),
    CONSTRAINT ck_canal_canal CHECK (id_canal > 0),
    CONSTRAINT ck_canal_servico CHECK (id_servico > 0)
);

CREATE TABLE INTERACAO (
    id_interacao NUMBER(10) NOT NULL,
    ds_assunto VARCHAR(100) NOT NULL,
    dh_interacao TIMESTAMP NOT NULL,
    st_resultado VARCHAR(20) NOT NULL,
    CONSTRAINT pk_interacao PRIMARY KEY (id_interacao),
    CONSTRAINT ck_int_resultado CHECK (st_resultado IN ('COM_RESULTADO','PEDIU_ESCLARECIMENTO','SEM_RESULTADO')),
    CONSTRAINT ck_interacao_interacao CHECK (id_interacao > 0)
);

CREATE TABLE RECOMENDACAO (
    id_interacao NUMBER(10) NOT NULL,
    id_servico NUMBER(10) NOT NULL,
    nr_posicao NUMBER(3) NOT NULL,
    ds_justificativa VARCHAR(600) NOT NULL,
    CONSTRAINT pk_recomendacao PRIMARY KEY (id_interacao, id_servico),
    CONSTRAINT uq_recomendacao_1 UNIQUE (id_interacao, nr_posicao),
    CONSTRAINT ck_rec_posicao CHECK (nr_posicao > 0),
    CONSTRAINT ck_recomendac_interacao CHECK (id_interacao > 0),
    CONSTRAINT ck_recomendac_servico CHECK (id_servico > 0)
);

CREATE TABLE MUNICIPIO (
    id_municipio NUMBER(7) NOT NULL,
    nm_municipio VARCHAR(100) NOT NULL,
    sg_uf CHAR(2) NOT NULL,
    CONSTRAINT pk_municipio PRIMARY KEY (id_municipio),
    CONSTRAINT uq_municipio_1 UNIQUE (sg_uf, nm_municipio),
    CONSTRAINT ck_mun_codigo CHECK (id_municipio BETWEEN 1100000 AND 5399999),
    CONSTRAINT ck_mun_uf CHECK (sg_uf = UPPER(sg_uf))
);

CREATE TABLE INDICADOR (
    id_indicador NUMBER(10) NOT NULL,
    id_fonte NUMBER(10) NOT NULL,
    cd_indicador VARCHAR(40) NOT NULL,
    nm_indicador VARCHAR(120) NOT NULL,
    ds_indicador VARCHAR(400) NOT NULL,
    ds_unidade VARCHAR(30) NOT NULL,
    tp_periodicidade VARCHAR(6) NOT NULL,
    CONSTRAINT pk_indicador PRIMARY KEY (id_indicador),
    CONSTRAINT ck_ind_periodicidade CHECK (tp_periodicidade IN ('ANUAL','MENSAL')),
    CONSTRAINT uq_indicador_1 UNIQUE (cd_indicador),
    CONSTRAINT ck_indicador_indicador CHECK (id_indicador > 0),
    CONSTRAINT ck_indicador_fonte CHECK (id_fonte > 0)
);

CREATE TABLE INDICADOR_MUNICIPIO (
    id_municipio NUMBER(7) NOT NULL,
    id_indicador NUMBER(10) NOT NULL,
    dt_referencia DATE NOT NULL,
    vl_indicador NUMBER(15,2) NOT NULL,
    CONSTRAINT pk_indicador_municipio PRIMARY KEY (id_municipio, id_indicador, dt_referencia),
    CONSTRAINT ck_indicador_mu_indicador CHECK (id_indicador > 0)
);

ALTER TABLE SERVICO ADD CONSTRAINT fk_servico_categoria FOREIGN KEY (id_categoria) REFERENCES CATEGORIA (id_categoria);
ALTER TABLE SERVICO ADD CONSTRAINT fk_servico_orgao FOREIGN KEY (id_orgao) REFERENCES ORGAO (id_orgao);
ALTER TABLE SERVICO ADD CONSTRAINT fk_servico_fonte FOREIGN KEY (id_fonte) REFERENCES FONTE (id_fonte);
ALTER TABLE REQUISITO ADD CONSTRAINT fk_requisito_servico FOREIGN KEY (id_servico) REFERENCES SERVICO (id_servico);
ALTER TABLE SERVICO_DOCUMENTO ADD CONSTRAINT fk_servico_do_servico FOREIGN KEY (id_servico) REFERENCES SERVICO (id_servico);
ALTER TABLE SERVICO_DOCUMENTO ADD CONSTRAINT fk_servico_do_documento FOREIGN KEY (id_documento) REFERENCES DOCUMENTO (id_documento);
ALTER TABLE ETAPA ADD CONSTRAINT fk_etapa_servico FOREIGN KEY (id_servico) REFERENCES SERVICO (id_servico);
ALTER TABLE CANAL ADD CONSTRAINT fk_canal_servico FOREIGN KEY (id_servico) REFERENCES SERVICO (id_servico);
ALTER TABLE RECOMENDACAO ADD CONSTRAINT fk_recomendac_interacao FOREIGN KEY (id_interacao) REFERENCES INTERACAO (id_interacao);
ALTER TABLE RECOMENDACAO ADD CONSTRAINT fk_recomendac_servico FOREIGN KEY (id_servico) REFERENCES SERVICO (id_servico);
ALTER TABLE INDICADOR ADD CONSTRAINT fk_indicador_fonte FOREIGN KEY (id_fonte) REFERENCES FONTE (id_fonte);
ALTER TABLE INDICADOR_MUNICIPIO ADD CONSTRAINT fk_indicador_mu_municipio FOREIGN KEY (id_municipio) REFERENCES MUNICIPIO (id_municipio);
ALTER TABLE INDICADOR_MUNICIPIO ADD CONSTRAINT fk_indicador_mu_indicador FOREIGN KEY (id_indicador) REFERENCES INDICADOR (id_indicador);

-- Índices nas FKs não cobertas pelo prefixo de PK/UNIQUE.
CREATE INDEX ix_servico_categoria ON SERVICO (id_categoria);
CREATE INDEX ix_servico_fonte ON SERVICO (id_fonte);
CREATE INDEX ix_requisito_servico ON REQUISITO (id_servico);
CREATE INDEX ix_servico_do_documento ON SERVICO_DOCUMENTO (id_documento);
CREATE INDEX ix_recomendac_servico ON RECOMENDACAO (id_servico);
CREATE INDEX ix_indicador_fonte ON INDICADOR (id_fonte);
CREATE INDEX ix_indicador_mu_indicador ON INDICADOR_MUNICIPIO (id_indicador);

-- Sequências usadas pela API para gerar IDs com segurança em acessos concorrentes.
-- MUNICIPIO não tem sequência: a chave é o código oficial do IBGE.
CREATE SEQUENCE sq_categoria START WITH 1000 INCREMENT BY 1;
CREATE SEQUENCE sq_orgao START WITH 1000 INCREMENT BY 1;
CREATE SEQUENCE sq_fonte START WITH 1000 INCREMENT BY 1;
CREATE SEQUENCE sq_servico START WITH 1000 INCREMENT BY 1;
CREATE SEQUENCE sq_documento START WITH 1000 INCREMENT BY 1;
CREATE SEQUENCE sq_requisito START WITH 1000 INCREMENT BY 1;
CREATE SEQUENCE sq_etapa START WITH 1000 INCREMENT BY 1;
CREATE SEQUENCE sq_canal START WITH 1000 INCREMENT BY 1;
CREATE SEQUENCE sq_interacao START WITH 1000 INCREMENT BY 1;
CREATE SEQUENCE sq_indicador START WITH 1000 INCREMENT BY 1;

COMMENT ON TABLE CATEGORIA IS 'Classifica os serviços por assunto. As cinco categorias detalham os eixos Trabalho e Educação.';
COMMENT ON COLUMN CATEGORIA.id_categoria IS 'Identificador numérico positivo, atribuído pela aplicação ou pela carga.';
COMMENT ON COLUMN CATEGORIA.nm_categoria IS 'Nome único da categoria.';
COMMENT ON COLUMN CATEGORIA.ds_categoria IS 'Descrição do tema.';
COMMENT ON TABLE ORGAO IS 'Identifica o órgão responsável pelo atendimento, sem repetir seu nome em cada serviço.';
COMMENT ON COLUMN ORGAO.id_orgao IS 'Identificador numérico positivo, atribuído pela aplicação ou pela carga.';
COMMENT ON COLUMN ORGAO.nm_orgao IS 'Nome do órgão.';
COMMENT ON COLUMN ORGAO.sg_orgao IS 'Sigla única.';
COMMENT ON COLUMN ORGAO.tp_esfera IS 'FEDERAL, ESTADUAL ou MUNICIPAL.';
COMMENT ON TABLE FONTE IS 'Registra a referência documental usada no catálogo. Uma fonte pode fundamentar vários serviços; cada serviço tem uma fonte principal.';
COMMENT ON COLUMN FONTE.id_fonte IS 'Identificador numérico positivo, atribuído pela aplicação ou pela carga.';
COMMENT ON COLUMN FONTE.nm_titulo IS 'Título da referência.';
COMMENT ON COLUMN FONTE.ds_url IS 'URL única de referência.';
COMMENT ON COLUMN FONTE.tp_fonte IS 'OFICIAL ou SIMULADA.';
COMMENT ON COLUMN FONTE.dt_consulta IS 'Data real de consulta; nula nas fontes simuladas.';
COMMENT ON TABLE SERVICO IS 'É o núcleo do catálogo: orientação sobre uma oportunidade de trabalho ou educação, com categoria, responsável e fonte principal.';
COMMENT ON COLUMN SERVICO.id_servico IS 'Identificador numérico positivo, atribuído pela aplicação ou pela carga.';
COMMENT ON COLUMN SERVICO.id_categoria IS 'Identificador numérico positivo, atribuído pela aplicação ou pela carga.';
COMMENT ON COLUMN SERVICO.id_orgao IS 'Identificador numérico positivo, atribuído pela aplicação ou pela carga.';
COMMENT ON COLUMN SERVICO.id_fonte IS 'Identificador numérico positivo, atribuído pela aplicação ou pela carga.';
COMMENT ON COLUMN SERVICO.nm_servico IS 'Nome do serviço.';
COMMENT ON COLUMN SERVICO.ds_servico IS 'Resumo em linguagem simples.';
COMMENT ON COLUMN SERVICO.ds_publico IS 'Público a que se destina.';
COMMENT ON COLUMN SERVICO.ds_palavras_chave IS 'Termos separados por vírgula que o assistente compara com a necessidade descrita. Não guarda texto do cidadão.';
COMMENT ON COLUMN SERVICO.st_servico IS 'ATIVO ou INATIVO.';
COMMENT ON COLUMN SERVICO.fl_demo IS 'S para demonstração; N para catálogo real.';
COMMENT ON TABLE DOCUMENTO IS 'Define tipos de documentos, sem guardar arquivos ou dados pessoais dos cidadãos.';
COMMENT ON COLUMN DOCUMENTO.id_documento IS 'Identificador numérico positivo, atribuído pela aplicação ou pela carga.';
COMMENT ON COLUMN DOCUMENTO.nm_documento IS 'Nome único do tipo de documento.';
COMMENT ON COLUMN DOCUMENTO.ds_finalidade IS 'Finalidade informativa do documento.';
COMMENT ON TABLE REQUISITO IS 'Armazena cada critério do serviço em uma linha própria. Não calcula nem comprova elegibilidade.';
COMMENT ON COLUMN REQUISITO.id_requisito IS 'Identificador numérico positivo, atribuído pela aplicação ou pela carga.';
COMMENT ON COLUMN REQUISITO.id_servico IS 'Identificador numérico positivo, atribuído pela aplicação ou pela carga.';
COMMENT ON COLUMN REQUISITO.ds_requisito IS 'Descrição do critério; na carga, conteúdo fictício.';
COMMENT ON TABLE SERVICO_DOCUMENTO IS 'Resolve a relação N:N entre serviços e documentos. A obrigatoriedade e a observação dependem do par completo.';
COMMENT ON COLUMN SERVICO_DOCUMENTO.id_servico IS 'Identificador numérico positivo, atribuído pela aplicação ou pela carga.';
COMMENT ON COLUMN SERVICO_DOCUMENTO.id_documento IS 'Identificador numérico positivo, atribuído pela aplicação ou pela carga.';
COMMENT ON COLUMN SERVICO_DOCUMENTO.fl_obrigatorio IS 'S: obrigatório no cenário; N: condicional.';
COMMENT ON COLUMN SERVICO_DOCUMENTO.ds_observacao IS 'Condição de apresentação ou observação.';
COMMENT ON TABLE ETAPA IS 'Organiza o passo a passo do atendimento. A ordem é única dentro de cada serviço.';
COMMENT ON COLUMN ETAPA.id_etapa IS 'Identificador numérico positivo, atribuído pela aplicação ou pela carga.';
COMMENT ON COLUMN ETAPA.id_servico IS 'Identificador numérico positivo, atribuído pela aplicação ou pela carga.';
COMMENT ON COLUMN ETAPA.nr_ordem IS 'Posição positiva no fluxo.';
COMMENT ON COLUMN ETAPA.ds_etapa IS 'Orientação para a etapa.';
COMMENT ON TABLE CANAL IS 'Permite vários canais por serviço. O endereço é uma URL ou uma instrução de local de atendimento.';
COMMENT ON COLUMN CANAL.id_canal IS 'Identificador numérico positivo, atribuído pela aplicação ou pela carga.';
COMMENT ON COLUMN CANAL.id_servico IS 'Identificador numérico positivo, atribuído pela aplicação ou pela carga.';
COMMENT ON COLUMN CANAL.tp_canal IS 'DIGITAL ou PRESENCIAL.';
COMMENT ON COLUMN CANAL.ds_endereco IS 'Destino ou local do canal.';
COMMENT ON TABLE INTERACAO IS 'Registra apenas assunto padronizado, horário e resultado de uma interação demonstrativa. Não guarda pergunta livre, nome, IP ou identificador de cidadão.';
COMMENT ON COLUMN INTERACAO.id_interacao IS 'Identificador numérico positivo, atribuído pela aplicação ou pela carga.';
COMMENT ON COLUMN INTERACAO.ds_assunto IS 'Rótulo padronizado da necessidade.';
COMMENT ON COLUMN INTERACAO.dh_interacao IS 'Data e hora do cenário.';
COMMENT ON COLUMN INTERACAO.st_resultado IS 'COM_RESULTADO, PEDIU_ESCLARECIMENTO (só a categoria foi reconhecida) ou SEM_RESULTADO.';
COMMENT ON TABLE RECOMENDACAO IS 'Associa serviços a interações com justificativa e posição. Não equivale a concessão de benefício nem a análise oficial de direito.';
COMMENT ON COLUMN RECOMENDACAO.id_interacao IS 'Identificador numérico positivo, atribuído pela aplicação ou pela carga.';
COMMENT ON COLUMN RECOMENDACAO.id_servico IS 'Identificador numérico positivo, atribuído pela aplicação ou pela carga.';
COMMENT ON COLUMN RECOMENDACAO.nr_posicao IS 'Posição positiva e única na interação.';
COMMENT ON COLUMN RECOMENDACAO.ds_justificativa IS 'Motivo da sugestão, sem afirmar elegibilidade.';
COMMENT ON TABLE MUNICIPIO IS 'Municípios cobertos pelo panorama territorial. A chave é o código IBGE de 7 dígitos, como na Base dos Dados.';
COMMENT ON COLUMN MUNICIPIO.id_municipio IS 'Código IBGE do município (7 dígitos).';
COMMENT ON COLUMN MUNICIPIO.nm_municipio IS 'Nome oficial do município.';
COMMENT ON COLUMN MUNICIPIO.sg_uf IS 'Sigla da unidade da federação.';
COMMENT ON TABLE INDICADOR IS 'Catálogo de indicadores públicos exibidos no panorama, cada um com a sua fonte.';
COMMENT ON COLUMN INDICADOR.id_indicador IS 'Identificador numérico positivo, atribuído pela aplicação ou pela carga.';
COMMENT ON COLUMN INDICADOR.id_fonte IS 'Fonte oficial do indicador (tabela da Base dos Dados).';
COMMENT ON COLUMN INDICADOR.cd_indicador IS 'Código estável usado pela API.';
COMMENT ON COLUMN INDICADOR.nm_indicador IS 'Nome exibido ao cidadão.';
COMMENT ON COLUMN INDICADOR.ds_indicador IS 'Como o valor foi calculado, em linguagem simples.';
COMMENT ON COLUMN INDICADOR.ds_unidade IS 'Unidade do valor (pessoas, famílias, vagas, unidades, reais).';
COMMENT ON COLUMN INDICADOR.tp_periodicidade IS 'ANUAL ou MENSAL: define como o período de referência é exibido.';
COMMENT ON TABLE INDICADOR_MUNICIPIO IS 'Valor de um indicador em um município e período. Guarda apenas números agregados, sem dados pessoais.';
COMMENT ON COLUMN INDICADOR_MUNICIPIO.id_municipio IS 'Município do valor.';
COMMENT ON COLUMN INDICADOR_MUNICIPIO.id_indicador IS 'Indicador do valor.';
COMMENT ON COLUMN INDICADOR_MUNICIPIO.dt_referencia IS 'Início do período de referência (ex.: 01/01/2025 para o ano de 2025).';
COMMENT ON COLUMN INDICADOR_MUNICIPIO.vl_indicador IS 'Valor agregado do indicador; o saldo de empregos pode ser negativo.';
