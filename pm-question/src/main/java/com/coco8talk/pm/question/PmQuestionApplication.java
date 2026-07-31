package com.coco8talk.pm.question;

import com.coco8talk.pm.api.user.service.UserApi;
import com.coco8talk.pm.common.lock.DistributedLock;
import com.coco8talk.pm.platform.cache.RedisCacheStore;
import com.coco8talk.pm.platform.config.CacheConfig;
import com.coco8talk.pm.platform.config.MybatisPlusConfig;
import com.coco8talk.pm.platform.config.RedissonConfig;
import com.coco8talk.pm.platform.config.WebConfig;
import com.coco8talk.pm.platform.web.GlobalExceptionHandler;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@MapperScan({
        "com.coco8talk.pm.question.mapper",
        "com.coco8talk.pm.question.bank.mapper"
})
@Import({
        MybatisPlusConfig.class,
        RedissonConfig.class,
        CacheConfig.class,
        RedisCacheStore.class,
        DistributedLock.class,
        WebConfig.class,
        GlobalExceptionHandler.class
})
@EnableFeignClients(basePackageClasses = UserApi.class)
@SpringBootApplication(scanBasePackages = {
        "com.coco8talk.pm.question",
        "com.coco8talk.pm.auth",
        "com.coco8talk.pm.common"
})
public class PmQuestionApplication {

    public static void main(String[] args) {
        SpringApplication.run(PmQuestionApplication.class, args);
    }
}
