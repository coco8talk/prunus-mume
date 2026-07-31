package com.coco8talk.pm.authserver;

import com.coco8talk.pm.api.user.service.UserApi;
import com.coco8talk.pm.platform.config.WebConfig;
import com.coco8talk.pm.platform.web.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

/**
 * pm-auth 独立服务入口。
 * scanBasePackages 同时覆盖 com.coco8talk.pm.auth（Sa-Token 会话相关 bean，
 * 也被其它业务服务通过同一 base package 各自内嵌扫描）与本模块自身的
 * com.coco8talk.pm.authserver（仅本服务使用的 Controller/Service），
 * 两者分包是为了避免 AuthController 被其它服务的 scanBasePackages="com.coco8talk.pm.auth" 一并扫入。
 */
@Import({
        WebConfig.class,
        GlobalExceptionHandler.class
})
@EnableFeignClients(basePackageClasses = UserApi.class)
@SpringBootApplication(scanBasePackages = {
        "com.coco8talk.pm.authserver",
        "com.coco8talk.pm.auth",
        "com.coco8talk.pm.common.chaos"
})
public class PmAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(PmAuthApplication.class, args);
    }
}
