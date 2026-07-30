package com.coco8talk.pm.gateway.config;

import com.coco8talk.pm.gateway.PmGatewayApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = PmGatewayApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.config.location=file:${maven.multiModuleProjectDirectory}/"
                        + "config/nacos/pm-gateway.yaml",
                "spring.cloud.nacos.config.enabled=false",
                "spring.cloud.nacos.discovery.enabled=false",
                "spring.cloud.discovery.enabled=false"
        })
class GatewayCorsIntegrationTest {

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @BeforeEach
    void connectToRunningGateway() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://127.0.0.1:" + port)
                .build();
    }

    @Test
    void approvedLocalhostOriginsReceiveSingleCredentialedCorsHeaders() {
        assertApprovedPreflight("http://localhost:3000");
        assertApprovedPreflight("http://localhost:3001");
    }

    @Test
    void unapprovedOriginReceivesNoAllowOriginHeader() {
        EntityExchangeResult<byte[]> result = preflight("http://localhost:3999");

        assertThat(result.getResponseHeaders().get(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
                .isNull();
    }

    private void assertApprovedPreflight(String origin) {
        EntityExchangeResult<byte[]> result = preflight(origin);
        HttpHeaders headers = result.getResponseHeaders();

        assertThat(result.getStatus().value()).isEqualTo(200);
        assertThat(headers.get(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
                .containsExactly(origin);
        assertThat(headers.get(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS))
                .containsExactly("true");
        assertThat(headers.get(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS))
                .hasSize(1);
        assertThat(commaSeparatedValues(
                headers.get(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS)))
                .containsExactly("satoken");
    }

    private EntityExchangeResult<byte[]> preflight(String origin) {
        return webTestClient.options()
                .uri("/api/auth/login")
                .header(HttpHeaders.ORIGIN, origin)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS,
                        "content-type,satoken")
                .exchange()
                .expectBody()
                .returnResult();
    }

    private List<String> commaSeparatedValues(List<String> headerValues) {
        return headerValues.stream()
                .flatMap(value -> Arrays.stream(value.split(",")))
                .map(String::trim)
                .toList();
    }
}
