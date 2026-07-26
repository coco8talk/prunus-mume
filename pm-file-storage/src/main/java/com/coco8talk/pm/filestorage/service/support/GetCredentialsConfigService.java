package com.coco8talk.pm.filestorage.service.support;

import cn.hutool.core.util.StrUtil;
import com.tencent.cloud.cos.util.Jackson;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 获取临时密钥和上传配置
 *
 * @author coco8talk
 * @since 2025/10/15 20:33
 **/
@Log4j2
@Service
public class GetCredentialsConfigService {
    
    private final GetKeyService getKeyService;
    @Value("${coco8talk.tencent.cos.bucketName}")
    private String bucketName;
    @Value("${coco8talk.tencent.cos.region}")
    private String region;
    @Value("${coco8talk.tencent.cos.secretId}")
    private String secretId;
    @Value("${coco8talk.tencent.cos.secretKey}")
    private String secretKey;
    @Value("${coco8talk.tencent.cos.appId}")
    private String appId;
    
    public GetCredentialsConfigService(GetKeyService getKeyService) {
        this.getKeyService = getKeyService;
    }
    
    /**
     * 获取配置信息
     *
     * @param filename           文件名
     * @param proxy              代理
     * @param durationSeconds    临时密钥有效期，单位秒
     * @param limitExt           是否限制上传文件后缀
     * @param extWhiteList       限制的上传后缀列表
     * @param limitContentType   是否限制上传 contentType
     * @param limitContentLength 是否限制上传文件大小
     * @return 配置信息
     */
    public TreeMap<String, Object> getConfig(String filename,
                                             String proxy,
                                             int durationSeconds,
                                             boolean limitExt,
                                             List<String> extWhiteList,
                                             boolean limitContentType,
                                             boolean limitContentLength) {
        String ext = filename.contains(".") ? filename.substring(filename.lastIndexOf(".") + 1).toLowerCase() : "";
        
        Map<String, Object> condition = new HashMap<>(2);
        
        // 1. 限制上传文件后缀
        if (limitExt) {
            boolean extInvalid = StrUtil.isBlank(ext) || !extWhiteList.contains(ext.toLowerCase());
            if (extInvalid) {
                throw new IllegalArgumentException("非法文件，禁止上传");
            }
        }
        
        // 2. 限制上传文件 content-type
        if (limitContentType) {
            HashMap<String, String> contentTypeCondition = new HashMap<>(1);
            contentTypeCondition.put("cos:content-type", "image/*");
            condition.put("string_like_if_exist", contentTypeCondition);
        }
        
        // 3. 限制上传文件大小(只对简单上传生效)
        if (limitContentLength) {
            HashMap<String, List<Long>> contentLengthCondition = new HashMap<>(1);
            contentLengthCondition.put("cos:content-length", List.of(0L, 5L * 1024 * 1024));
            condition.put("numeric_between", contentLengthCondition);
        }
        
        // 生成 cos key
        String key = getKeyService.generateAvatarCosKey();
        
        // 资源描述符
        String resource = "qcs::cos:" + region + ":uid/" + appId + ':' + bucketName + '/' + key;
        
        // 允许的操作列表
        List<String> allowActions = Arrays.asList(
            // 简单上传
            "name/cos:PutObject",
            // 分块上传
            "name/cos:InitiateMultipartUpload",
            "name/cos:ListMultipartUploads",
            "name/cos:ListParts",
            "name/cos:UploadPart",
            "name/cos:CompleteMultipartUpload"
        );
        // 构建policy
        Map<String, Object> policy = new HashMap<>(2);
        policy.put("version", "2.0");
        Map<String, Object> statement = new HashMap<>(4);
        statement.put("action", allowActions);
        statement.put("effect", "allow");
        List<String> resources = List.of(
            resource
        );
        statement.put("resource", resources);
        statement.put("condition", condition);
        policy.put("statement", List.of(statement));
        
        
        // 构建config
        TreeMap<String, Object> config = new TreeMap<>();
        config.put("secretId", secretId);
        config.put("secretKey", secretKey);
        config.put("proxy", proxy);
        config.put("duration", durationSeconds);
        config.put("bucket", bucketName);
        config.put("region", region);
        config.put("key", key);
        config.put("policy", Jackson.toJsonPrettyString(policy));
        return config;
    }
}
