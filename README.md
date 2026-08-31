# Track Flow

Uma plataforma logística full-stack construída como projeto de estudo: Java/Spring no backend, Angular/NgRx no frontend. Começou como rastreamento de envios e cresceu para um sistema de operações completo — catálogo de produtos, estoque, pedidos, clientes e transportadoras — com o envio como a etapa final de um fluxo real de pedido, não mais um cadastro solto. A interface está disponível em português, inglês e espanhol.

## O Problema

Uma transportadora precisa de mais do que só rastrear pacotes: precisa saber o que tem em estoque, receber pedidos de clientes, reservar e dar baixa em produtos conforme o pedido avança, escolher uma transportadora só na hora de despachar, e permitir que o cliente acompanhe a entrega em tempo real — sem expor sistemas internos nem exigir login do cliente.

## Por que este projeto

Construído para praticar desenvolvimento full-stack de ponta a ponta, com uma escolha arquitetural deliberada mantida nos dois lados da stack: um **Monolito Modular**, organizado por capacidade de negócio (`shipment`, `auth`, `user`, `product`, `inventory`, `order`, `customer`, `carrier`) em vez de camada técnica.

O projeto usa **dois padrões de comunicação entre módulos, escolhidos deliberadamente conforme a necessidade**, não um só aplicado sem pensar:

- **Eventos de domínio (assíncronos, fire-and-forget)** — quando quem publica não precisa de resposta. Um `Product` criado publica `ProductCreatedEvent`; só o módulo `inventory` escuta, criando o registro de estoque inicial. Um `Shipment` que chega em `DELIVERED` publica `ShipmentStatusChangedEvent`; só o módulo `order` escuta, pra sincronizar o pedido vinculado sozinho. Os dois listeners rodam `@Async` — uma pegadinha real do Spring: um `@TransactionalEventListener(phase = AFTER_COMMIT)` que grava no banco precisa da própria transação, senão a escrita falha em silêncio (sem exceção, sem log).
- **Chamada direta (síncrona)** — quando quem chama precisa do resultado na hora. `OrderService` orquestra `ProductService`, `InventoryService`, `ShipmentService`, `CustomerService` e `CarrierService` via chamada direta às APIs públicas de cada um (nunca repositório ou entidade interna) — confirmar um pedido precisa saber, ali mesmo, se a reserva de estoque deu certo; despachar precisa do id do envio recém-criado pra gravar no pedido. Um evento assíncrono não devolveria isso a tempo.

## Arquitetura

```
Backend (Spring Boot)                  Frontend (Angular)
┌───────────────────────────┐          ┌───────────────────────────┐
│ Controllers + WebSocket    │  REST +  │ HTTP + WebSocket services │
│ (STOMP endpoints)          │◄───────► │ (HttpClient, RxJS/STOMP)  │
├───────────────────────────┤  STOMP   ├───────────────────────────┤
│ Services                   │          │ NgRx effects               │
│ (business logic, domain    │          │ (side effects, API calls) │
│  events + direct calls     │          ├───────────────────────────┤
│  between modules)          │          │ NgRx store                 │
├───────────────────────────┤          │ (actions, reducers,        │
│ Repositories (Spring Data  │          │  selectors, entity)        │
│  JPA)                      │          ├───────────────────────────┤
├───────────────────────────┤          │ Components                 │
│ PostgreSQL                 │          │ (standalone, Material)     │
└───────────────────────────┘          └───────────────────────────┘
```

Dois exemplos concretos de como o tempo real funciona:

- **Envio → Pedido:** quando o status de um `Shipment` muda, o service persiste e publica `ShipmentStatusChangedEvent`; um `@TransactionalEventListener(phase = AFTER_COMMIT)` captura — deliberadamente *depois* que a transação é commitada, pra nunca anunciar uma mudança que acabe sendo revertida — e transmite via STOMP para `/topic/shipments/{trackingCode}`. Se o novo status é `DELIVERED`, um segundo listener (no módulo `order`) reage ao mesmo evento e sincroniza o pedido vinculado sozinho.
- **Estoque baixo:** ao contrário do tópico de envio (por recurso), o alerta de estoque baixo usa **um único tópico pra todo o painel**, `/topic/inventory/low-stock` — porque é uma preocupação de qualquer staff olhando a tela de produtos, não de quem está acompanhando um item específico.

No frontend, serviços de WebSocket dedicados alimentam a store NgRx diretamente, então qualquer tela aberta se atualiza sozinha, sem polling nem refresh manual.

## Stack Tecnológica

