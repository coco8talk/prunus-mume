# 本地/CI 先 `mvn -pl pm-interaction clean package -DskipTests` 编译好 jar，这里只负责打包运行。
# 不在容器内跑 Maven，避免多模块 reactor 的构建上下文问题。
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY pm-interaction/target/pm-interaction-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
