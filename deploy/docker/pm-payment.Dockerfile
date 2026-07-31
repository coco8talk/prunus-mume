# 方案 A：本地/CI 先 `mvn -pl pm-payment -am package` 编译好 jar，这里只负责打包运行。
# 不在容器内跑 Maven，避免多模块 reactor 的构建上下文问题。
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY pm-payment/target/pm-payment-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