| Área | Tecnologia |
|---|---|
| Linguagem / runtime do backend | Java 21, Spring Boot 4.1 |
| Build tool do backend | Maven |
| Banco de dados | PostgreSQL, Flyway |
| Tempo real | Spring WebSocket (STOMP, sem SockJS) |
| Autenticação | JWT auto-emitido (Spring Security, HMAC/HS256) |
| Testes do backend | Testcontainers (PostgreSQL), Spring Boot Test |
| Framework do frontend | Angular 22 (componentes standalone) |
| Gerenciamento de estado | NgRx (store, effects, entity, store-devtools) |
| UI | Angular Material (tema escuro customizado) |
| Formulários | Reactive Forms |
| Cliente de tempo real | @stomp/stompjs |
| i18n | `TranslationService` próprio, baseado em signal, com dicionários JSON em runtime (PT / EN / ES) |

## Estrutura do Repositório

```
track-flow/
  backend/     API Spring Boot
  frontend/    Aplicação Angular
```

## Rodando Localmente

### 1. Backend

```bash
cd backend
cp .env.example .env   # preencha credenciais do banco e JWT_SECRET (openssl rand -base64 32)
docker compose up -d   # sobe o PostgreSQL
./mvnw spring-boot:run
```

O Flyway aplica todas as migrations na inicialização, incluindo uma conta de admin já semeada:

```
email:    admin@trackflow.dev
password: ChangeMe123!
```

Troque essa senha imediatamente fora de ambiente de desenvolvimento local.

**Dados de demonstração (opcional):** `mock-data.sql`, na raiz do repositório, popula um banco vazio com um cenário realista — 8 clientes, 5 transportadoras, 15 produtos (com estoque propositalmente baixo em alguns, pra ver o alerta disparar), 12 pedidos cobrindo todos os status, e os envios/históricos correspondentes. Roda direto contra o Postgres (`psql`, DBeaver, etc.) depois que as migrations já tiverem sido aplicadas ao menos uma vez. Não é uma migration Flyway — é só um script solto pra facilitar demonstração.

### 2. Frontend

```bash
cd frontend
npm install
ng serve
```

Abra `http://localhost:4200`. O dev server da Angular CLI faz proxy de `/api` e `/ws` para o backend em `localhost:8080` (ver `frontend/proxy.conf.json`), então não é necessária configuração de CORS em desenvolvimento.

## Tour de Funcionalidades

