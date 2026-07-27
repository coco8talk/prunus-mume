package com.coco8talk.pm.common.chaos;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 故障的状态控制组件
 * ToDo 当前没有任何具体的故障逻辑，只是一个框架，后续在 p4 完成
 *
 * @author silas
 * @since 2026/7/26 20:07
 */
@Component
public class ChaosRegister {

    /**
     * 故障信息
     */
    private final Map<String, ChaosFlagState> chaos = new ConcurrentHashMap<>();

    /**
     * 故障是否开启
     *
     * @param chaosName 故障名称
     * @return 是否开启故障
     */
    public boolean isEnabled(String chaosName) {
        ChaosFlagState state = chaos.get(chaosName);
        return state != null && state.chaosEnabled();
    }

    /**
     * 获取故障参数
     *
     * @param chaosName 故障名称
     * @return 故障参数
     */
    public Map<String, String> getParams(String chaosName) {
        ChaosFlagState state = chaos.get(chaosName);
        return state == null ? Map.of() : state.params();
    }

    /**
     * 注册故障
     *
     * @param chaosName      故障名称
     * @param chaosFlagState 故障信息
     */
    public void register(String chaosName, ChaosFlagState chaosFlagState) {
        chaos.put(chaosName, chaosFlagState);
    }

    /**
     * 获取所有故障
     *
     * @return 所有故障
     */
    public Map<String, ChaosFlagState> getAll() {
        return Collections.unmodifiableMap(chaos);
    }
}
