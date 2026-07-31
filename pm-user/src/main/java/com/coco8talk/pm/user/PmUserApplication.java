package com.coco8talk.pm.user;

import com.coco8talk.pm.api.question.service.QuestionApi;
import com.coco8talk.pm.platform.config.MybatisPlusConfig;
import com.coco8talk.pm.platform.config.RedissonConfig;
import com.coco8talk.pm.platform.config.WebConfig;
import com.coco8talk.pm.platform.web.GlobalExceptionHandler;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@MapperScan("com.coco8talk.pm.user.mapper")
@Import({
        MybatisPlusConfig.class,
        RedissonConfig.class,
        WebConfig.class,
        GlobalExceptionHandler.class
})
@SpringBootApplication(scanBasePackages = {
        "com.coco8talk.pm.user",
        "com.coco8talk.pm.auth",
        "com.coco8talk.pm.common"
})
@EnableDiscoveryClient
@EnableFeignClients(clients = QuestionApi.class)
public class PmUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(PmUserApplication.class, args);
    }
}
