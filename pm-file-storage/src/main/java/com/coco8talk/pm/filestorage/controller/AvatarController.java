package com.coco8talk.pm.filestorage.controller;

import com.coco8talk.pm.common.result.Result;
import com.coco8talk.pm.filestorage.service.AvatarService;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author coco8talk
 * @since 2025/10/16 20:43
 **/
@RestController
@RequestMapping("/avatar")
@Log4j2
public class AvatarController {
    
    private final AvatarService avatarService;
    
    public AvatarController(AvatarService avatarService) {
        this.avatarService = avatarService;
    }
    
    @GetMapping("/getAvatarCredentials")
    public Result<Map<String, Object>> getAvatarCredentials(@RequestParam String filename) {
        return avatarService.getAvatarCredentials(filename);
    }
    
    @GetMapping("/confirmAvatarUpload")
    public Result<String> confirmAvatarUpload() {
        return avatarService.confirmAvatarUpload();
    }
    
}
