# Cidadão Fácil — API (Domain Driven Design - Java)

> Global Solution FIAP 2026.2 · CitizenTech · Engenharia de Software · 2º ano

## Ficha da CitizenTech

| Campo | Definição |
|---|---|
| Nome da CitizenTech | Cidadão Fácil |
| Cidadão beneficiado | Pessoas que procuram emprego, qualificação e serviços públicos relacionados, especialmente quem tem pouca familiaridade com portais do governo |
| Problema, em uma frase | Cidadãos têm dificuldade para encontrar, entender e acessar informações sobre serviços públicos, documentos, direitos e benefícios |
| Como funciona | O cidadão descreve o que precisa; o assistente identifica o assunto, consulta um catálogo com fonte oficial e mostra requisitos, documentos, etapas e o link oficial |
| O que cada parte faz | **Java (este repositório):** API REST com catálogo, assistente e panorama do município · **Database:** modelo Oracle (14 tabelas) usado pela API · Front-end: telas de busca, detalhe e painel · Python: análise de demanda / algoritmo · Packet Tracer: rede do ponto de atendimento |
| Links | API: https://cidadao-facil-api-6hqm.onrender.com · Site: https://cidadao-facil-web.onrender.com (também em https://cidadao-facil-techverse.vercel.app) · Front-end: https://github.com/TechVerseFiap/TechVerse.GS-2.FrontEnd |
| Indicador de impacto | Taxa de buscas que terminam com uma orientação encontrada (`GET /api/assistente/indicadores`) |
| ODS conectados | 4, 8, 10 e 16 |

---

## O que a API faz

