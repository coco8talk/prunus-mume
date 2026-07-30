package com.coco8talk.pm.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayConfigurationTest {
    @Test
    void gatewayNacosConfigurationShouldExposeOnlyExplicitPublicRoutesWithBrowserCors() throws IOException {
        Path repositoryRoot = Path.of(System.getProperty("maven.multiModuleProjectDirectory"));
        PropertySource<?> properties = loadYaml(
                new FileSystemResource(repositoryRoot.resolve("config/nacos/pm-gateway.yaml")));

        List<String> routeIds = indexedProperties(
                properties, "spring.cloud.gateway.server.webflux.routes[%d].id");
        List<String> routeUris = indexedProperties(
                properties, "spring.cloud.gateway.server.webflux.routes[%d].uri");
        List<String> allPathPredicates = indexedProperties(
                properties, "spring.cloud.gateway.server.webflux.routes[%d].predicates[0]");
        List<String> allowedOrigins = indexedProperties(
                properties,
                "spring.cloud.gateway.server.webflux.globalcors.cors-configurations[/**].allowedOrigins[%d]");
        List<String> exposedHeaders = indexedProperties(
                properties,
                "spring.cloud.gateway.server.webflux.globalcors.cors-configurations[/**].exposedHeaders[%d]");

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
        assertThat(properties.getProperty(
                "spring.cloud.gateway.server.webflux.globalcors.add-to-simple-url-handler-mapping"))
                .isEqualTo(true);
        assertThat(indexedProperties(
                properties, "spring.cloud.gateway.server.webflux.default-filters[%d]"))
                .anyMatch(filter -> filter.startsWith("DedupeResponseHeader="));
    }

    private PropertySource<?> loadYaml(org.springframework.core.io.Resource resource) throws IOException {
        return new YamlPropertySourceLoader().load(resource.getFilename(), resource).getFirst();
    }

    private List<String> indexedProperties(PropertySource<?> properties, String propertyNamePattern) {
        return IntStream.iterate(0, index -> properties.getProperty(propertyNamePattern.formatted(index)) != null,
                        index -> index + 1)
                .mapToObj(index -> String.valueOf(properties.getProperty(propertyNamePattern.formatted(index))))
                .toList();
    }
}
