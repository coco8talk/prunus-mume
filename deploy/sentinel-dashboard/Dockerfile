# Custom Sentinel dashboard image: alibaba/Sentinel v1.8.10 dashboard, patched so
# flow-rule edits made in the UI are read from / published to Nacos config center
# (dataId "<app>-flow-rules", group "SENTINEL_GROUP") instead of living only in the
# dashboard's in-memory store. See ../../README.md for the full rationale.
#
# Build stage: clone the official source at the release tag, apply the patch in
# ./patch, override the extension's pinned nacos-client (1.4.2, incompatible with
# Nacos Server 3.x — see project notes) to 3.0.2, and package the dashboard jar.
FROM maven:3.9.9-eclipse-temurin-17 AS build

ARG SENTINEL_VERSION=1.8.10
ARG NACOS_CLIENT_VERSION=3.0.2

WORKDIR /workspace

RUN git clone --depth 1 --branch ${SENTINEL_VERSION} https://github.com/alibaba/Sentinel.git .

COPY patch/rule/nacos/*.java sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/rule/nacos/

# Make the Nacos datasource extension a real (non-test-scope) runtime dependency of the dashboard.
RUN sed -i.bak \
    -e '/<!-- for Nacos rule publisher sample -->/,/<\/dependency>/{/<scope>test<\/scope>/d}' \
    sentinel-dashboard/pom.xml && rm sentinel-dashboard/pom.xml.bak

# Point FlowControllerV2 at the Nacos-backed provider/publisher beans instead of the
# default in-memory ones that only push rules straight to a connected client machine.
RUN sed -i.bak \
    -e 's/@Qualifier("flowRuleDefaultProvider")/@Qualifier("flowRuleNacosProvider")/' \
    -e 's/@Qualifier("flowRuleDefaultPublisher")/@Qualifier("flowRuleNacosPublisher")/' \
    sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/controller/v2/FlowControllerV2.java \
    && rm sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/controller/v2/FlowControllerV2.java.bak

RUN --mount=type=cache,target=/root/.m2 \
    mvn -pl sentinel-dashboard -am clean package -DskipTests -B \
    -Dnacos.version=${NACOS_CLIENT_VERSION}

FROM eclipse-temurin:17-jre

RUN apt-get update && apt-get install -y --no-install-recommends wget \
    && rm -rf /var/lib/apt/lists/*
RUN useradd --system --create-home --home-dir /app sentinel
WORKDIR /app
COPY --from=build /workspace/sentinel-dashboard/target/sentinel-dashboard.jar /app/sentinel-dashboard.jar
USER sentinel

EXPOSE 8858

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=8858 -Dcsp.sentinel.dashboard.server=localhost:8858 -Dproject.name=sentinel-dashboard $JAVA_OPTS -jar /app/sentinel-dashboard.jar"]
