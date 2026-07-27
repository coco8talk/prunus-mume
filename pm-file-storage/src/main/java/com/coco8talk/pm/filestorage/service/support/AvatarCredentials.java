package com.coco8talk.pm.filestorage.service.support;

import com.coco8talk.pm.common.result.http.HttpStatusEnum;
import com.coco8talk.pm.common.exception.ThrowUtils;
import com.tencent.cloud.CosStsClient;
import com.tencent.cloud.Response;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 获取头像上传临时密钥和上传配置
 * @author coco8talk
 * @since 2025/10/16 20:18
 **/
@Service
@Log4j2
public class AvatarCredentials {
    private final GetCredentialsConfigService getCredentialsConfigService;
    
    public AvatarCredentials(GetCredentialsConfigService getCredentialsConfigService) {
        this.getCredentialsConfigService = getCredentialsConfigService;
    }
    
    /**
     * 获取头像上传credentials
     *
     * @param filename 文件名
     * @return credentials json 字符串
     */
    public Map<String, Object> getAvatarCredentials(String filename) {
        
        TreeMap<String, Object> config =
            getCredentialsConfigService.getConfig(
                filename,
                "",
                1800,
                true,
                List.of("jpg", "jpeg", "png", "gif", "bmp"),
                false,
                false);
        
        ThrowUtils.throwIfNull(config, HttpStatusEnum.BAD_REQUEST, "get cos config error");
        
        try {
            Response response = CosStsClient.getCredential(config);
            TreeMap <String,Object> result = new TreeMap<>();
            TreeMap <String,Object> credentials = new TreeMap<>();
            credentials.put("tmpSecretId",response.credentials.tmpSecretId);
            credentials.put("tmpSecretKey",response.credentials.tmpSecretKey);
            credentials.put("sessionToken",response.credentials.sessionToken);
            result.put("startTime",response.startTime);
            result.put("expiredTime",response.expiredTime);
            result.put("requestId",response.requestId);
            result.put("expiration",response.expiration);
            result.put("credentials",credentials);
            result.put("bucket",config.get("bucket"));
            result.put("region",config.get("region"));
            result.put("key",config.get("key"));
            return result;
            
        } catch (Exception e) {
            log.error("get cos sts error", e);
            throw new IllegalArgumentException("no valid secret !");
        }
    }
}
