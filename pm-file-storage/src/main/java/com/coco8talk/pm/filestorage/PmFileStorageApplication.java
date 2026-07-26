package com.coco8talk.pm.filestorage;

import com.coco8talk.pm.api.user.client.RemoteUserApi;
import com.coco8talk.pm.platform.config.WebConfig;
import com.coco8talk.pm.platform.web.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import({
        WebConfig.class,
        GlobalExceptionHandler.class,
        RemoteUserApi.class
})
@SpringBootApplication(scanBasePackages = {
        "com.coco8talk.pm.filestorage",
        "com.coco8talk.pm.auth"
})
public class PmFileStorageApplication {

    public static void main(String[] args) {
        SpringApplication.run(PmFileStorageApplication.class, args);
    }
}
