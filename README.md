# Track Flow

Uma plataforma de rastreamento de envios logísticos construída como projeto de estudo full-stack: Java/Spring no backend, Angular/NgRx no frontend. A equipe de operações cria e atualiza envios em um console escuro e denso em dados; clientes rastreiam qualquer envio em tempo real, sem precisar de conta. A interface está disponível em português, inglês e espanhol.

## O Problema

Uma transportadora precisa de uma forma de registrar envios e seus status conforme se movem da coleta até a entrega, e de uma forma dos clientes acompanharem seu pacote em tempo real — sem expor sistemas internos nem exigir login do cliente.

## Por que este projeto

Construído para praticar desenvolvimento full-stack de ponta a ponta, com uma escolha arquitetural deliberada mantida nos dois lados da stack: um **Monolito Modular**, organizado por capacidade de negócio (`shipment`, `auth`, `user`) em vez de camada técnica, com atualizações em tempo real propagadas através dos eventos de domínio internos do Spring no backend e uma store NgRx por feature no frontend — sem broker de mensagens externo, porque a escala deste projeto não precisa disso.

## Arquitetura

```
Backend (Spring Boot)                  Frontend (Angular)
┌───────────────────────────┐          ┌───────────────────────────┐
│ Controllers + WebSocket    │  REST +  │ HTTP + WebSocket services │
│ (STOMP endpoints)          │◄───────► │ (HttpClient, RxJS/STOMP)  │
├───────────────────────────┤  STOMP   ├───────────────────────────┤
│ Services                   │          │ NgRx effects               │
│ (business logic, publishes │          │ (side effects, API calls) │
│  domain events)            │          ├───────────────────────────┤
├───────────────────────────┤          │ NgRx store                 │
│ Repositories (Spring Data  │          │ (actions, reducers,        │
│  JPA)                      │          │  selectors, entity)        │
├───────────────────────────┤          ├───────────────────────────┤
│ PostgreSQL                 │          │ Components                 │
│                             │          │ (standalone, Material)     │
└───────────────────────────┘          └───────────────────────────┘
```

Quando o status de um envio muda: o service persiste a mudança e publica um `ShipmentStatusChangedEvent`; um `@TransactionalEventListener(phase = AFTER_COMMIT)` captura esse evento — deliberadamente *depois* que a transação é commitada, para que uma notificação nunca anuncie uma mudança que acabe sendo revertida — e transmite via STOMP para `/topic/shipments/{trackingCode}`. No frontend, um `ShipmentSocketService` compartilhado se inscreve por código de rastreio e alimenta os eventos de volta na store NgRx, então qualquer tela aberta (a lista de operações, o dialog de detalhes, a página pública de rastreamento) se atualiza sozinha, sem polling nem refresh manual.

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

### 2. Frontend

```bash
cd frontend
npm install
ng serve
```

Abra `http://localhost:4200`. O dev server da Angular CLI faz proxy de `/api` e `/ws` para o backend em `localhost:8080` (ver `frontend/proxy.conf.json`), então não é necessária configuração de CORS em desenvolvimento.

## Tour de Funcionalidades

