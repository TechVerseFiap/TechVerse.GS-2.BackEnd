# Cidadão Fácil — API (Domain Driven Design - Java)

> Global Solution FIAP 2026.2 · CitizenTech · Engenharia de Software · 2º ano

## Ficha da CitizenTech

| Campo | Definição |
|---|---|
| Nome da CitizenTech | Cidadão Fácil |
| Cidade e bairro | _preencher (ex.: São Paulo – bairro X)_ |
| Cidadão beneficiado | Pessoas que procuram emprego, qualificação e serviços públicos relacionados, especialmente quem tem pouca familiaridade com portais do governo |
| Problema, em uma frase | Cidadãos têm dificuldade para encontrar, entender e acessar informações sobre serviços públicos, documentos, direitos e benefícios |
| Como funciona | O cidadão descreve o que precisa; o assistente identifica o assunto, consulta um catálogo com fonte oficial e mostra requisitos, documentos, etapas e o link oficial |
| O que cada parte faz | **Java (este repositório):** API REST com catálogo e assistente · Banco Oracle: persistência · Front-end: telas de busca e detalhe · Python: análise de demanda / algoritmo · Packet Tracer: rede do ponto de atendimento |
| Link do repositório | _preencher_ |
| Indicador de impacto | Taxa de buscas que terminam com uma orientação encontrada (`GET /api/assistente/indicadores`) |
| ODS conectados | 4, 8, 10 e 16 |

---

## O que a API faz

1. **Catálogo de serviços públicos** (CRUD completo): categorias, serviços, requisitos, etapas e documentos.
2. **Assistente:** recebe uma frase em linguagem natural ("estou desempregado e quero um curso gratuito") e devolve os serviços mais relevantes, com justificativa e link oficial.
3. **Transparência:** todo serviço tem URL oficial (https obrigatório) e data de verificação; informações com mais de 180 dias são sinalizadas (`precisaReverificacao`).
4. **Privacidade:** o texto digitado pelo cidadão **não é salvo**, apenas o assunto identificado e o resultado (base do indicador de impacto).
5. **Honestidade:** quando não encontra nada, o assistente admite e sugere categorias; nunca afirma que a pessoa "tem direito".

## Arquitetura em camadas

```
controller  →  bo  →  dao  →  Oracle
   (HTTP)    (regras) (acesso a dados)
     ↑
 exception (GlobalExceptionHandler: transforma erros em JSON padronizado)
```

| Pacote | Responsabilidade |
|---|---|
| `model` | Entidades do domínio mapeadas para as tabelas `T_CF_*` (JPA) |
| `dao` | Interfaces Spring Data JPA: acesso ao banco (findBy..., existsBy...) |
| `bo` | Regras de negócio (nome duplicado, fonte https, ordem de etapas, lógica do assistente) |
| `controller` | Endpoints REST (GET, POST, PUT, DELETE) |
| `dto` | O que entra (Request, com validação) e o que sai (Response) da API |
| `exception` | Exceções do domínio + tratamento global de erros |
| `config` | CORS, Swagger e carga dos dados demonstrativos |

### Regras de negócio implementadas (BO)

| Regra | Onde | HTTP |
|---|---|---|
| Nome de categoria e de documento é único | CategoriaBO, DocumentoBO | 409 |
| Não excluir categoria que tem serviços | CategoriaBO | 409 |
| URL da fonte oficial deve começar com `https://` | ServicoBO | 422 |
| Data de verificação não pode ser futura | ServicoDTO (`@PastOrPresent`) | 400 |
| Ordem da etapa não se repete no mesmo serviço | EtapaBO | 409 |
| Documento vinculado a serviço não pode ser excluído | DocumentoBO | 409 |
| Serviço já recomendado pelo assistente não pode ser excluído (histórico) | ServicoBO | 409 |
| Registro inexistente | todos os BOs | 404 |

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
| PUT | `/api/servicos/{id}/documentos/{documentoId}` | Altera observação do vínculo |
| DELETE | `/api/servicos/{id}/documentos/{documentoId}` | Desvincula documento |
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
  "orgaoResponsavel": "Órgão X",
  "canal": "DIGITAL",
  "urlFonte": "https://www.gov.br/...",
  "dataVerificacao": "2026-11-10",
  "palavrasChave": "curso,jovem,aprendiz"
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

## Como executar

### Ferramentas e versões
- Java JDK 21
- Maven 3.9+ (ou o Maven embutido do IntelliJ)
- IntelliJ IDEA
- Banco Oracle da FIAP (`oracle.fiap.com.br:1521/ORCL`)
- Spring Boot 3.5

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
Depois rode `CidadaoFacilApplication`. Na primeira execução as tabelas `T_CF_*` são criadas e o catálogo demonstrativo é inserido.

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
| `DDL_AUTO` | `update` | `update` cria as tabelas; `validate` quando o DDL oficial já estiver aplicado |
| `SEED_ENABLED` | `true` | Insere o catálogo demonstrativo se o banco estiver vazio |
| `PORT` | `8080` | Definida automaticamente pelo Render |

## Dados

Os serviços cadastrados são **demonstrativos**, resumidos a partir de páginas oficiais gov.br consultadas em 10/11/2026 (links no campo `urlFonte` de cada serviço). Nenhum dado pessoal real é usado ou armazenado.

## Integrantes

| Nome | RM |
|---|---|
| Davi Marques de Andrade Munhoz | 566223 |
| Diogo Oliveira Lima | 562559 |
| Leandro Simoneli da Silva | 566539 |
| Lucas dos Reis Aquino | 562414 |
| Lucas Perez Bonato | 565356 |
