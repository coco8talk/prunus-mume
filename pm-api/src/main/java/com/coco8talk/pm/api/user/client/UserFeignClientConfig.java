package com.coco8talk.pm.api.user.client;

import com.coco8talk.pm.common.exception.BizException;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class UserFeignClientConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            String body = "";
            if (response.body() != null) {
                try {
                    body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
                } catch (IOException ignored) {
                }
            }
            return new BizException(response.status(), body);
        };
    }
}
