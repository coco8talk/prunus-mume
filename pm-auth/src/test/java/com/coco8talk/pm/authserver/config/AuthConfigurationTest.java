package com.coco8talk.pm.authserver.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AuthConfigurationTest {
    @Test
    void authNacosConfigurationShouldProvidePortAndRedisDefaults() throws IOException {
        PropertySource<?> application = loadYaml(new ClassPathResource("application.yml"));
        Path repositoryRoot = Path.of(System.getProperty("maven.multiModuleProjectDirectory"));
        Path authConfigPath = repositoryRoot.resolve("config/nacos/pm-auth.yaml");
        PropertySource<?> authConfig = loadYaml(new FileSystemResource(authConfigPath));
        String rawAuthConfig = Files.readString(authConfigPath);

        assertThat(application.getProperty("spring.config.import[1]"))
                .asString()
                .startsWith("nacos:pm-auth.yaml");
        assertThat(authConfig.getProperty("server.port")).isEqualTo(8081);
        assertThat(authConfig.getProperty("spring.data.redis.host")).isNotNull();
        assertThat(Integer.valueOf(resolveProperty(authConfig, "spring.data.redis.port"))).isEqualTo(6379);
        assertThat(rawAuthConfig).contains("${coco8talk.redis.host:localhost}");
        assertThat(rawAuthConfig).doesNotContain("password:");
    }

    private PropertySource<?> loadYaml(org.springframework.core.io.Resource resource) throws IOException {
        return new YamlPropertySourceLoader().load(resource.getFilename(), resource).getFirst();
    }

    private String resolveProperty(PropertySource<?> propertySource, String propertyName) {
        MutablePropertySources propertySources = new MutablePropertySources();
        propertySources.addLast(propertySource);
        return new PropertySourcesPropertyResolver(propertySources)
                .resolveRequiredPlaceholders(String.valueOf(propertySource.getProperty(propertyName)));
    }
}
