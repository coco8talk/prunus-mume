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
  ["/me/favourites", "把重要的问题"],
  ["/me/contributions", "把你的好问题"],
  ["/me/sign-in", "让持续发生"],
  ["/me/profile", "让大家认识"],
  ["/users/lin-wan", "认证贡献者"],
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
  assert.match(html, /href="\/me\/favourites"/);
  assert.match(html, /href="\/me\/contributions"/);
  assert.match(html, /href="\/me\/sign-in"/);
  assert.match(html, /href="\/me\/profile"/);
  assert.match(html, /href="\/questions\/q101"/);
  assert.doesNotMatch(html, /href="#"/);
  assert.doesNotMatch(html, /Your site is taking shape|Building your site/);
});

test("public profile omits private account fields", async () => {
  const response = await render("/users/lin-wan");
  const html = await response.text();

  assert.match(html, /林晚/);
  assert.match(html, /加入于/);
  assert.match(html, /公开题库/);
  assert.doesNotMatch(html, /linwan@example\.com/);
  assert.doesNotMatch(html, /138 \*\*\*\* 6721/);
  assert.doesNotMatch(html, /手机号码|邮箱|账号名/);
});

test("personal routes expose the required local workflows", async () => {
  const favourites = await (await render("/me/favourites")).text();
  const contributions = await (await render("/me/contributions")).text();
  const signIn = await (await render("/me/sign-in")).text();
  const profile = await (await render("/me/profile")).text();

  assert.match(favourites, /移除收藏/);
  assert.match(favourites, /搜索题目或标签/);
  assert.match(contributions, /提交新题/);
  assert.match(contributions, /参考答案/);
  assert.match(signIn, /签到日历/);
  assert.match(signIn, /立即签到/);
  assert.match(profile, /编辑资料/);
  assert.match(profile, /修改密码/);
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
