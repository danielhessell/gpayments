# GPayments Gateway — Frontend

Painel web do Gateway de Pagamentos GPayments, construído em Next.js 16 (App Router).

## Sobre o projeto

O Gateway de Pagamentos é um sistema distribuído composto por:

- **Frontend em Next.js** (este repositório)
- **API Gateway** em Kotlin/Spring Boot ([`gpayments-gateway`](../gpayments-gateway))
- Apache Kafka para processamento assíncrono de transações de alto valor

## Stack

- Next.js 16 (App Router, Turbopack, Server Actions, Server Components)
- React 19.2
- TypeScript
- Tailwind CSS v4
- shadcn/ui (Base UI)

## Pré-requisitos

- Node.js 20.9+
- A API do gateway (`gpayments-gateway`) em execução — veja o README daquele projeto. Por padrão ela sobe em `http://localhost:8080`.

## Configuração

```bash
cp .env.example .env.local
```

`API_URL` aponta para a API do gateway (`http://localhost:8080` por padrão). É uma variável apenas de servidor — nunca é exposta ao navegador.

## Desenvolvimento

```bash
npm install
npm run dev
```

A aplicação sobe em `http://localhost:3000`.

## Build de produção

```bash
npm run build
npm run start
```

## Docker

```bash
docker compose up --build
```

O container do frontend acessa a API do host via `API_URL=http://host.docker.internal:8080` (configurável). O backend não faz parte deste `docker-compose.yaml` — suba-o separadamente.

## Funcionalidades

### Autenticação
- Login via API Key do comerciante (`X-API-Key`)
- Rotas de `/invoices` protegidas por proxy (`src/proxy.ts`)

### Faturas
- Listagem com resumo (total aprovado, pendentes, total de faturas)
- Detalhes de uma fatura específica
- Criação de fatura (processamento de pagamento) com pré-visualização do cartão

### Regras de negócio
- Transações acima de R$ 10.000 são enviadas para análise e ficam com status `pending`
- Transações menores são processadas de forma síncrona (aprovada/rejeitada)

## Estrutura

```
src/
  app/
    (auth)/login/        # tela de autenticação
    (app)/invoices/       # painel autenticado (faturas)
  components/            # componentes de UI e de domínio
  components/ui/         # shadcn/ui
  lib/                   # api client, tipos e formatação
  proxy.ts               # proteção de rotas autenticadas
```
