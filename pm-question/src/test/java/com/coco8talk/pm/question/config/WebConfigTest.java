package com.coco8talk.pm.question.config;

import com.coco8talk.pm.platform.config.WebConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD;
import static org.springframework.http.HttpHeaders.ORIGIN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitWebConfig(WebConfigTest.TestConfiguration.class)
@TestPropertySource(properties = {
        "coco8talk.web.allowed-origins=http://localhost:3000,http://127.0.0.1:3000,http://localhost:3001,http://127.0.0.1:3001"
})
class WebConfigTest {
    private final WebApplicationContext context;
    private MockMvc mockMvc;

    WebConfigTest(WebApplicationContext context) {
        this.context = context;
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void corsShouldExposeSaTokenResponseHeader() throws Exception {
        mockMvc.perform(options("/probe")
                        .header(ORIGIN, "http://localhost:3000")
                        .header(ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(ACCESS_CONTROL_EXPOSE_HEADERS, "satoken"));
    }

    @Test
    void corsShouldAllowAdminDevelopmentOrigin() throws Exception {
        mockMvc.perform(options("/probe")
                        .header(ORIGIN, "http://localhost:3001")
                        .header(ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3001"))
                .andExpect(header().string(ACCESS_CONTROL_EXPOSE_HEADERS, "satoken"));
    }

    @RestController
    static class TestController {
        @GetMapping("/probe")
        void probe() {
        }
    }

    @Configuration
    @EnableWebMvc
    @Import({WebConfig.class, TestController.class})
    static class TestConfiguration {
    }
}