1. **Catálogo de serviços públicos** (CRUD completo): categorias, serviços, requisitos, etapas e documentos.
2. **Assistente:** recebe uma frase em linguagem natural ("estou desempregado e quero um curso gratuito") e devolve os serviços mais relevantes, com justificativa e link oficial.
3. **Transparência:** todo serviço tem URL oficial (https obrigatório) e data de verificação; informações com mais de 180 dias são sinalizadas (`precisaReverificacao`).
4. **Privacidade:** o texto digitado pelo cidadão **não é salvo**, apenas o assunto identificado e o resultado (base do indicador de impacto).
5. **Honestidade:** quando não encontra nada, o assistente admite e sugere categorias; nunca afirma que a pessoa "tem direito".
6. **Panorama do município:** indicadores públicos reais dos 645 municípios de SP (população, saldo de empregos formais, Bolsa Família e estabelecimentos do SUS). A fonte é a [Base dos Dados](https://basedosdados.org): a API roda SQL no BigQuery da Base dos Dados e grava o período mais recente no Oracle (veja [Integração com a Base dos Dados](#integração-com-a-base-dos-dados)). Só números agregados, sem dados pessoais.

## Banco de dados: o mesmo modelo da disciplina de Database

A API usa exatamente o modelo Oracle entregue em Database Design (14 tabelas, 13 chaves estrangeiras, 10 sequências).
As tabelas são criadas e carregadas pelo **Flyway** a partir dos mesmos scripts:

| Migração | Origem | Conteúdo |
|---|---|---|
| `db/migration/oracle/V1__estrutura.sql` | `01_DDL.sql` | Tabelas, restrições, índices, sequências e comentários |
| `db/migration/oracle/V2__carga.sql` | `02_DML.sql` | Catálogo real (6 serviços gov.br) e panorama (645 municípios x 5 indicadores), em blocos PL/SQL para carregar rápido |
| `db/migration/h2/*` | idem | Mesmo conteúdo adaptado ao H2, para o perfil `dev` |

```
CATEGORIA ─┐        ORGAO ─┐        FONTE ─┬───────────── INDICADOR ── INDICADOR_MUNICIPIO ── MUNICIPIO
           └──── SERVICO ──┘───────────────┘
                  ├── REQUISITO      ├── ETAPA       ├── CANAL
                  ├── SERVICO_DOCUMENTO ── DOCUMENTO
                  └── RECOMENDACAO ── INTERACAO
```

- IDs gerados pelas sequências `SQ_*` (começam em 1000; a carga usa IDs abaixo de 1000).
- O canal exibido pela API (`DIGITAL`, `PRESENCIAL`, `DIGITAL_E_PRESENCIAL`) é calculado a partir dos registros de `CANAL`.
- A data de verificação de um serviço é a `DT_CONSULTA` da sua `FONTE`.
- O resultado da interação é gravado como `COM_RESULTADO`, `PEDIU_ESCLARECIMENTO` ou `SEM_RESULTADO` (conversor JPA).

## Arquitetura em camadas (Model, Service, Controller + Repository)

```
controller  →  service  →  repository  →  Oracle
   (HTTP)       (regras)    (acesso a dados)
                    └────→  BaseDosDadosRepository  →  BigQuery (Base dos Dados)
     ↑
 exception (GlobalExceptionHandler: transforma erros em JSON padronizado)
```

| Pacote | Responsabilidade |
|---|---|
| `model` | Entidades do domínio mapeadas para as 14 tabelas do modelo de Database (JPA) |
| `repository` | Acesso a dados (papel do DAO): interfaces Spring Data JPA para o Oracle (findBy..., existsBy...) e `BaseDosDadosRepository`, que roda SQL no BigQuery |
| `service` | Regras de negócio (papel do BO): nome duplicado, fonte https, ordem de etapas, lógica do assistente, sincronização com a Base dos Dados |
| `controller` | Endpoints REST (GET, POST, PUT, DELETE) |
| `dto` | O que entra (Request, com validação) e o que sai (Response) da API |
| `exception` | Exceções do domínio + tratamento global de erros |
| `config` | CORS e Swagger |
| `resources/db/migration` | Scripts do Flyway (estrutura e carga) |

### Regras de negócio implementadas (services)

| Regra | Onde | HTTP |
|---|---|---|
| Nome de categoria e de documento é único | CategoriaService, DocumentoService | 409 |
| Sigla de órgão é única; órgão com serviços não pode ser excluído | OrgaoService | 409 |
| Serviço precisa de órgão cadastrado | ServicoService | 404 / 422 |
| Fonte reaproveitada pela URL (atualiza a data de consulta) | ServicoService | — |
| Assistente recomenda só serviços ativos | AssistenteService | — |
| Busca de município com pelo menos 2 letras | PanoramaService | 422 |
| Não excluir categoria que tem serviços | CategoriaService | 409 |
| URL da fonte oficial deve começar com `https://` | ServicoService | 422 |
| Data de verificação não pode ser futura | ServicoDTO (`@PastOrPresent`) | 400 |
| Ordem da etapa não se repete no mesmo serviço | EtapaService | 409 |
| Documento vinculado a serviço não pode ser excluído | DocumentoService | 409 |
| Serviço já recomendado pelo assistente não pode ser excluído (histórico); pode ser marcado como inativo | ServicoService | 409 |
| Registro inexistente | todos os services | 404 |
| Sincronização com a Base dos Dados sem credencial ou com o BigQuery fora do ar | SincronizacaoService | 503 |
| Uma sincronização por vez | SincronizacaoService | 409 |

## Endpoints

Documentação interativa: **`/swagger-ui.html`** (a raiz `/` redireciona para lá).

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/categorias` | Lista categorias |
| GET | `/api/categorias/{id}` | Busca categoria |
| GET | `/api/categorias/{id}/servicos` | Serviços da categoria |
| POST | `/api/categorias` | Cria categoria |
| PUT | `/api/categorias/{id}` | Atualiza categoria |
| DELETE | `/api/categorias/{id}` | Exclui categoria |
| GET | `/api/servicos?categoria={id}&busca={texto}` | Lista/filtra serviços |
| GET | `/api/servicos/{id}` | Detalhe completo (requisitos, documentos, etapas, fonte) |
| POST | `/api/servicos` | Cria serviço |
| PUT | `/api/servicos/{id}` | Atualiza serviço |
| DELETE | `/api/servicos/{id}` | Exclui serviço |
| GET | `/api/servicos/{id}/requisitos` | Requisitos do serviço |
| POST | `/api/servicos/{id}/requisitos` | Adiciona requisito |
| GET / PUT / DELETE | `/api/requisitos/{id}` | Consulta, altera, exclui requisito |
| GET | `/api/servicos/{id}/etapas` | Etapas do serviço |
| POST | `/api/servicos/{id}/etapas` | Adiciona etapa |
| GET / PUT / DELETE | `/api/etapas/{id}` | Consulta, altera, exclui etapa |
| GET | `/api/servicos/{id}/documentos` | Documentos do serviço |
| POST | `/api/servicos/{id}/documentos` | Vincula documento |
| PUT | `/api/servicos/{id}/documentos/{documentoId}` | Altera observação e obrigatoriedade do vínculo |
| DELETE | `/api/servicos/{id}/documentos/{documentoId}` | Desvincula documento |
| GET | `/api/orgaos` | Lista órgãos |
| GET | `/api/orgaos/{id}` | Busca órgão |
| POST | `/api/orgaos` | Cria órgão |
| PUT | `/api/orgaos/{id}` | Atualiza órgão |
| DELETE | `/api/orgaos/{id}` | Exclui órgão |
| GET | `/api/fontes` | Fontes oficiais com data de consulta |
| DELETE | `/api/fontes/{id}` | Exclui uma fonte sem uso (409 se ainda houver serviço ou indicador ligado) |
| GET | `/api/documentos` | Lista documentos |
| GET | `/api/documentos/{id}` | Busca documento |
| POST | `/api/documentos` | Cria documento |
| PUT | `/api/documentos/{id}` | Atualiza documento |
| DELETE | `/api/documentos/{id}` | Exclui documento |
| POST | `/api/assistente/perguntas` | Pergunta ao assistente |
| GET | `/api/assistente/interacoes` | Histórico (sem texto do cidadão) |
| GET | `/api/assistente/interacoes/{id}` | Busca interação |
| DELETE | `/api/assistente/interacoes/{id}` | Exclui interação |
| GET | `/api/assistente/indicadores` | Indicador de impacto |
| GET | `/api/municipios?busca={nome}` | Busca municípios (sem diferenciar acentos) |
| GET | `/api/municipios/{codigoIbge}` | Dados do município |
| GET | `/api/municipios/{codigoIbge}/panorama` | Indicadores públicos do município, com fonte e período |
| GET | `/api/indicadores` | Indicadores disponíveis |
| GET | `/api/sincronizacao` | Situação da integração com a Base dos Dados (configurada, em andamento, último resultado) |
| POST | `/api/sincronizacao` | Roda o SQL na Base dos Dados e grava o período mais recente no Oracle |
| GET | `/api/status` | Verifica se a API está no ar |

### Exemplos

**Perguntar ao assistente**
```http
POST /api/assistente/perguntas
Content-Type: application/json

{ "mensagem": "Estou desempregado e quero um curso gratuito para conseguir trabalho" }
```

**Criar serviço**
```http
POST /api/servicos
Content-Type: application/json

{
  "categoriaId": 2,
  "nome": "Curso de exemplo",
  "descricao": "Descrição em linguagem simples",
  "publicoAlvo": "Jovens a partir de 16 anos",
  "orgaoId": 1,
  "canal": "DIGITAL",
  "urlFonte": "https://www.gov.br/...",
  "dataVerificacao": "2026-11-10",
  "palavrasChave": "curso,jovem,aprendiz",
  "ativo": true
}
```

**Panorama de Caieiras (SP)**
```http
GET /api/municipios/3509007/panorama
```
```json
{
  "municipio": { "codigoIbge": 3509007, "nome": "Caieiras", "uf": "SP" },
  "indicadores": [
    { "codigo": "POPULACAO", "nome": "População estimada", "valor": 98549, "unidade": "pessoas", "periodo": "2025",
      "fonte": "Base dos Dados - População Brasileira (IBGE)", "urlFonte": "https://basedosdados.org/dataset/..." }
  ]
}
```

**Formato de erro (todas as falhas)**
```json
{
  "timestamp": "2026-11-10T20:15:00",
  "status": 422,
  "erro": "Unprocessable Entity",
  "mensagem": "A URL da fonte oficial deve começar com https://",
  "caminho": "/api/servicos"
}
```

## Integração com a Base dos Dados

A [Base dos Dados](https://basedosdados.org) organiza dados públicos brasileiros no **Google BigQuery**, consultáveis com SQL.
A API usa a Base dos Dados como **fonte dos indicadores do panorama**:

1. `BaseDosDadosRepository` envia SQL para a API REST do BigQuery (`jobs.query`), autenticado com uma conta de serviço do Google Cloud.
2. `SincronizacaoService` faz duas consultas por indicador: o **período mais recente** e o **valor de cada município** nesse período. Toda agregação (soma, contagem) acontece no BigQuery, então **nenhum dado pessoal chega à API**.
3. Os valores são gravados no Oracle (`MUNICIPIO`, `INDICADOR_MUNICIPIO`), e a `DT_CONSULTA` da `FONTE` passa a ser a data da sincronização. Períodos antigos continuam guardados, e o panorama mostra sempre o mais recente.
4. As telas leem do Oracle: o cidadão não espera o BigQuery a cada busca.

| Indicador | Tabela na Base dos Dados | Cálculo |
|---|---|---|
| `POPULACAO` | `br_ibge_populacao.municipio` | `populacao` do ano mais recente |
| `SALDO_EMPREGO_FORMAL` | `br_me_caged.microdados_movimentacao` | `SUM(saldo_movimentacao)` do último ano completo |
| `FAMILIAS_BOLSA_FAMILIA` | `br_cgu_beneficios_cidadao.novo_bolsa_familia` | `COUNT(DISTINCT nis_favorecido)` da competência mais recente |
| `VALOR_BOLSA_FAMILIA` | `br_cgu_beneficios_cidadao.novo_bolsa_familia` | `SUM(valor_parcela)` da competência mais recente |
| `ESTABELECIMENTOS_SUS` | `br_ms_cnes.estabelecimento` | estabelecimentos com `indicador_vinculo_sus = 1` na competência mais recente |
| (municípios) | `br_bd_diretorios_brasil.municipio` | código IBGE, nome e UF |

**Quando roda:** pelo `POST /api/sincronizacao` (botão no painel de indicadores do site) e, ao subir a API, em segundo plano, se a última consulta tiver mais de 7 dias.

### Configurar o acesso (uma vez)
1. Em https://console.cloud.google.com crie um projeto (o BigQuery tem 1 TB de consulta grátis por mês; o modo sandbox dispensa cartão).
2. Ative a **BigQuery API** no projeto.
3. Em **IAM > Contas de serviço**, crie uma conta com o papel **Usuário de jobs do BigQuery** (`roles/bigquery.jobUser`) e gere uma **chave JSON**.
4. Coloque o conteúdo do JSON na variável `BASEDOSDADOS_CREDENTIALS` (no Render: *Environment > Add Environment Variable*). Nunca commite esse arquivo.
5. Chame `POST /api/sincronizacao` (ou o botão no painel) e confira o resultado.

Sem a variável, a API funciona normalmente com os dados já gravados no Oracle, e `POST /api/sincronizacao` responde 503 explicando o que falta.

## Como executar

### Ferramentas e versões
- Java JDK 21
- Maven 3.9+ (ou o Maven embutido do IntelliJ)
- IntelliJ IDEA
- Banco Oracle da FIAP (`oracle.fiap.com.br:1521/ORCL`)
- Spring Boot 4.1

### 1. Rodar local sem Oracle (teste rápido, banco H2 em memória)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
No IntelliJ: *Run > Edit Configurations > Active profiles* = `dev`.
Acesse `http://localhost:8080/swagger-ui.html`.

### 2. Rodar local com o Oracle da FIAP
Defina as variáveis de ambiente (no IntelliJ: *Edit Configurations > Environment variables*):
```
DB_USER=rmXXXXXX
DB_PASSWORD=sua_senha
```
Depois rode `CidadaoFacilApplication`. Na primeira execução o Flyway cria as 14 tabelas e carrega os dados (tabela de controle: `flyway_schema_history`).
Se as tabelas já tiverem sido criadas pelos scripts de Database, o Flyway reconhece a versão e não roda de novo.

> As credenciais nunca vão para o código nem para o GitHub.

### 3. Deploy no Render
1. Suba este projeto no GitHub (repositório público ou com o Render autorizado).
2. No Render: **New > Web Service** > conecte o repositório.
3. **Runtime: Docker** (o Render usa o `Dockerfile` da raiz). Plano **Free**.
4. Em **Environment**, adicione `DB_USER` e `DB_PASSWORD` (e, se quiser, `DB_URL`).
5. **Health Check Path:** `/api/status`.
6. Aguarde o build (5 a 10 min na primeira vez) e acesse `https://<nome>.onrender.com/swagger-ui.html`.

> O plano free "dorme" após inatividade: a primeira requisição pode levar ~1 minuto. Acesse o link antes de enviar e antes da correção.

### Variáveis de ambiente

| Variável | Padrão | Uso |
|---|---|---|
| `DB_URL` | `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL` | URL do Oracle |
| `DB_USER` | — | Usuário (RM) |
| `DB_PASSWORD` | — | Senha |
| `DDL_AUTO` | `none` | As tabelas são criadas pelo Flyway; deixe `none` |
| `PORT` | `8080` | Definida automaticamente pelo Render |
| `BASEDOSDADOS_CREDENTIALS` | — | JSON da conta de serviço do Google Cloud (Base dos Dados / BigQuery) |
| `BASEDOSDADOS_BILLING_PROJECT` | `project_id` do JSON | Projeto do Google Cloud que paga a consulta |
| `BASEDOSDADOS_UF` | `SP` | UF dos municípios sincronizados |
| `BASEDOSDADOS_SYNC_ON_STARTUP` | `true` | Sincroniza ao subir, se os dados estiverem desatualizados |
| `BASEDOSDADOS_MAX_AGE_DAYS` | `7` | Idade máxima (em dias) dos dados antes da sincronização automática |

## Dados

- **Catálogo:** 6 serviços reais, resumidos de páginas oficiais gov.br consultadas em 10/11/2026 (link e data em `FONTE`).
- **Panorama:** a carga inicial (`V2__carga.sql`) veio de uma consulta à Base dos Dados em 18/11/2026: população 2025 (IBGE), saldo do Novo CAGED em 2025, Bolsa Família e CNES na competência 03/2026. A partir daí, `POST /api/sincronizacao` busca o período mais recente direto na Base dos Dados.
- Nenhum dado pessoal é usado ou armazenado; as interações da carga são demonstrativas.

## Integrantes

| Nome | RM |
|---|---|
| Davi Marques de Andrade Munhoz | 566223 |
| Diogo Oliveira Lima | 562559 |
| Leandro Simoneli da Silva | 566539 |
| Lucas dos Reis Aquino | 562414 |
| Lucas Perez Bonato | 565356 |
