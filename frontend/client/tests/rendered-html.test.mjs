import assert from "node:assert/strict";
import test from "node:test";

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

const routes = [
  ["/", "把复杂知识"],
  ["/login", "登录梅问"],
  ["/register", "注册梅问"],
  ["/banks", "找到下一组值得练习的题"],
  ["/banks/frontend", "现代前端核心题库"],
  ["/questions", "把模糊的理解"],
  ["/questions/q101", "JavaScript 闭包解决了什么问题"],
  ["/questions/q106", "会员专享解析"],
];

for (const [pathname, expectedCopy] of routes) {
  test(`server-renders ${pathname}`, async () => {
    const response = await render(pathname);
    assert.equal(response.status, 200);
    assert.match(response.headers.get("content-type") ?? "", /^text\/html\b/i);
    const html = await response.text();
    assert.match(html, new RegExp(expectedCopy));
    assert.match(html, /梅问/);
  });
}

test("renders real navigation and no placeholder links", async () => {
  const response = await render("/");
  const html = await response.text();

  assert.match(html, /href="\/banks"/);
  assert.match(html, /href="\/questions"/);
  assert.match(html, /href="\/favorites"/);
  assert.match(html, /href="\/questions\/q101"/);
  assert.doesNotMatch(html, /href="#"/);
  assert.doesNotMatch(html, /Your site is taking shape|Building your site/);
});

test("keeps public answers collapsed and VIP answers out of the document", async () => {
  const publicResponse = await render("/questions/q101");
  const publicHtml = await publicResponse.text();
  assert.match(publicHtml, /显示答案/);
  assert.doesNotMatch(publicHtml, /闭包是函数与其词法环境的组合/);

  const vipResponse = await render("/questions/q106");
  const vipHtml = await vipResponse.text();
  assert.match(vipHtml, /vip-answer-gate/);
  assert.match(vipHtml, /了解会员/);
  assert.doesNotMatch(vipHtml, /连接层通过网关集群维护长连接/);
});
