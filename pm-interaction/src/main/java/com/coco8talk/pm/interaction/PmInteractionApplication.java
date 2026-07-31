package com.coco8talk.pm.interaction;

import com.coco8talk.pm.api.question.service.QuestionApi;
import com.coco8talk.pm.common.lock.DistributedLock;
import com.coco8talk.pm.platform.config.MybatisPlusConfig;
import com.coco8talk.pm.platform.config.RedissonConfig;
import com.coco8talk.pm.platform.config.WebConfig;
import com.coco8talk.pm.platform.web.GlobalExceptionHandler;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@MapperScan("com.coco8talk.pm.interaction.mapper")
@Import({
        MybatisPlusConfig.class,
        RedissonConfig.class,
        DistributedLock.class,
        WebConfig.class,
        GlobalExceptionHandler.class
})
@EnableFeignClients(basePackageClasses = QuestionApi.class)
@SpringBootApplication(scanBasePackages = {
        "com.coco8talk.pm.interaction",
        "com.coco8talk.pm.auth",
        "com.coco8talk.pm.common"
})
public class PmInteractionApplication {

    public static void main(String[] args) {
        SpringApplication.run(PmInteractionApplication.class, args);
    }
}