- **Dashboard** (`/`, autenticado, tela inicial após o login) — cards de estatística por módulo: envios por status, pedidos por status, catálogo de produtos (total, ativos, estoque baixo), clientes e transportadoras, e — só pra admin — equipe. Tudo calculado a partir do estado já carregado na store NgRx, sem endpoint de resumo dedicado no backend.
- **Catálogo de produtos** (`/products`) — CRUD aberto a qualquer staff autenticado (exclusão fica admin-only); o SKU é imutável depois de criado; desativar e excluir são dois estados de ciclo de vida distintos (produto pausado vs. descontinuado); preço com formatação de moeda local via `Intl.NumberFormat`, adaptando o símbolo por idioma (pt → R$, en → $, es → €); dialog de ajuste de estoque direto na linha do produto.
- **Estoque** — 1:1 com cada produto; *optimistic locking* (`@Version`) protege contra ajustes concorrentes na mesma linha; alerta de estoque baixo transmitido por um tópico único de WebSocket pra todo o painel, assim que a quantidade disponível cruza o limiar configurado.
- **Pedidos** (`/orders`) — ciclo de vida completo `PENDING → CONFIRMED → SHIPPED → DELIVERED` (ou `CANCELLED` em qualquer um dos três primeiros estágios). Criar um pedido **nunca** reserva estoque — a reserva só acontece na confirmação, pra suportar uma futura integração externa criando pedidos sem travar estoque de algo ainda não revisado por um humano. Despachar é uma ação explícita ("Despachar Pedido", não automática), momento em que a transportadora é escolhida e o `Shipment` é criado de verdade; `DELIVERED` nunca é setado manualmente — é sincronizado sozinho assim que o envio vinculado chega lá.
- **Clientes / Transportadoras** (`/customers`, `/carriers`) — módulos de CRUD independentes, referenciados pelos pedidos (o endereço do cliente pré-preenche o destino do pedido; a transportadora é escolhida só na hora do despacho, já que disponibilidade de transportadora é uma decisão do momento da logística, não da criação do pedido).
- **Envios** (`/shipments`) — não são mais criados manualmente: todo envio agora nasce de despachar um pedido. Atualizar status, rastrear e excluir continuam funcionando como antes.
- **Página pública de rastreamento** (`/track`) — acessível sem login; digite um código de rastreio pra ver status e histórico, atualizando ao vivo via WebSocket.
- **Autenticação** — login por e-mail/senha emitindo um JWT auto-assinado; o token é persistido no `localStorage` e lido de forma síncrona no estado inicial da store NgRx (evitando uma race condition entre o guard de rota e a restauração da sessão ao recarregar a página); um `HttpInterceptorFn` anexa o token automaticamente em toda requisição `/api`.
- **Gerenciamento de usuários** (`/admin/users`, somente admin) — criar usuários, mudar papéis (`ADMIN` / `OPS`), e desativar/reativar contas (soft delete, preservando o histórico de auditoria).
- **Internacionalização** — a interface inteira, incluindo todos os módulos acima, labels de status, papéis, e mensagens de erro da API, está disponível em português (padrão), inglês e espanhol, alternável instantaneamente por um toggle na sidebar, sem recarregar a página. Ver [Internacionalização](#internacionalização) abaixo.

## Referência da API

URL base: `http://localhost:8080`. Todos os corpos de requisição/resposta são JSON.

### Auth (`/api/auth`)

| Método | Path | Autenticação | Descrição |
|---|---|---|---|
| POST | `/api/auth/login` | Pública | Autentica com `{ email, password }`, retorna `{ token }` (JWT, expira em 2h). |

### Usuários (`/api/users`) — todos os endpoints exigem `ROLE_ADMIN`

| Método | Path | Descrição |
|---|---|---|
| POST | `/api/users` | Cria um usuário: `{ email, password, role }`. |
| GET | `/api/users` | Lista todos os usuários. |
| PATCH | `/api/users/{id}/role` | Muda o papel de um usuário: `{ role }`. |
| PATCH | `/api/users/{id}/deactivate` | Desativa um usuário (soft delete). |
| PATCH | `/api/users/{id}/activate` | Reativa um usuário previamente desativado. |
| DELETE | `/api/users/{id}` | Exclui um usuário (soft delete). Rejeitado com `SELF_DELETION_NOT_ALLOWED` se quem chama tentar excluir a própria conta. |

### Produtos (`/api/products`) — criar/editar aberto a qualquer staff; exclusão exige `ROLE_ADMIN`

| Método | Path | Descrição |
|---|---|---|
| POST | `/api/products` | Cria um produto: `{ sku, name, description?, unitPrice, initialQuantity? }`. |
| GET | `/api/products` | Lista produtos não excluídos. |
| GET | `/api/products/{id}` | Busca um produto (inclui excluídos — outros módulos precisam resolver histórico). |
| PUT | `/api/products/{id}` | Atualiza `{ name, description?, unitPrice }`. SKU é imutável. |
| PATCH | `/api/products/{id}/activate` \| `/deactivate` | Ativa/desativa. |
| DELETE | `/api/products/{id}` | Soft-delete (`ROLE_ADMIN`). |

### Estoque (`/api/inventory`) — aberto a qualquer staff autenticado

| Método | Path | Descrição |
|---|---|---|
| GET | `/api/inventory` | Lista o estoque de todos os produtos. |
| GET | `/api/inventory/{productId}` | Estoque de um produto. |
| PATCH | `/api/inventory/{productId}/adjust` | `{ quantityDelta }` — positivo repõe, negativo corrige/dá baixa. Rejeita se o resultado for negativo. |
| PATCH | `/api/inventory/{productId}/threshold` | `{ lowStockThreshold }`. |

### Pedidos (`/api/orders`) — criar/confirmar/despachar/cancelar aberto a qualquer staff; exclusão exige `ROLE_ADMIN`

| Método | Path | Descrição |
|---|---|---|
| POST | `/api/orders` | Cria um pedido: `{ customerId, origin, destination, items: [{ productId, quantity }] }`. Nasce `PENDING`, sem reservar estoque. |
| GET | `/api/orders` | Lista pedidos não excluídos. |
| GET | `/api/orders/{id}` | Busca um pedido. |
| GET | `/api/orders/{id}/items` | Itens do pedido. |
| PATCH | `/api/orders/{id}/confirm` | `PENDING → CONFIRMED`, reserva estoque de cada item. |
| PATCH | `/api/orders/{id}/ship` | `{ carrierId }`. `CONFIRMED → SHIPPED`: decrementa estoque físico, cria o `Shipment`. |
| PATCH | `/api/orders/{id}/cancel` | Cancela (de `PENDING`, `CONFIRMED` ou `SHIPPED`). Libera reserva se `CONFIRMED`; cancela o envio vinculado (sem repor estoque) se `SHIPPED`. |
| DELETE | `/api/orders/{id}` | Soft-delete (`ROLE_ADMIN`). |

### Clientes (`/api/customers`) e Transportadoras (`/api/carriers`)

Mesmo formato de CRUD nos dois — criar/editar/ativar/desativar aberto a qualquer staff, exclusão exige `ROLE_ADMIN`:

| Método | Path | Descrição |
|---|---|---|
| POST | `/api/customers` \| `/api/carriers` | Cria. `Customer`: `{ name, email, phone, address }`. `Carrier`: `{ name, contactInfo? }`. |
| GET | `/api/customers` \| `/api/carriers` | Lista os não excluídos. |
| GET | `/api/customers/{id}` \| `/api/carriers/{id}` | Busca um registro. |
| PUT | `/api/customers/{id}` \| `/api/carriers/{id}` | Atualiza todos os campos. |
| PATCH | `.../{id}/activate` \| `/deactivate` | Ativa/desativa. |
| DELETE | `/api/customers/{id}` \| `/api/carriers/{id}` | Soft-delete (`ROLE_ADMIN`). |

### Envios (`/api/shipments`)

Sem endpoint de criação manual — todo envio nasce de `PATCH /api/orders/{id}/ship`.

| Método | Path | Autenticação | Descrição |
|---|---|---|---|
| GET | `/api/shipments/{trackingCode}` | Pública | Busca um único envio. |
| GET | `/api/shipments/{trackingCode}/history` | Pública | Histórico de eventos de rastreio. |
| PUT | `/api/shipments/{trackingCode}/status` | Autenticado | Atualiza status: `{ status, location?, description? }`. `status` é uma string livre — o backend não valida contra um enum fixo; o conjunto de valores (`CREATED`, `IN_TRANSIT`, `OUT_FOR_DELIVERY`, `DELIVERED`, `CANCELLED`) é convenção do frontend. |
| GET | `/api/shipments` | Autenticado | Lista todos os envios. |
| DELETE | `/api/shipments/{trackingCode}` | `ROLE_ADMIN` | Exclui um envio. |

### WebSocket

Endpoint STOMP: `ws://localhost:8080/ws` (sem fallback SockJS).

| Tópico | Escopo | Conteúdo |
|---|---|---|
| `/topic/shipments/{trackingCode}` | Por recurso | Mudanças de status e exclusão daquele envio. |
| `/topic/inventory/low-stock` | Painel inteiro | Alerta sempre que o estoque disponível de qualquer produto cruza o limiar configurado. |

### Respostas de Erro

Erros retornam um corpo JSON consistente em todos os módulos:

```json
{
  "timestamp": "2026-08-31T14:32:10.123",
  "errorCode": "ORDER_NOT_FOUND",
  "message": "No order found with id 42",
  "fields": null
}
```

- `errorCode` é um identificador estável e legível por máquina — o frontend o mapeia pra uma string localizada (ver [Internacionalização](#internacionalização)).
- `message` é uma descrição livre, em inglês, pra logs e depuração.
- `fields` só é preenchido em erros de validação (`VALIDATION_FAILED`).

Cada exceção de domínio implementa `ApiException` (`common/error/ApiException.java`), expondo seu próprio `errorCode`. `ACCESS_DENIED` (403) e `UNAUTHENTICATED` (401) são exceções globais, tratadas fora do fluxo normal do Spring MVC (`RestAccessDeniedHandler`/`RestAuthenticationEntryPoint`), já que são lançadas dentro da cadeia de filtros do Spring Security, antes de qualquer `@RestControllerAdvice` conseguir interceptar.

| errorCode | Status HTTP | Módulo |
|---|---|---|
| `INVALID_CREDENTIALS` | 401 | auth |
| `EMAIL_ALREADY_REGISTERED` | 409 | auth |
| `USER_NOT_FOUND` | 404 | auth |
| `ACCOUNT_DEACTIVATED` | 403 | auth |
| `SELF_DELETION_NOT_ALLOWED` | 409 | auth |
| `SHIPMENT_NOT_FOUND` | 404 | shipment |
| `PRODUCT_NOT_FOUND` | 404 | product |
| `SKU_ALREADY_EXISTS` | 409 | product |
| `INVENTORY_NOT_FOUND` | 404 | inventory |
| `INSUFFICIENT_STOCK` | 409 | inventory |
| `STOCK_CONFLICT` | 409 | inventory (optimistic locking) |
| `ORDER_NOT_FOUND` | 404 | order |
| `INVALID_ORDER_STATUS` | 409 | order |
| `PRODUCT_NOT_ORDERABLE` | 409 | order |
| `CUSTOMER_NOT_ORDERABLE` | 409 | order |
| `CARRIER_NOT_ORDERABLE` | 409 | order |
| `CUSTOMER_NOT_FOUND` | 404 | customer |
| `CARRIER_NOT_FOUND` | 404 | carrier |
| `VALIDATION_FAILED` | 400 | qualquer módulo (bean validation) |
| `ACCESS_DENIED` | 403 | global (Spring Security) |
| `UNAUTHENTICATED` | 401 | global (Spring Security) |

## Internacionalização

A aplicação suporta português (padrão), inglês e espanhol, alternáveis em runtime sem recarregar a página.

**Abordagem:** um `TranslationService` próprio, baseado em signal (`frontend/src/app/core/translation.service.ts`), em vez de `@angular/localize` ou `ngx-translate`. `@angular/localize` foi descartado porque compila um build separado por idioma, o que não suporta um toggle em runtime. `ngx-translate` foi considerado, mas deixado de lado em favor de construir o mecanismo na mão, consistente com a abordagem geral deste projeto.

**Como funciona:**
- Três dicionários JSON ficam em `frontend/public/i18n/` (`pt.json`, `en.json`, `es.json`), carregados uma única vez na inicialização da aplicação via `provideAppInitializer`, antes da app renderizar.
- O idioma ativo fica guardado num `signal`, persistido no `localStorage` (`track_flow_lang`), e lido de forma síncrona na inicialização para evitar qualquer flash do idioma errado — o mesmo padrão usado na restauração da sessão JWT.
- Templates resolvem traduções através de uma pipe impura, `TranslatePipe` (`{{ 'key.path' | translate }}`, com interpolação opcional via `{{param}}`); código fora de template (ex: montar o payload de um dialog de confirmação) chama `TranslationService.t()` diretamente.
- Um toggle de idioma (PT / EN / ES) fica na sidebar do app shell.
- Os textos internos do Angular Material (paginador, etc.) são traduzidos separadamente via `AppPaginatorIntl`, já que o Material tem seu próprio mecanismo de i18n, fora dos nossos dicionários.

**Valores de domínio vs. texto de exibição:** valores de status (envio, pedido) e papéis de usuário são traduzidos apenas para exibição, através de uma camada de mapeamento por chave (`shipment.status.*`, `order.status.*`, `auth.role.*`). O valor real enviado e recebido do backend nunca é traduzido — só o label mostrado ao usuário muda conforme o idioma selecionado.

**Preço em moeda local:** o preço do produto usa `Intl.NumberFormat` nativo, sem dependência externa, adaptando símbolo e separadores por idioma (pt → R$, en → $, es → €) tanto na exibição quanto na máscara de digitação do formulário.

**Mensagens de erro do backend:** como descrito em [Respostas de Erro](#respostas-de-erro) acima, erros da API incluem um `errorCode` estável. Os effects do NgRx extraem esse código via `extractErrorCode()` (`frontend/src/app/core/http-error.utils.ts`) e o guardam no state em vez da mensagem crua do HTTP; os templates resolvem esse código para uma string localizada através das chaves do dicionário `errors.*`, caindo em `errors.UNKNOWN_ERROR` pra qualquer coisa fora do formato `ErrorResponse` do backend.

## Testes

- **Backend:** `cd backend && ./mvnw verify` — suíte de testes de integração completa contra containers PostgreSQL efêmeros via Testcontainers, cobrindo todos os módulos (`auth`, `shipment`, `product`, `inventory`, `order`, `customer`, `carrier`), incluindo:
  - Regras de negócio e todos os `errorCode` de cada módulo, nas camadas de service e de HTTP.
  - Um teste determinístico de *optimistic locking* no estoque (duas cópias da mesma linha carregadas independentemente, a segunda gravação rejeitada).
  - Os dois listeners assíncronos de eventos cross-module (`product → inventory`, `shipment → order`), com polling nos testes já que ambos rodam `@Async`.
  - Um teste de WebSocket ponta a ponta pra cada um dos dois tópicos (`shipments/{trackingCode}` e `inventory/low-stock`).
- **Frontend:** ainda não há suíte de testes automatizada — um próximo passo natural para este projeto. Até lá, mudanças são verificadas manualmente: alterne os idiomas pelo toggle da sidebar e percorra cada tela, e dispare cada `errorCode` da tabela acima para confirmar que a mensagem localizada aparece em vez do código cru ou de um texto desatualizado.

## Licença

Projeto pessoal de estudo. Sem restrições de licença — sinta-se à vontade para usar como referência.