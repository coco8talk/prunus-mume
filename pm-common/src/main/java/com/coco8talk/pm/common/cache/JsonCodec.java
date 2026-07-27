package com.coco8talk.pm.common.cache;

import com.coco8talk.pm.common.exception.BizException;
import com.coco8talk.pm.common.result.http.HttpStatusEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * 缓存值的 JSON 编解码薄封装。非 String 类型经 Jackson 序列化为字符串存入缓存,
 * 反序列化异常统一转 {@link BizException}。注册 JavaTimeModule 以支持 {@code LocalDateTime}。
 *
 * @author coco8talk
 */
final class JsonCodec {

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private JsonCodec() {
    }

    static String write(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw new BizException(HttpStatusEnum.INTERNAL_SERVER_ERROR.getCode(), "缓存序列化失败: " + e.getMessage());
        }
    }

    static <T> T read(String raw, Class<T> type) {
        try {
            return MAPPER.readValue(raw, type);
        } catch (Exception e) {
            throw new BizException(HttpStatusEnum.INTERNAL_SERVER_ERROR.getCode(), "缓存反序列化失败: " + e.getMessage());
        }
    }
}
