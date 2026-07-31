# 依赖基线（M0 · T0.1/T0.2）

> 验证方式：`docker pull <image>:<tag>` + `docker image inspect <image>:<tag> --format '{{.Os}}/{{.Architecture}}'`，在本机 OrbStack（`linux/arm64`）上实际拉取并读取 manifest 确认，不是查文档臆测。验证时间：2026-07-30。

## 中间件 + 观测栈镜像（8 个，T0.1）

| 组件 | 镜像 | Tag | 架构确认 | 备注 |
|---|---|---|---|---|
| Nacos | `nacos/nacos-server` | `v3.2.3` | `linux/arm64` ✅ | 单机模式需设 `MODE=standalone`；tag **必须带 `v` 前缀**，`3.2.3`（无 v）不存在 |
| MySQL | `mysql` | `8.4.11` | `linux/arm64` ✅ | 8.4 是当前 LTS 分支，`8.4.11` 是目前最新点版本（2026-07-28 发布）；不要用浮动 tag `8.4` |
| Redis | `redis` | `8.8.1` | `linux/arm64` ✅ | 当前稳定分支最新点版本 |
| Prometheus | `prom/prometheus` | `v3.13.1` | `linux/arm64` ✅ | v3.13 是 LTS 分支的最新 bugfix 版本 |
| Grafana | `grafana/grafana` | `13.1.1` | `linux/arm64` ✅ | tag **不带 `v` 前缀** |
| Loki | `grafana/loki` | `3.7.4` | `linux/arm64` ✅ | tag 不带 `v` 前缀 |
| Promtail | `grafana/promtail` | `3.6.11` | `linux/arm64` ✅ | ⚠️ **Promtail 已于 2026-03-02 EOL**，官方不再更新，冻结在 `3.6.x` 分支（比 Loki 的 `3.7.x` 落后一个大版本是正常的，不是选错）。官方推荐迁移到 **Grafana Alloy**，M0 阶段先按计划用 Promtail，迁移作为后续技术债记录 |
| Alertmanager | `prom/alertmanager` | `v0.33.1` | `linux/arm64` ✅ | — |

**统一原则**：所有 tag 均为精确版本号，不使用 `latest`。

## 待核实项（T0.2 · pm 服务接入）

以下信息来自 `prunus-mume` 仓库一次历史联调记录（`docs/integration-report-2026-07-29.md`），**Nacos 配置随时可能变化，需要你自己在 Nacos 控制台核实后更新本表**：

| 项目 | 已知值 | 来源 | 状态 |
|---|---|---|---|
| pm-user 端口 | `8091` | 历史联调记录 | 需重新核实（Nacos `pm-user.yaml` dataId 的 `server.port`） |
| 全局 context-path | `/api` | 历史联调记录（`pm-common.yaml`） | 需重新核实 |
| pm-auth 端口 | 未知 | 历史联调记录标注"无 `server.port` 事实来源" | **阻塞项**：Nacos 里缺 `pm-auth.yaml` dataId，需先补齐配置才能启动 |
| 其余五个 pm 服务端口 | 未知 | — | 待逐个用同样方法核实并记录 |
| `/chaos/register` 入参格式 | 未知 | `ChaosController.java` 只看到 `ChaosFlagState` 请求体类型 | 待读 `ChaosFlagState.java` 字段后补充示例 |
| MySQL / Redis 账号密码 | 未知（本地配置只有密码占位符 `${coco8talk.datasource.password:}`） | `application.yml` | 待从 Nacos 配置集或团队约定取值 |
| pm-user 容器化 | 无 Dockerfile | 扫描 `prunus-mume` 仓库确认 | T0.2 第 1 段先在宿主机跑（IDE / `mvn spring-boot:run`），容器化需另行编写 Dockerfile |
