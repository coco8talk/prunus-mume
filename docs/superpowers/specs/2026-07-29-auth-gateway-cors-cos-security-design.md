# Auth、Gateway、CORS 与 COS 密钥治理设计

## 目标

解决以下四个已确认的联调阻塞与安全问题：

1. `pm-auth` 缺少明确端口和专属 Nacos 配置。
2. 仓库缺少统一 Gateway，两个前端无法通过单一 API Base 完成跨服务调用。
3. 管理端实际运行于 3001，但当前 CORS 只允许 3000。
4. 文件存储模块包含已提交到 Git 历史的明文腾讯 COS 凭据。

本设计不执行真实支付宝调用，不升级既有框架版本，不重构无关业务逻辑，不向 Gateway 暴露内部 Feign 接口。

## 已确认决策

- `pm-auth` 使用端口 `8081`。
- 新增 `pm-gateway`，使用端口 `8082`。
- Client 与 Admin 的本地 API Base 统一为 `http://localhost:8082/api`。
- 允许重写本地 Git 历史以清除敏感文件；不 force-push 远端。
- 腾讯云旧密钥的吊销和新密钥创建必须由配置所有者在腾讯云完成，本地实现不得声称已经完成云端轮换。

## 方案选择

采用独立的 Spring Cloud Gateway WebFlux 模块和显式路由表。

未采用 Discovery Locator，因为它会让外部 URL 耦合服务名，并需要额外路径重写。未采用两个前端各自代理，因为它会复制路由事实且不能形成生产可用的统一认证入口。

## 组件设计

### pm-auth

`pm-auth/src/main/resources/application.yml` 继续声明服务名、Nacos 地址和配置导入，并新增 `pm-auth.yaml` 导入。`pm-auth.yaml` 是 auth 运行配置的事实源，包含：

- `server.port: 8081`
- Redis host、port、database
- Redis 密码使用环境变量占位符，不写入明文

auth 不引入数据库。登录和注册继续通过现有 `UserApi` Feign 契约调用 `pm-user`，会话继续使用 Sa-Token 与 Redis。

### pm-gateway

新增根 Maven 模块 `pm-gateway`，依赖：

- `spring-cloud-starter-gateway-server-webflux`
- `spring-cloud-starter-loadbalancer`
- `spring-cloud-starter-alibaba-nacos-discovery`
- `spring-cloud-starter-alibaba-nacos-config`

Gateway 不依赖 `pm-platform`，避免把 Spring MVC 配置带入 WebFlux 应用。`pm-gateway` 的本地 `application.yml` 只声明应用名、Nacos 地址和 `pm-gateway.yaml` 导入；`pm-gateway.yaml` 是端口、路由与 CORS 的事实源。

请求保留完整 `/api` 前缀转发，下游服务继续使用公共 `/api` context-path，不使用 `StripPrefix`。

### 显式路由

| Gateway Path | URI |
| --- | --- |
| `/api/auth/**` | `lb://pm-auth` |
| `/api/users/**` | `lb://pm-user` |
| `/api/questions/**` | `lb://pm-question` |
| `/api/question-reviews/**` | `lb://pm-question` |
| `/api/question-banks/**` | `lb://pm-question` |
| `/api/question-bank-relations/**` | `lb://pm-question` |
| `/api/covers/**` | `lb://pm-question` |
| `/api/thumbs/**` | `lb://pm-interaction` |
| `/api/favourites/**` | `lb://pm-interaction` |
| `/api/sign-ins/**` | `lb://pm-interaction` |
| `/api/membership-orders/**` | `lb://pm-payment` |
| `/api/alipay/**` | `lb://pm-payment` |
| `/api/avatars/**` | `lb://pm-file-storage` |

`/api/internal/**` 不配置 Gateway route。服务内部调用继续通过 Nacos 和 Feign 直连。

Gateway 不增加 fallback、熔断、重试或自定义错误包装。服务不可用时保留 Gateway 的标准 503 行为。

### CORS

Gateway 统一允许以下本地开发来源：

- `http://localhost:3000`
- `http://127.0.0.1:3000`
- `http://localhost:3001`
- `http://127.0.0.1:3001`

允许方法为 GET、POST、PUT、DELETE、OPTIONS，允许任意请求头，允许 credentials，并暴露 `satoken` 响应头。OPTIONS 使用 simple URL handler mapping，避免预检因 route method predicate 不匹配而失败。

