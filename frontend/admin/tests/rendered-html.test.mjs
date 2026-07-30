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
  assert.match(html, /http:\/\/localhost(?::3000)?\/og-questions\.png/);
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

test("server-renders question management metadata and loading state", async () => {
  const response = await render("/questions");
  assert.equal(response.status, 200);

  const html = await response.text();
  assert.match(html, /<title>Questions · Prunus Mume Admin<\/title>/i);
  assert.match(html, /Preparing your operations workspace/);
});

test("server-renders review routes and metadata", async () => {
  const [queueResponse, historyResponse] = await Promise.all([
    render("/reviews/pending"),
    render("/reviews/history"),
  ]);
  assert.equal(queueResponse.status, 200);
  assert.equal(historyResponse.status, 200);

  const [queueHtml, historyHtml] = await Promise.all([
    queueResponse.text(),
    historyResponse.text(),
  ]);
  assert.match(
    queueHtml,
    /<title>Review queue · Prunus Mume Admin<\/title>/i,
  );
  assert.match(
    historyHtml,
    /<title>Review history · Prunus Mume Admin<\/title>/i,
  );
  assert.match(queueHtml, /Preparing your operations workspace/);
  assert.match(historyHtml, /Preparing your operations workspace/);
});

test("keeps API and role contracts explicit", async () => {
  const [api, auth, users, banks, contents, questions, queue, history] =
    await Promise.all([
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
    readFile(
      new URL("../app/components/QuestionManagement.tsx", import.meta.url),
      "utf8",
    ),
    readFile(
      new URL("../app/components/ReviewQueue.tsx", import.meta.url),
      "utf8",
    ),
    readFile(
      new URL("../app/components/ReviewHistory.tsx", import.meta.url),
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
  assert.match(users, /"\/users\/search"/);
  assert.match(users, /"\/users"/);
  assert.match(users, /method: "DELETE"/);
  assert.match(users, /method: "PUT"/);
  assert.match(banks, /"\/question-banks\/search"/);
  assert.match(banks, /"\/covers\/question-banks\/batch"/);
  assert.match(contents, /\/question-bank-relations\/banks\//);
  assert.match(contents, /method: "DELETE"/);
  assert.match(contents, /method: "POST"/);
  assert.match(questions, /"\/questions\/search"/);
  assert.match(questions, /"\/questions\/batch"/);
  assert.match(questions, /method: "DELETE"/);
  assert.match(questions, /method: "PUT"/);
  assert.match(queue, /"\/question-reviews\/pending\/count"/);
  assert.match(queue, /`\/question-reviews\/pending\?current=\$\{page\}&pageSize=\$\{pageSize\}`/);
  assert.match(queue, /"\/question-reviews"/);
  assert.match(queue, /reviewMessage/);
  assert.match(history, /"\/question-reviews\/search"/);
  assert.match(history, /`\/question-reviews\/questions\/\$\{encodeURIComponent\(questionId\)\}`/);
  for (const source of [users, banks, contents, questions, queue, history]) {
    assert.doesNotMatch(source, /["`]\/(?:internal|orders|payments)/i);
  }
  for (const source of [banks, questions]) {
    assert.doesNotMatch(source, /\/(?:question-banks|questions)\/admin/);
  }
  assert.doesNotMatch(banks, /\/covers\/question-bank\//);
});
