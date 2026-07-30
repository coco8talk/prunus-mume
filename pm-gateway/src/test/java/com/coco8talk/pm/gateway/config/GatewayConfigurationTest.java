package com.coco8talk.pm.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayConfigurationTest {
    private static final String GATEWAY_PREFIX =
            "spring.cloud.gateway.server.webflux.";

    @Test
    void gatewayNacosConfigurationShouldExposeOnlyExplicitPublicRoutesWithBrowserCors() throws IOException {
        Path repositoryRoot = Path.of(System.getProperty("maven.multiModuleProjectDirectory"));
        PropertySource<?> properties = loadYaml(
                new FileSystemResource(repositoryRoot.resolve("config/nacos/pm-gateway.yaml")));

        List<String> routeIds = indexedProperties(
                properties, GATEWAY_PREFIX + "routes[%d].id");
        List<String> routeUris = indexedProperties(
                properties, GATEWAY_PREFIX + "routes[%d].uri");
        List<String> allPathPredicates = indexedProperties(
                properties, GATEWAY_PREFIX + "routes[%d].predicates[0]");
        List<String> publicPathPatterns = allPathPredicates.stream()
                .map(predicate -> predicate.replaceFirst("^Path=", ""))
                .flatMap(paths -> Arrays.stream(paths.split(",")))
                .toList();
        List<String> allowedOrigins = indexedProperties(
                properties,
                GATEWAY_PREFIX + "globalcors.cors-configurations[/**].allowedOrigins[%d]");
        List<String> exposedHeaders = indexedProperties(
                properties,
                GATEWAY_PREFIX + "globalcors.cors-configurations[/**].exposedHeaders[%d]");

        assertThat(properties.getProperty("server.port")).isEqualTo(8082);
        assertThat(routeIds).containsExactly(
                "auth", "user", "question", "interaction", "payment", "file-storage");
        assertThat(routeUris).containsExactly(
                "lb://pm-auth", "lb://pm-user", "lb://pm-question",
                "lb://pm-interaction", "lb://pm-payment", "lb://pm-file-storage");
        assertThat(allPathPredicates).containsExactly(
                "Path=/api/auth/**",
                "Path=/api/users/**",
                "Path=/api/questions/**,/api/question-reviews/**,/api/question-banks/**,"
                        + "/api/question-bank-relations/**,/api/covers/**",
                "Path=/api/thumbs/**,/api/favourites/**,/api/sign-ins/**",
                "Path=/api/membership-orders/**,/api/alipay/**",
                "Path=/api/avatars/**");
        assertThat(publicPathPatterns).containsExactly(
                "/api/auth/**",
                "/api/users/**",
                "/api/questions/**",
                "/api/question-reviews/**",
                "/api/question-banks/**",
                "/api/question-bank-relations/**",
                "/api/covers/**",
                "/api/thumbs/**",
                "/api/favourites/**",
                "/api/sign-ins/**",
                "/api/membership-orders/**",
                "/api/alipay/**",
                "/api/avatars/**");
        assertThat(propertyNames(properties))
                .noneMatch(name -> name.startsWith(
                        GATEWAY_PREFIX + "discovery.locator."))
                .noneMatch(name -> name.startsWith(
                        "spring.cloud.gateway.discovery.locator."));
        assertThat(propertyValues(properties))
                .noneMatch(value -> value.contains("StripPrefix"));
        IntStream.range(0, routeIds.size()).forEach(index -> {
            assertThat(properties.getProperty(
                    (GATEWAY_PREFIX + "routes[%d].predicates[1]").formatted(index))).isNull();
            assertThat(properties.getProperty(
                    (GATEWAY_PREFIX + "routes[%d].filters[0]").formatted(index))).isNull();
        });
        assertThat(allowedOrigins).containsExactly(
                "http://localhost:3000", "http://127.0.0.1:3000",
                "http://localhost:3001", "http://127.0.0.1:3001");
        assertThat(exposedHeaders).containsExactly("satoken");
        assertThat(properties.getProperty(
                GATEWAY_PREFIX + "globalcors.add-to-simple-url-handler-mapping"))
                .isEqualTo(true);
        assertThat(indexedProperties(
                properties, GATEWAY_PREFIX + "default-filters[%d]"))
                .containsExactly("DedupeResponseHeader=Access-Control-Allow-Credentials "
                        + "Access-Control-Allow-Origin Access-Control-Expose-Headers, RETAIN_UNIQUE");
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

    private List<String> propertyNames(PropertySource<?> properties) {
        return Arrays.asList(((EnumerablePropertySource<?>) properties).getPropertyNames());
    }

    private List<String> propertyValues(PropertySource<?> properties) {
        return propertyNames(properties).stream()
                .map(properties::getProperty)
                .map(String::valueOf)
                .toList();
    }
}