下游服务继续保留现有 MVC CORS 支持，并从公共配置读取同一来源集合。Gateway 使用 `DedupeResponseHeader` 去重 `Access-Control-Allow-Origin`、`Access-Control-Allow-Credentials` 和 `Access-Control-Expose-Headers`。

### 前端

Client 与 Admin 的本地环境配置均设置：

```dotenv
NEXT_PUBLIC_API_BASE_URL=http://localhost:8082/api
```

Client 现有运行时配置继续支持 `API_BASE_URL` 覆盖。Admin 继续使用 `NEXT_PUBLIC_API_BASE_URL`。不在 TypeScript 代码中增加端口默认值。

### COS 密钥治理

删除 `pm-file-storage/src/main/resources/application.yaml` 中的明文凭据。文件存储运行配置继续从 Nacos 的环境变量占位符读取：

- `TENCENT_COS_SECRET_ID`
- `TENCENT_COS_SECRET_KEY`
- `TENCENT_COS_APP_ID`
- `TENCENT_COS_REGION`
- `TENCENT_COS_BUCKET_NAME`

提交无密钥示例文件和安全说明，所有示例值为空。增加自动扫描，拒绝资源文件中的腾讯密钥 ID 格式和非空 COS secret 值。

历史清理流程：

1. 记录 remote、HEAD、工作树状态。
2. 保存所有 tracked 和 untracked 用户改动，排除目标敏感文件。
3. 使用历史过滤工具从所有本地 refs 删除敏感文件路径。
4. 恢复用户工作树改动和 remote 配置。
5. 删除过滤工具备份 refs，立即过期 reflog，并清理不可达对象。
6. 验证所有本地 refs 不再包含敏感路径或可达敏感 blob。

不会 force-push。若敏感提交已存在于远端，远端仍需协调历史重写和强制更新。

## 测试设计

### 自动化红绿测试

- Gateway 配置测试：应用能加载 13 个批准的公开路径模式，且没有 `/api/internal/**` route。
- Gateway CORS 测试：3000 与 3001 预检成功，未知来源被拒绝，响应暴露 `satoken`。
- Auth 配置测试：Nacos 配置导入存在，端口为 8081，Redis 密码为占位符。
- Web MVC CORS 测试：公共服务允许 3000/3001 并暴露 `satoken`。
- 前端契约测试：两个前端的 API Base 指向 Gateway，不再为空。
- 密钥扫描测试：当前工作树不存在腾讯密钥 ID 或非空 COS secret。

每个生产修改均先运行对应失败测试，确认失败原因是缺失能力，再做最小实现并转绿。

### 真实运行验证

按 Nacos、Redis、MySQL、业务服务、auth、Gateway、前端顺序启动。验证：

- Nacos 中 `pm-auth:8081` 与 `pm-gateway:8082` healthy、enabled。
- Gateway 路由可访问 user、question、interaction、payment 静态入口和 file-storage 鉴权入口。
- Client 3000 与 Admin 3001 的 CORS 预检均返回单值允许来源、credentials 和 `satoken` 暴露。
- Client 和 Admin 均可通过 Gateway 登录并读取 `satoken`。
- 登录后通过 Gateway 查询当前用户、题库、题目、点赞/收藏状态。
- 使用唯一联调数据执行创建、查询、修改、删除，并回归相邻查询接口。
- 支付接口仅检查 route、Controller、Bean 和既有单测，不发起支付宝网络调用。

### 完成标准

- Maven reactor 全量测试通过。
- Client 与 Admin 的 test、lint、build 全部通过。
- Gateway 浏览器登录与至少一条跨服务 CRUD 主流程通过。
- 当前工作树不含明文 COS 凭据。
- 本地 Git 历史所有可达 refs 不含敏感文件路径或敏感 blob。
- 报告明确区分“本地历史已清理”与“腾讯云密钥/远端历史仍需所有者操作”。

## 风险与边界

- Git 历史重写会改变 commit SHA；实施前必须保全当前大规模未提交改动。
- Gateway 为 WebFlux 应用，不能复用 `pm-platform` 的 MVC 配置。
- 允许 credentials 时不能使用通配符来源。
- 删除明文凭据后，涉及 COS 的真实功能只有在外部注入新密钥后才能运行；本次不会使用或验证旧密钥。
- 不修改依赖版本，只使用现有 Spring Cloud BOM 管理的新依赖版本。
