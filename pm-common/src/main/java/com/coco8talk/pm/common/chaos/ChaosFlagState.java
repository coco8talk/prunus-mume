package com.coco8talk.pm.common.chaos;

import java.util.Map;

/**
 *
 *
 * @author silas
 * @since 2026/7/27 00:20
 */
public record ChaosFlagState(
        boolean chaosEnabled,
        Map<String, String> params
) {
    public ChaosFlagState {
        params = Map.copyOf(params == null ? Map.of() : params);
    }
}
