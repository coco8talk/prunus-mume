# Prunus Mume Admin Console

Internal React and TypeScript operations console for Prunus Mume administrators.

## Requirements

- Node.js 22.13 or newer
- A Prunus Mume backend reachable from the browser
- An account with `userRole = 0`

## Configuration

Copy `.env.example` to `.env.local` and set the backend origin:

```bash
NEXT_PUBLIC_API_BASE_URL=http://localhost:8082/api
```

The value may be left empty when the API is served from the same origin.

For cross-origin deployments, the backend must allow credentialed requests,
accept the `satoken` request header, and expose the `satoken` response header.

## Run locally

```bash
npm install
npm run dev
```

Open `http://localhost:3001`.

## Verify

```bash
npm run lint
npm run build
```

The current API scope implements administrator login, logout, role gating,
complete user management, question-bank operations, question management, and
the question-review workflow.
Administrators can filter, sort, page, create, edit, and delete questions and
banks; batch-delete questions; generate individual or missing covers; open a
bank; bulk add or remove approved questions; review pending questions inline;
and search the complete review decision history.

Orders and payments are intentionally excluded because the backend has no
administrator list/search endpoint.
