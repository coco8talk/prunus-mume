import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import test from "node:test";

async function render(pathname) {
  const workerUrl = new URL("../dist/server/index.js", import.meta.url);
  workerUrl.searchParams.set(
    "test",
    `${pathname}-${process.pid}-${Date.now()}`,
  );
  const { default: worker } = await import(workerUrl.href);

  return worker.fetch(
    new Request(`http://localhost${pathname}`, {
      headers: { accept: "text/html" },
    }),
    {
      ASSETS: {
        fetch: async () => new Response("Not found", { status: 404 }),
      },
    },
    {
      waitUntil() {},
      passThroughOnException() {},
    },
  );
}

test("server-renders the administrator login", async () => {
  const response = await render("/login");
  assert.equal(response.status, 200);
  assert.match(response.headers.get("content-type") ?? "", /^text\/html\b/i);

  const html = await response.text();
  assert.match(html, /<title>Sign in · Prunus Mume Admin<\/title>/i);
  assert.match(html, /Administrator access/);
  assert.match(html, /Enter admin console/);
  assert.match(html, /userRole = 0/);
  assert.doesNotMatch(html, /codex-preview|Your site is taking shape/i);
});

test("server-renders protected user management metadata and loading state", async () => {
  const response = await render("/users");
  assert.equal(response.status, 200);

  const html = await response.text();
  assert.match(html, /<title>Users · Prunus Mume Admin<\/title>/i);
  assert.match(html, /http:\/\/localhost(?::3000)?\/og-admin\.png/);
  assert.match(html, /Preparing your operations workspace/);
});

test("server-renders question-bank routes and metadata", async () => {
  const [banksResponse, questionsResponse] = await Promise.all([
    render("/question-banks"),
    render("/question-banks/194501/questions"),
  ]);
  assert.equal(banksResponse.status, 200);
  assert.equal(questionsResponse.status, 200);

  const [banksHtml, questionsHtml] = await Promise.all([
    banksResponse.text(),
    questionsResponse.text(),
  ]);
  assert.match(
    banksHtml,
    /<title>Question banks · Prunus Mume Admin<\/title>/i,
  );
  assert.match(
    questionsHtml,
    /<title>Bank contents · Prunus Mume Admin<\/title>/i,
  );
  assert.match(questionsHtml, /Preparing your operations workspace/);
});

test("keeps API and role contracts explicit", async () => {
  const [api, auth, users, banks, contents] = await Promise.all([
    readFile(new URL("../app/lib/api.ts", import.meta.url), "utf8"),
    readFile(
      new URL("../app/components/AuthProvider.tsx", import.meta.url),
      "utf8",
    ),
    readFile(
      new URL("../app/components/UserManagement.tsx", import.meta.url),
      "utf8",
    ),
    readFile(
      new URL("../app/components/QuestionBankManagement.tsx", import.meta.url),
      "utf8",
    ),
    readFile(
      new URL("../app/components/BankQuestions.tsx", import.meta.url),
      "utf8",
    ),
  ]);

  assert.match(api, /NEXT_PUBLIC_API_BASE_URL/);
  assert.match(api, /headers\.set\("satoken", token\)/);
  assert.match(api, /credentials: "include"/);
  assert.match(api, /envelope\.code < 200 \|\| envelope\.code >= 300/);
  assert.match(auth, /response\.data\.userRole !== 0/);
  assert.match(auth, /"\/auth\/login"/);
  assert.match(auth, /"\/auth\/logout"/);
  assert.match(users, /"\/user\/admin\/search"/);
  assert.match(users, /"\/user\/admin"/);
  assert.match(users, /method: "DELETE"/);
  assert.match(users, /method: "PUT"/);
  assert.match(banks, /"\/question-banks\/admin\/search"/);
  assert.match(banks, /"\/covers\/question-bank\/batch"/);
  assert.match(contents, /\/question-bank-relations\/banks\//);
  assert.match(contents, /method: "DELETE"/);
  assert.match(contents, /method: "POST"/);
  for (const source of [users, banks, contents]) {
    assert.doesNotMatch(source, /["`]\/(?:internal|orders|payments)/i);
  }
});
