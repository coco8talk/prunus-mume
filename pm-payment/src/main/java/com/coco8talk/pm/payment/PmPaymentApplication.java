package com.coco8talk.pm.payment;

import com.coco8talk.pm.api.user.service.UserApi;
import com.coco8talk.pm.platform.config.MybatisPlusConfig;
import com.coco8talk.pm.platform.config.WebConfig;
import com.coco8talk.pm.platform.web.GlobalExceptionHandler;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@MapperScan("com.coco8talk.pm.payment.mapper")
@Import({
        MybatisPlusConfig.class,
        WebConfig.class,
        GlobalExceptionHandler.class
})
@EnableFeignClients(basePackageClasses = UserApi.class)
@SpringBootApplication(scanBasePackages = {
        "com.coco8talk.pm.payment",
        "com.coco8talk.pm.auth",
        "com.coco8talk.pm.common.chaos"
})
public class PmPaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(PmPaymentApplication.class, args);
    }
}
