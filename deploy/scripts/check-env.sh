#!/usr/bin/env bash
#
# 环境就绪门:先探 Nacos,不通则判定"全链路阻塞"并跳过后续检测;
# Nacos 通了再依次探六个 pm 服务的 /chaos/status。

set -uo pipefail

# 这个 Nacos 镜像(v3.2.3-slim)没有暴露可靠的 HTTP health 端点
# (/nacos/v1/console/health/readiness、/nacos/actuator/health 实测都是 404,
# 只暴露了 actuator/prometheus)。改用 Web 控制台根路径:
# Nacos 内部 Web 层真正初始化完才会对它返回 200,没起来时是连接拒绝或 000。
NACOS_URL="http://localhost:8848/nacos/"

# 用 "name:port" 字符串数组而不是关联数组(declare -A),
# 因为 macOS 自带的 /bin/bash 是 3.2,不支持关联数组。
PM_SERVICES=(
  "pm-user:8091"
  "pm-auth:8081"
  "pm-payment:8089"
  "pm-question:8083"
  "pm-interaction:8086"
  "pm-file-storage:8092"
)

check_http() {
  local url=$1
  curl -s -o /dev/null -w "%{http_code}" --max-time 3 "$url"
}

echo "== 检测 Nacos =="
nacos_status=$(check_http "$NACOS_URL")
if [ "$nacos_status" != "200" ]; then
  echo "❌ Nacos 未就绪 (got ${nacos_status:-000}) —— 全链路阻塞,跳过后续 pm 服务检测"
  exit 1
fi
echo "✅ Nacos OK"

fail=0
echo "== 检测 pm 服务 =="
for entry in "${PM_SERVICES[@]}"; do
  name=${entry%%:*}
  port=${entry##*:}
  url="http://localhost:${port}/api/chaos/status"
  status=$(check_http "$url")
  if [ "$status" = "200" ]; then
    echo "✅ ${name} OK"
  else
    echo "❌ ${name} FAIL (got ${status:-000})"
    fail=1
  fi
done

if [ "$fail" -eq 0 ]; then
  echo "== 全链路 PASS =="
  exit 0
else
  echo "== 存在服务未就绪,FAIL =="
  exit 1
fi
