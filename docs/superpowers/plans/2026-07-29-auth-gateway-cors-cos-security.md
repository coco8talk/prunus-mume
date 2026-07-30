# Auth、Gateway、CORS 与 COS 密钥治理 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为项目增加可运行的认证与统一 Gateway 入口，允许 Client 3000 和 Admin 3001 完成浏览器跨服务调用，并从当前工作树和本地 Git 历史清除明文 COS 凭据。

**Architecture:** `pm-auth:8081` 与新 `pm-gateway:8082` 都通过 Nacos 注册和取配置；Gateway 使用 WebFlux、显式 `lb://` 路由并保留 `/api` 前缀。两个前端只面向 Gateway。CORS 来源由配置声明，COS 凭据只允许通过环境变量注入；代码与运行验证完成后再重写本地历史。

**Tech Stack:** Java 21、Spring Boot 3.5.16、Spring Cloud 2025.0.3、Spring Cloud Gateway WebFlux、Spring Cloud Alibaba Nacos 2025.0.0.0、Sa-Token 1.43.0、Redis、Vinext/Vite、Node.js 22、Maven、Git filter-branch。

## Global Constraints

- `pm-auth` 使用端口 `8081`；`pm-gateway` 使用端口 `8082`。
- Client 与 Admin 的本地 API Base 必须为 `http://localhost:8082/api`。
- Gateway 必须保留 `/api` 前缀，不得使用 `StripPrefix`。
- `/api/internal/**` 不得通过 Gateway 暴露。
- 只使用现有 BOM 管理的依赖版本，不新增显式版本、不升级框架版本。
- CORS 必须允许 localhost/127.0.0.1 的 3000 与 3001，允许 credentials，并暴露 `satoken`。
- 不执行支付宝真实请求。
- 不在仓库、测试、日志、报告或 subagent 消息中输出 COS 明文凭据。
- 历史重写只作用于本地仓库；不得 force-push 远端。
- 保护当前工作树中已有的 tracked、untracked 与删除状态，不得覆盖或清理无关改动。
- 每个生产改动先运行能证明缺陷的失败测试，再做最小实现。

---

### Task 1: 配置化公共 CORS 与 pm-auth

**Files:**
- Create: `config/nacos/pm-common.yaml`
- Create: `config/nacos/pm-auth.yaml`
- Create: `pm-auth/src/test/java/com/coco8talk/pm/authserver/config/AuthConfigurationTest.java`
- Modify: `pm-auth/pom.xml`
- Modify: `pm-auth/src/main/resources/application.yml`
- Modify: `pm-platform/src/main/java/com/coco8talk/pm/platform/config/WebConfig.java`
- Modify: `pm-question/src/test/java/com/coco8talk/pm/question/config/WebConfigTest.java`

**Interfaces:**
- Produces Nacos DataId `pm-common.yaml` with `coco8talk.web.allowed-origins`.
- Produces Nacos DataId `pm-auth.yaml` with `server.port=8081` and Redis host/port/database.
- Produces `WebConfig(List<String> allowedOrigins)` behavior through Spring property injection.
- Gateway Task 2 consumes the same four allowed origins.

- [ ] **Step 1: Extend the MVC CORS regression test before changing production**

Add a second test to `WebConfigTest`:

```java
@Test
void corsShouldAllowAdminDevelopmentOrigin() throws Exception {
    mockMvc.perform(options("/probe")
                    .header(ORIGIN, "http://localhost:3001")
                    .header(ACCESS_CONTROL_REQUEST_METHOD, "GET"))
            .andExpect(status().isOk())
            .andExpect(header().string(ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3001"))
            .andExpect(header().string(ACCESS_CONTROL_EXPOSE_HEADERS, "satoken"));
}
```

Supply the four configured origins to the test Spring context with:

```java
@TestPropertySource(properties = {
        "coco8talk.web.allowed-origins=http://localhost:3000,http://127.0.0.1:3000,http://localhost:3001,http://127.0.0.1:3001"
})
```

- [ ] **Step 2: Run the CORS test and verify RED**

Run:

```bash
mvn -pl pm-question -am -Dtest=WebConfigTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: the 3001 preflight receives 403 because production `WebConfig` only allows port 3000.

- [ ] **Step 3: Add the auth configuration contract test**

Add test dependency to `pm-auth/pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

`AuthConfigurationTest` must use `YamlPropertySourceLoader` to load `application.yml` and the repository file `config/nacos/pm-auth.yaml`, then assert:

```java
assertThat(application.getProperty("spring.config.import[1]"))
        .asString()
        .startsWith("nacos:pm-auth.yaml");
assertThat(authConfig.getProperty("server.port")).isEqualTo(8081);
assertThat(authConfig.getProperty("spring.data.redis.host")).isNotNull();
assertThat(authConfig.getProperty("spring.data.redis.port")).isEqualTo(6379);
```

The test locates the repository root through `System.getProperty("maven.multiModuleProjectDirectory")`. It must also read the raw YAML and assert it contains `${coco8talk.redis.host:localhost}` and does not contain a literal Redis password.

- [ ] **Step 4: Run the auth test and verify RED**

Run:

```bash
mvn -pl pm-auth -am -Dtest=AuthConfigurationTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: compile or assertion failure because the test/config file/import does not yet exist.

- [ ] **Step 5: Implement the minimum configuration**

Create `config/nacos/pm-common.yaml` with the currently published non-secret settings plus the configured origins:

```yaml
server:
  servlet:
    context-path: /api

mybatis-plus:
  mapper-locations: classpath*:mapper/*.xml
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      logic-delete-value: 1
      logic-not-delete-value: 0

sa-token:
  token-name: satoken
  timeout: 2592000
  active-timeout: 1800
  is-concurrent: true
  is-share: true

coco8talk:
  web:
    allowed-origins:
      - http://localhost:3000
      - http://127.0.0.1:3000
      - http://localhost:3001
      - http://127.0.0.1:3001
```

Create `config/nacos/pm-auth.yaml`:

```yaml
spring:
  data:
    redis:
      host: ${coco8talk.redis.host:localhost}
      port: ${coco8talk.redis.port:6379}
      database: ${coco8talk.redis.database:0}

server:
  port: 8081
```

Append to `pm-auth/src/main/resources/application.yml`:

```yaml
      - nacos:pm-auth.yaml?group=${NACOS_GROUP:DEFAULT_GROUP}&refreshEnabled=true
  data:
    redis:
      password: ${coco8talk.redis.password:}
```

Change `WebConfig` to receive:

```java
@Value("${coco8talk.web.allowed-origins}")
List<String> allowedOrigins
```

and call:

```java
.allowedOrigins(allowedOrigins.toArray(String[]::new))
```

Keep existing methods, headers, credentials, maxAge and `exposedHeaders("satoken")` unchanged.

- [ ] **Step 6: Run targeted and adjacent tests**

Run:

```bash
mvn -pl pm-auth,pm-question -am -Dtest=AuthConfigurationTest,WebConfigTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: all targeted tests pass.

- [ ] **Step 7: Commit only Task 1 files**

```bash
git add config/nacos/pm-common.yaml config/nacos/pm-auth.yaml \
  pm-auth/pom.xml pm-auth/src/main/resources/application.yml \
  pm-auth/src/test/java/com/coco8talk/pm/authserver/config/AuthConfigurationTest.java \
  pm-platform/src/main/java/com/coco8talk/pm/platform/config/WebConfig.java \
  pm-question/src/test/java/com/coco8talk/pm/question/config/WebConfigTest.java
git commit -m "feat: configure auth and shared cors origins"
```

---

### Task 2: 新增显式路由的 pm-gateway

**Files:**
- Create: `pm-gateway/pom.xml`
- Create: `pm-gateway/src/main/java/com/coco8talk/pm/gateway/PmGatewayApplication.java`
- Create: `pm-gateway/src/main/resources/application.yml`
- Create: `pm-gateway/src/test/java/com/coco8talk/pm/gateway/config/GatewayConfigurationTest.java`
- Create: `config/nacos/pm-gateway.yaml`
- Modify: `pom.xml`

**Interfaces:**
- Consumes service IDs `pm-auth`, `pm-user`, `pm-question`, `pm-interaction`, `pm-payment`, `pm-file-storage`.
- Consumes public API paths listed in the design spec.
- Produces `http://localhost:8082/api` as the single browser API entry.

- [ ] **Step 1: Add the failing Gateway configuration test first**

Create `GatewayConfigurationTest` with `YamlPropertySourceLoader`. Load repository file `config/nacos/pm-gateway.yaml` and assert:

```java
assertThat(properties.getProperty("server.port")).isEqualTo(8082);
assertThat(routeIds).containsExactlyInAnyOrder(
        "auth", "user", "question", "interaction", "payment", "file-storage");
assertThat(routeUris).contains(
        "lb://pm-auth", "lb://pm-user", "lb://pm-question",
        "lb://pm-interaction", "lb://pm-payment", "lb://pm-file-storage");
assertThat(allPathPredicates).noneMatch(path -> path.contains("/api/internal/"));
assertThat(allowedOrigins).contains(
        "http://localhost:3000", "http://127.0.0.1:3000",
        "http://localhost:3001", "http://127.0.0.1:3001");
assertThat(exposedHeaders).contains("satoken");
```

Also assert `spring.cloud.gateway.server.webflux.globalcors.add-to-simple-url-handler-mapping=true` and the default filter contains `DedupeResponseHeader`.

- [ ] **Step 2: Run the Gateway test and verify RED**

Run:

```bash
mvn -pl pm-gateway -am -Dtest=GatewayConfigurationTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: Maven fails because `pm-gateway` is not a reactor module.

- [ ] **Step 3: Create the Gateway module**

Add `<module>pm-gateway</module>` after `pm-auth` in the root `pom.xml`.

`pm-gateway/pom.xml` must depend on:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway-server-webflux</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

Do not add `spring-boot-starter-web` or `pm-platform`.

Create the application entry:

```java
@SpringBootApplication
public class PmGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(PmGatewayApplication.class, args);
    }
}
```

`application.yml` declares `spring.application.name: pm-gateway`, the same Nacos address/namespace pattern as other services, and imports only `nacos:pm-gateway.yaml?...`.

- [ ] **Step 4: Add exact explicit route and CORS configuration**

Create `config/nacos/pm-gateway.yaml` with `server.port: 8082` and exactly six route entries:

```yaml
spring:
  cloud:
    gateway:
      server:
        webflux:
          default-filters:
            - DedupeResponseHeader=Access-Control-Allow-Credentials Access-Control-Allow-Origin Access-Control-Expose-Headers, RETAIN_UNIQUE
          globalcors:
            add-to-simple-url-handler-mapping: true
            cors-configurations:
              '[/**]':
                allowedOrigins:
                  - http://localhost:3000
                  - http://127.0.0.1:3000
                  - http://localhost:3001
                  - http://127.0.0.1:3001
                allowedMethods:
                  - GET
                  - POST
                  - PUT
                  - DELETE
                  - OPTIONS
                allowedHeaders:
                  - '*'
                exposedHeaders:
                  - satoken
                allowCredentials: true
                maxAge: 3600
          routes:
            - id: auth
              uri: lb://pm-auth
              predicates:
                - Path=/api/auth/**
            - id: user
              uri: lb://pm-user
              predicates:
                - Path=/api/users/**
            - id: question
              uri: lb://pm-question
              predicates:
                - Path=/api/questions/**,/api/question-reviews/**,/api/question-banks/**,/api/question-bank-relations/**,/api/covers/**
            - id: interaction
              uri: lb://pm-interaction
              predicates:
                - Path=/api/thumbs/**,/api/favourites/**,/api/sign-ins/**
            - id: payment
              uri: lb://pm-payment
              predicates:
                - Path=/api/membership-orders/**,/api/alipay/**
            - id: file-storage
              uri: lb://pm-file-storage
              predicates:
                - Path=/api/avatars/**

server:
  port: 8082
```

- [ ] **Step 5: Run targeted test and module package**

Run:

```bash
mvn -pl pm-gateway -am -Dtest=GatewayConfigurationTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl pm-gateway -am package spring-boot:repackage -DskipTests
```

Expected: test and executable JAR packaging pass.

- [ ] **Step 6: Commit only Task 2 files**

```bash
git add pom.xml config/nacos/pm-gateway.yaml pm-gateway
git commit -m "feat: add explicit Spring Cloud gateway"
```

---

### Task 3: 将两个前端指向 Gateway

**Files:**
- Create: `frontend/client/tests/gateway-config.test.mjs`
- Create: `frontend/admin/tests/gateway-config.test.mjs`
- Modify: `frontend/client/.env.example`
- Modify: `frontend/admin/.env.example`
- Modify: `frontend/client/package.json`
- Modify: `frontend/admin/package.json`
- Runtime-only ignored files: `frontend/client/.env.local`, `frontend/admin/.env.local`

**Interfaces:**
- Consumes Gateway base `http://localhost:8082/api`.
- Existing Client `apiUrl()` and Admin `API_BASE_URL` consume `NEXT_PUBLIC_API_BASE_URL`; no TypeScript API implementation change is required.

- [ ] **Step 1: Add failing environment contract tests**

Each `gateway-config.test.mjs` reads its project `.env.example` and asserts:

```js
assert.match(contents, /^NEXT_PUBLIC_API_BASE_URL=http:\\/\\/localhost:8082\\/api$/m);
assert.doesNotMatch(contents, /^NEXT_PUBLIC_API_BASE_URL=$/m);
```

The Client test also asserts `API_BASE_URL=http://localhost:8082/api`. Update both `package.json` test commands from one explicit test file to:

```json
"test": "npm run build && node --test tests/*.test.mjs"
```

- [ ] **Step 2: Run both tests and verify RED**

Run:

```bash
npm test
```

in `frontend/client`, then in `frontend/admin`.

Expected: gateway config tests fail because the example values are empty.

- [ ] **Step 3: Set the local Gateway base without changing TypeScript defaults**

Set Client `.env.example`:

```dotenv
API_BASE_URL=http://localhost:8082/api
MEMBERSHIP_LEVEL_ID=
NEXT_PUBLIC_API_BASE_URL=http://localhost:8082/api
NEXT_PUBLIC_MEMBERSHIP_LEVEL_ID=
```

Set Admin `.env.example`:

```dotenv
NEXT_PUBLIC_API_BASE_URL=http://localhost:8082/api
```

Create matching ignored `.env.local` files for runtime. Do not commit `.env.local`.

- [ ] **Step 4: Run frontend tests, lint and build**

Run in both frontend projects:

```bash
npm test
npm run lint
npm run build
```

Expected: all commands exit 0.

- [ ] **Step 5: Commit tracked Task 3 files**

```bash
git add frontend/client/.env.example frontend/admin/.env.example \
  frontend/client/package.json frontend/admin/package.json \
  frontend/client/tests/gateway-config.test.mjs \
  frontend/admin/tests/gateway-config.test.mjs
git commit -m "fix: route both frontends through gateway"
```

---

### Task 4: 清除当前工作树中的 COS 明文凭据

**Files:**
- Delete: `pm-file-storage/src/main/resources/application.yaml`
- Create: `pm-file-storage/.env.example`
- Create: `pm-file-storage/src/test/java/com/coco8talk/pm/filestorage/config/CosSecretHygieneTest.java`
- Create: `SECURITY.md`

**Interfaces:**
- Consumes the existing environment placeholders in Nacos `pm-file-storage.yaml`.
- Produces the required runtime variable list without values.
- Task 6 removes the deleted sensitive file from prior commits.

- [ ] **Step 1: Add a secret hygiene test before deleting the file**

`CosSecretHygieneTest` must scan:

- `pm-file-storage/src/main/resources`
- `config/nacos/pm-file-storage.yaml` if present
- `pm-file-storage/.env.example` if present

It must reject:

```java
Pattern.compile("\\bAKID[A-Za-z0-9]{20,}\\b")
Pattern.compile("(?im)^\\s*(?:TENCENT_COS_SECRET_KEY|secretKey)\\s*[:=]\\s*(?!\\$\\{)[^\\s#]+")
```

It must never contain or print the compromised literal. On failure, print only file path and rule name.

- [ ] **Step 2: Run the hygiene test and verify RED**

Run:

```bash
mvn -pl pm-file-storage -am -Dtest=CosSecretHygieneTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: failure identifies `pm-file-storage/src/main/resources/application.yaml` by path and rule, without printing the credential.

- [ ] **Step 3: Remove the secret and document safe injection**

Delete `pm-file-storage/src/main/resources/application.yaml`.

Create `pm-file-storage/.env.example`:

```dotenv
TENCENT_COS_SECRET_ID=
TENCENT_COS_SECRET_KEY=
TENCENT_COS_APP_ID=
TENCENT_COS_REGION=
TENCENT_COS_BUCKET_NAME=
```

Create `SECURITY.md` with:

- the five required environment variable names;
- instruction to revoke the exposed key in Tencent Cloud before creating a replacement;
- explicit statement that `.env.example` contains no secret and real `.env*` files remain ignored;
- statement that local history cleanup does not clean remote clones or GitHub until coordinated history replacement.

Do not include old credential values.

- [ ] **Step 4: Run hygiene and file-storage regression tests**

Run:

```bash
mvn -pl pm-file-storage -am test
```

Expected: all tests pass and no resource contains a literal COS secret.

- [ ] **Step 5: Commit Task 4 files**

```bash
git add SECURITY.md pm-file-storage/.env.example \
  pm-file-storage/src/test/java/com/coco8talk/pm/filestorage/config/CosSecretHygieneTest.java \
  pm-file-storage/src/main/resources/application.yaml
git commit -m "security: remove committed COS credentials"
```

---

### Task 5: 发布配置并完成真实 Gateway 登录与跨服务回归

**Files:**
- Create/update runtime reports only under this plan's SDD workspace.
- No production file is modified unless a reproducible failure first receives a regression test and the fix remains within the approved design.

**Interfaces:**
- Consumes Tasks 1–4.
- Produces healthy `pm-auth:8081`, `pm-gateway:8082`, Client 3000 and Admin 3001 runtime state.

- [ ] **Step 1: Publish tracked Nacos configuration**

Use Nacos v3 client config API to publish the exact contents of:

```text
config/nacos/pm-common.yaml
config/nacos/pm-auth.yaml
config/nacos/pm-gateway.yaml
```

to namespace `public`, group `DEFAULT_GROUP`, matching DataIds. Do not log Redis passwords or any secret.

Immediately GET each DataId back and compare its SHA-256 to the tracked file content.

- [ ] **Step 2: Build all executable JARs**

Run:

```bash
mvn test
mvn install -DskipTests
mvn -pl pm-user,pm-question,pm-interaction,pm-payment,pm-file-storage,pm-auth,pm-gateway \
  -am package spring-boot:repackage -DskipTests
```

Expected: reactor success and executable JARs contain their launcher classes.

- [ ] **Step 3: Replace existing launchd jobs**

Stop only these exact existing labels if present:

```text
com.coco8talk.prunus-mume.pm-user
com.coco8talk.prunus-mume.pm-question
com.coco8talk.prunus-mume.pm-interaction
com.coco8talk.prunus-mume.pm-payment
com.coco8talk.prunus-mume.pm-file-storage
com.coco8talk.prunus-mume.pm-auth
com.coco8talk.prunus-mume.pm-gateway
com.openai.prunus-mume.frontend-client
com.openai.prunus-mume.frontend-admin
```

Start services from the current feature workspace in dependency order: user/question/interaction/payment/file-storage, auth, gateway, then Client and Admin. Inject Redis/MySQL passwords only through process environment without echoing or writing them. Do not inject old COS credentials.

- [ ] **Step 4: Verify registration and route matrix**

Assert Nacos healthy/enabled instances and ports:

```text
pm-auth 8081
pm-gateway 8082
pm-question 8083
pm-interaction 8086
pm-payment 8089
pm-user 8091
pm-file-storage 8092
```

Through `http://localhost:8082`, verify:

```text
POST /api/auth/login
GET  /api/users/me
GET  /api/question-banks/22001
GET  /api/questions/20001
GET  /api/thumbs/questions/20001/count
GET  /api/favourites/questions/20001/status
GET  /api/avatars/credentials?filename=test.png
```

The avatar request may return an auth/business result but must route to file-storage, not 404/503. Payment verification is limited to route existence and existing unit tests.

- [ ] **Step 5: Verify CORS from both frontends**

OPTIONS `/api/auth/login` with origins `http://localhost:3000` and `http://localhost:3001` must return:

```text
Access-Control-Allow-Origin: exact requesting origin
Access-Control-Allow-Credentials: true
Access-Control-Expose-Headers: satoken
```

Each header must have a single deduplicated value. Origin `http://localhost:3999` must not receive an allow-origin header.

- [ ] **Step 6: Execute authenticated CRUD through Gateway**

Use a unique test account or approved existing fixture. Never record the password. Capture the returned `satoken`, then:

1. GET `/api/users/me`.
2. POST `/api/questions` with a unique title/content/answer.
3. GET the returned question ID.
4. PUT `/api/questions/{id}` with an updated title.
5. GET and assert the updated title.
6. DELETE `/api/questions/{id}`.
7. GET and assert the deleted record is no longer available.
8. Re-run adjacent question search and bank detail.

If authorization requires review/admin role, use the seeded admin account without recording credentials. Do not weaken auth or bypass the Gateway.

- [ ] **Step 7: Verify browser login**

With real Chrome, test Client 3000 and Admin 3001:

- form submission reaches Gateway, not the frontend dev server;
- login succeeds;
- `satoken` is visible to frontend code and retained;
- Client can open题库/题目/个人信息;
- Admin can open at least用户或题目管理列表.

Record any exclusions. Do not call payment.

- [ ] **Step 8: Finish as a verification-only task**

Create no commit. If any runtime assertion fails, return `BLOCKED` with the exact request, sanitized response, relevant log lines and failing component boundary. The controller will create a new scoped TDD task rather than allowing unreviewed fixes inside the integration task.

---

### Task 6: 从本地 Git 历史清除敏感文件

**Files:**
- Remove from every local ref: `pm-file-storage/src/main/resources/application.yaml`
- Create: `docs/security/2026-07-29-cos-history-cleanup.md`
- Preserve: all current tracked/untracked user changes and all Task 1–5 commits.

**Interfaces:**
- Consumes Task 4 current-tree deletion and Task 5 green runtime.
- Produces rewritten local refs with no reachable sensitive path/blob.
- Produces `REWRITTEN_BASE` for the task review package because the pre-task SHA is intentionally invalidated.

- [ ] **Step 1: Capture safety metadata without secret output**

Record in the task report:

```bash
git remote -v
git branch --show-current
git rev-parse HEAD
git status --short
git worktree list --porcelain
```

Save the origin URL, current branch, old HEAD and the list of refs. Do not inspect or print blob contents.

- [ ] **Step 2: Preserve the dirty worktree**

Create an external temporary directory with `mktemp -d`. Save:

```bash
git diff --binary HEAD
git diff --cached --binary HEAD
git ls-files --others --exclude-standard -z
```

Archive only the listed untracked files. Exclude the sensitive path explicitly. Verify the patch and archive exist before changing refs.

If a linked implementation worktree exists, complete reviews, return to the primary checkout and remove only this plan's worktree before filter-branch. Never remove unrelated worktrees.

- [ ] **Step 3: Rewrite all local refs**

With a clean checkout and saved remote URL, run:

```bash
FILTER_BRANCH_SQUELCH_WARNING=1 git filter-branch --force \
  --index-filter 'git rm --cached --ignore-unmatch pm-file-storage/src/main/resources/application.yaml' \
  --prune-empty --tag-name-filter cat -- --all
```

Capture the rewritten feature branch HEAD as `REWRITTEN_BASE`. Restore the origin remote if needed. Do not push.

- [ ] **Step 4: Remove backup refs and unreachable objects**

After verifying rewritten refs point to valid commits:

```bash
git for-each-ref --format='delete %(refname)' refs/original | git update-ref --stdin
git reflog expire --expire=now --all
git gc --prune=now
```

This step is authorized by the user specifically for local secret-history removal.

- [ ] **Step 5: Restore the worktree and verify history**

Restore the saved tracked/untracked changes. Then assert:

```bash
test -z "$(git log --all --format=%H -- pm-file-storage/src/main/resources/application.yaml)"
! git rev-list --objects --all | rg 'pm-file-storage/src/main/resources/application.yaml'
```

Scan all reachable text blobs without printing matches; output only pass/fail counts for the key-ID and non-placeholder secret-key patterns from Task 4. Verify `origin` still points to the original URL and no force-push occurred.

- [ ] **Step 6: Document the local-only cleanup**

Create `docs/security/2026-07-29-cos-history-cleanup.md` stating:

- cleanup timestamp;
- removed path;
- verification commands and pass/fail results;
- old/new HEAD identifiers;
- explicit warning that GitHub and other clones remain unchanged;
- explicit instruction to revoke the old Tencent key.

Do not include secret literals.

- [ ] **Step 7: Commit the cleanup record after rewrite**

```bash
git add -f docs/security/2026-07-29-cos-history-cleanup.md
git commit -m "docs: record local COS history cleanup"
```

Report `REWRITTEN_BASE` and the new HEAD. Task review must generate its diff package from `REWRITTEN_BASE..HEAD`, not the invalid pre-rewrite SHA, and must also read the operational verification report.

---

## Final Verification

After all six task reviews are clean:

```bash
mvn test
(cd frontend/client && npm test && npm run lint && npm run build)
(cd frontend/admin && npm test && npm run lint && npm run build)
git diff --check
```

Re-run the Gateway route, CORS, auth login, authenticated CRUD and Nacos health probes. Verify all seven Java services and both frontends remain alive after the launching shell exits. Run the current-tree and reachable-history secret scans again without printing candidate values.

The final report must distinguish:

- locally fixed and verified;
- remote history still requiring coordinated rewrite/force-push;
- Tencent Cloud key still requiring revoke/create by its owner.
