import assert from "node:assert/strict";
import { existsSync, readFileSync } from "node:fs";
import test from "node:test";
import { fileURLToPath } from "node:url";

const projectRoot = fileURLToPath(new URL("..", import.meta.url));

function source(pathname) {
  return readFileSync(new URL(`../${pathname}`, import.meta.url), "utf8");
}

async function render(pathname = "/") {
  const workerUrl = new URL("../dist/server/index.js", import.meta.url);
  workerUrl.searchParams.set("test", `${process.pid}-${Date.now()}-${pathname}`);
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

const staticRoutes = [
  ["/", "把复杂知识"],
  ["/login", "登录梅问"],
  ["/register", "注册梅问"],
  ["/banks", "找到下一组值得练习的题"],
  ["/questions", "把模糊的理解"],
  ["/membership", "更完整的题解"],
  ["/membership/return", "请以支付宝返回页面为准"],
];

for (const [pathname, expectedCopy] of staticRoutes) {
  test(`server-renders ${pathname}`, async () => {
    const response = await render(pathname);
    assert.equal(response.status, 200);
    assert.match(response.headers.get("content-type") ?? "", /^text\/html\b/i);
    const html = await response.text();
    assert.match(html, new RegExp(expectedCopy));
    assert.match(html, /梅问/);
  });
}

test("data-backed detail routes render safe loading shells without mock records", async () => {
  const bank = await (await render("/banks/42")).text();
  const question = await (await render("/questions/101")).text();
  const profile = await (await render("/users/7")).text();

  assert.match(bank, /正在加载题库/);
  assert.match(question, /正在加载题目/);
  assert.match(profile, /正在加载学习者主页/);
  assert.doesNotMatch(bank + question + profile, /现代前端核心题库|JavaScript 闭包解决了什么问题|林晚/);
});

test("protected routes withhold personal content until session restoration", async () => {
  for (const pathname of ["/me/favourites", "/me/contributions", "/me/sign-in", "/me/profile"]) {
    const html = await (await render(pathname)).text();
    assert.match(html, /正在恢复登录状态/);
    assert.doesNotMatch(html, /linwan@example\.com|138 \*\*\*\* 6721/);
  }
});

test("renders real public navigation and no placeholder links", async () => {
  const html = await (await render("/")).text();

  assert.match(html, /href="\/banks"/);
  assert.match(html, /href="\/questions"/);
  assert.match(html, /href="\/me\/profile"/);
  assert.match(html, /href="\/membership"/);
  assert.doesNotMatch(html, /href="#"/);
  assert.doesNotMatch(html, /Your site is taking shape|Building your site/);
});

test("shared client implements the envelope, configurable host, credentials, and Sa-Token", () => {
  const api = source("app/lib/api.ts");

  assert.match(api, /NEXT_PUBLIC_API_BASE_URL/);
  assert.match(api, /__MEIWEN_RUNTIME_CONFIG__/);
  assert.match(api, /credentials:\s*"include"/);
  assert.match(api, /response\.headers\.get\("satoken"\)/);
  assert.match(api, /\.\.\.\(token \? \{ satoken: token \}/);
  assert.match(api, /SUCCESS_CODES/);
  assert.match(api, /envelope\.message/);
  assert.match(api, /normalizePage/);
});

test("API client unwraps pages, sends credentials, persists tokens, and throws backend messages", async () => {
  const api = await import(new URL(`../app/lib/api.ts?test=${Date.now()}`, import.meta.url));
  const originalFetch = globalThis.fetch;
  const originalWindow = globalThis.window;
  const localValues = new Map();
  const sessionValues = new Map();
  const storage = (values) => ({
    getItem: (key) => values.get(key) ?? null,
    setItem: (key, value) => values.set(key, value),
    removeItem: (key) => values.delete(key),
  });
  globalThis.window = {
    localStorage: storage(localValues),
    sessionStorage: storage(sessionValues),
  };

  let requestInit;
  globalThis.fetch = async (_url, init) => {
    requestInit = init;
    return new Response(JSON.stringify({
      code: 200,
      data: { data: { records: [{ id: 1 }], total: 1, size: 10, current: 2 } },
      message: "ok",
    }), {
      headers: { "Content-Type": "application/json", satoken: "token-123" },
    });
  };

  try {
    const pagePayload = await api.apiRequest("/questions/search", {
      method: "POST",
      body: { current: 2 },
    });
    assert.equal(requestInit.credentials, "include");
    assert.equal(requestInit.headers["Content-Type"], "application/json");
    assert.equal(localValues.get("meiwen.satoken"), "token-123");
    assert.deepEqual(api.normalizePage(pagePayload), {
      records: [{ id: 1 }],
      total: 1,
      size: 10,
      current: 2,
    });

    globalThis.fetch = async () => new Response(JSON.stringify({
      code: 500,
      data: null,
      message: "后端校验失败",
    }), { headers: { "Content-Type": "application/json" } });
    await assert.rejects(api.apiRequest("/questions"), /后端校验失败/);
  } finally {
    globalThis.fetch = originalFetch;
    if (originalWindow === undefined) delete globalThis.window;
    else globalThis.window = originalWindow;
  }
});

test("service layer covers every specified JSON integration and never calls internal routes", () => {
  const services = source("app/lib/services.ts");
  const auth = source("app/components/AuthProvider.tsx");
  const authForm = source("app/components/AuthCard.tsx");
  const combined = `${services}\n${auth}\n${authForm}`;
  const requiredPaths = [
    "/auth/login",
    "/auth/register",
    "/auth/logout",
    "/question-banks/search",
    "/question-banks/",
    "/question-bank-relations/banks/",
    "/questions/search",
    "/questions/",
    "/favourites/my",
    "/favourites/questions/",
    "/thumbs/questions/",
    "/sign-ins",
    "/sign-ins/streak",
    "/sign-ins/today",
    "/sign-ins/monthly-count",
    "/sign-ins/records",
    "/users/me",
    "/users/me/password",
    "/avatars/credentials",
    "/avatars/confirmation",
    "/users/",
    "/membership-orders",
  ];

  for (const path of requiredPaths) assert.match(combined, new RegExp(path.replaceAll("/", "\\/")));
  assert.match(services, /"\/avatars\/confirmation", \{ method: "POST" \}/);
  assert.match(services, /const \{ id, \.\.\.request \} = body/);
  assert.match(services, /`\/questions\/\$\{encodeURIComponent\(id\)\}`/);
  assert.match(services, /if \(value === 2\) return "进阶"/);
  assert.match(services, /if \(value === "进阶"\) return 2/);
  assert.doesNotMatch(services, /"\/user\//);
  assert.doesNotMatch(services, /"\/avatar\//);
  assert.doesNotMatch(combined, /["'`]\/internal\b|Internal[A-Z]/);
});

test("search effects cancel stale requests and reset pagination when filters change", () => {
  const banks = source("app/banks/page.tsx");
  const questions = source("app/questions/page.tsx");

  assert.match(banks, /new AbortController/);
  assert.match(questions, /new AbortController/);
  assert.match(banks, /return \(\) => controller\.abort\(\)/);
  assert.match(questions, /return \(\) => controller\.abort\(\)/);
  assert.match(questions, /setPage\(1\)/);
});

test("question pages keep bank requests within the backend page limit", () => {
  const detail = source("app/questions/[questionId]/page.tsx");
  const questions = source("app/questions/page.tsx");

  assert.match(detail, /bankService\.questions\(bankId, 1, 20, controller\.signal\)/);
  assert.doesNotMatch(detail, /bankService\.questions\(bankId, 1, 100,/);
  assert.match(questions, /bankService\.search\(\{ current: 1, pageSize: 20 \}/);
  assert.doesNotMatch(questions, /bankService\.search\(\{ current: 1, pageSize: 100 \}/);
});

test("VIP answers have no mock fallback and are removed for unauthorized users", () => {
  const detail = source("app/questions/[questionId]/page.tsx");
  const mockPath = `${projectRoot}/app/data/mock.ts`;

  assert.equal(existsSync(mockPath), false);
  assert.match(detail, /safeQuestion\.vip && !activeVip/);
  assert.match(detail, /delete safeQuestion\.answer/);
  assert.match(detail, /href="\/membership"/);
  assert.doesNotMatch(detail, /连接层通过网关集群|闭包是函数与其词法环境/);
});

test("membership uses a real order request and full-page checkout navigation", () => {
  const purchase = source("app/membership/MembershipPurchase.tsx");
  const services = source("app/lib/services.ts");
  const result = source("app/membership/return/page.tsx");

  assert.match(purchase, /membershipLevelId/);
  assert.match(purchase, /membershipService\.submit/);
  assert.match(services, /body: \{ levelId, durationType, durationValue, currency: "CNY" \}/);
  assert.match(purchase, /window\.location\.assign\(apiUrl\(`\/membership-orders\/\$\{encodeURIComponent\(order\.outTradeNo\)\}\/pay-page`\)\)/);
  assert.doesNotMatch(services, /apiRequest<[^>]*>\(`?\/membership-orders\/.*pay-page/);
  assert.match(result, /不会通过站内请求获取或模拟支付页面/);
  assert.doesNotMatch(purchase + result, /模拟订单|演示状态|MW-DEMO/);
});

test("contribution UI exposes the missing list-contract gap instead of mixing mock data", () => {
  const contributions = source("app/me/contributions/page.tsx");

  assert.match(contributions, /questionService\.create/);
  assert.match(contributions, /questionService\.update/);
  assert.match(contributions, /questionService\.remove/);
  assert.match(contributions, /接口规范没有提供历史贡献列表/);
  assert.doesNotMatch(contributions, /initialContributions|Date\.now\(\)/);
});