- **Console de operações** (`/`, autenticado) — layout de sidebar escuro com cards de estatística ao vivo (total / em trânsito / entregues / cancelados, calculados a partir do estado real, não métricas fabricadas), uma tabela de envios ordenável e filtrável com paginação, e uma animação de pulso na linha quando chega uma atualização via WebSocket.
- **Criar e atualizar envios** — formulários em modal (`MatDialog` do Angular Material) em vez de navegação de página inteira, para ações rápidas e em contexto. Envios cancelados não podem mais ser atualizados, refletido na UI com um ícone de cadeado desabilitado e com tooltip. A equipe pode mover um envio por `CREATED` → `IN_TRANSIT` → `OUT_FOR_DELIVERY` → `DELIVERED`, ou `CANCELLED` a qualquer momento antes da entrega.
- **Detalhe do envio** — clicar num código de rastreio abre um dialog com o progresso atual (um stepper passando por Created → In Transit → Delivered, ou um estado cancelado) e o histórico completo de eventos, sem sair da lista.
- **Página pública de rastreamento** (`/track`) — acessível sem login; digite um código de rastreio para ver status e histórico, também atualizando ao vivo via WebSocket.
- **Autenticação** — login por e-mail/senha emitindo um JWT auto-assinado; o token é persistido no `localStorage` e lido de forma síncrona no estado inicial da store NgRx (evitando uma race condition entre o guard de rota e a restauração da sessão ao recarregar a página); um `HttpInterceptorFn` anexa o token automaticamente em toda requisição `/api`.
- **Gerenciamento de usuários** (`/admin/users`, somente admin) — criar usuários, mudar papéis (`ADMIN` / `OPS`), e desativar/reativar contas (soft delete, preservando o histórico de auditoria). Protegido tanto por rota (`adminGuard`, decodificando a claim `role` do JWT) quanto, de fato, pela checagem `hasRole("ADMIN")` do backend.
- **Internacionalização** — a interface inteira (incluindo labels de status, papéis, e mensagens de erro da API) está disponível em português (padrão), inglês e espanhol, alternável instantaneamente por um toggle na sidebar, sem recarregar a página. Ver [Internacionalização](#internacionalização) abaixo para entender como foi construído.

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

### Envios (`/api/shipments`)

| Método | Path | Autenticação | Descrição |
|---|---|---|---|
| POST | `/api/shipments` | Autenticado | Cria um envio: `{ origin, destination, carrier }`. |
| GET | `/api/shipments/{trackingCode}` | Pública | Busca um único envio. |
| GET | `/api/shipments/{trackingCode}/history` | Pública | Busca seu histórico de eventos de rastreio. |
| PUT | `/api/shipments/{trackingCode}/status` | Autenticado | Atualiza o status: `{ status, location?, description? }`. `status` é uma string livre — o backend não valida contra um enum fixo; o conjunto de valores permitidos (`CREATED`, `IN_TRANSIT`, `OUT_FOR_DELIVERY`, `DELIVERED`, `CANCELLED`) hoje é uma convenção mantida só pela lista `STATUS_OPTIONS` do frontend. |
| GET | `/api/shipments` | Autenticado | Lista todos os envios. |
| DELETE | `/api/shipments/{trackingCode}` | `ROLE_ADMIN` | Exclui um envio. |

### WebSocket

Endpoint STOMP: `ws://localhost:8080/ws` (sem fallback SockJS). Inscreva-se em `/topic/shipments/{trackingCode}` para receber eventos ao vivo de mudança de status e exclusão daquele envio — usado tanto pelo console de operações quanto pela página pública de rastreamento.

### Respostas de Erro

Erros retornam um corpo JSON consistente em todos os módulos:

```json
{
  "timestamp": "2026-08-29T14:32:10.123",
  "errorCode": "SHIPMENT_NOT_FOUND",
  "message": "No shipment found with tracking code TF1A2B3C4D5E",
  "fields": null
}
```

- `errorCode` é um identificador estável e legível por máquina, pensado para uso programático — o frontend o mapeia para uma string localizada (ver [Internacionalização](#internacionalização)). Não muda entre releases para a mesma condição de erro.
- `message` é uma descrição livre, em inglês, pensada para logs e depuração — não há garantia de que seja voltada ao usuário final nem localizada.
- `fields` só é preenchido em erros de validação (`errorCode: "VALIDATION_FAILED"`), mapeando o nome do campo para a falha de validação específica.

Cada exceção de domínio implementa `ApiException` (`common/error/ApiException.java`), expondo seu próprio `errorCode`; os handlers `@RestControllerAdvice` de `auth` e `shipment` montam a resposta a partir dele, mantendo o código do erro junto da exceção que é dona dele.

| errorCode | Status HTTP | Módulo |
|---|---|---|
| `INVALID_CREDENTIALS` | 401 | auth |
| `EMAIL_ALREADY_REGISTERED` | 409 | auth |
| `USER_NOT_FOUND` | 404 | auth |
| `ACCOUNT_DEACTIVATED` | 403 | auth |
| `SELF_DELETION_NOT_ALLOWED` | 409 | auth |
| `SHIPMENT_NOT_FOUND` | 404 | shipment |
| `VALIDATION_FAILED` | 400 | shipment (bean validation) |

## Internacionalização

A aplicação suporta português (padrão), inglês e espanhol, alternáveis em runtime sem recarregar a página.

**Abordagem:** um `TranslationService` próprio, baseado em signal (`frontend/src/app/core/translation.service.ts`), em vez de `@angular/localize` ou `ngx-translate`. `@angular/localize` foi descartado porque compila um build separado por idioma, o que não suporta um toggle em runtime. `ngx-translate` foi considerado, mas deixado de lado em favor de construir o mecanismo na mão, consistente com a abordagem geral deste projeto.

**Como funciona:**
- Três dicionários JSON ficam em `frontend/public/i18n/` (`pt.json`, `en.json`, `es.json`), carregados uma única vez na inicialização da aplicação via `provideAppInitializer`, antes da app renderizar.
- O idioma ativo fica guardado num `signal`, persistido no `localStorage` (`track_flow_lang`), e lido de forma síncrona na inicialização para evitar qualquer flash do idioma errado — o mesmo padrão usado na restauração da sessão JWT.
- Templates resolvem traduções através de uma pipe impura, `TranslatePipe` (`{{ 'key.path' | translate }}`, com interpolação opcional via `{{param}}`); código fora de template (ex: montar o payload de um dialog de confirmação) chama `TranslationService.t()` diretamente.
- Um toggle de idioma (PT / EN / ES) fica na sidebar do app shell.

**Valores de domínio vs. texto de exibição:** valores de status (`CREATED`, `IN_TRANSIT`, ...) e papéis de usuário (`ADMIN`, `OPS`) são traduzidos apenas para exibição, através de uma camada de mapeamento por chave (`shipment.status.*`, `auth.role.*`). O valor real enviado e recebido do backend nunca é traduzido — só o label mostrado ao usuário muda conforme o idioma selecionado.

**Mensagens de erro do backend:** como descrito em [Respostas de Erro](#respostas-de-erro) acima, erros da API incluem um `errorCode` estável. Os effects do NgRx no frontend extraem esse código via `extractErrorCode()` (`frontend/src/app/core/http-error.utils.ts`) e o guardam no state em vez da mensagem crua do HTTP; os templates resolvem esse código para uma string localizada através das chaves do dicionário `errors.*`, caindo em `errors.UNKNOWN_ERROR` para qualquer coisa fora do formato `ErrorResponse` do próprio backend (falhas de rede, proxies inesperados, etc.).

## Testes

- **Backend:** `cd backend && ./mvnw verify` — roda a suíte de testes de integração completa contra containers PostgreSQL efêmeros via Testcontainers, incluindo um teste de WebSocket ponta a ponta real (abre uma sessão STOMP e verifica que um evento transmitido é recebido).
- **Frontend:** ainda não há suíte de testes automatizada — um próximo passo natural para este projeto. Até lá, mudanças de i18n e de código de erro são verificadas manualmente: alterne os idiomas pelo toggle da sidebar e percorra cada tela, e dispare cada `errorCode` da tabela acima (ex: senha errada, autoexclusão, um código de rastreio inexistente) para confirmar que a mensagem localizada aparece em vez do código cru ou de um texto em inglês desatualizado.

## Licença

Projeto pessoal de estudo. Sem restrições de licença — sinta-se à vontade para usar como referência.
