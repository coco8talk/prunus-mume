/*
 * Sentinel Dashboard Nacos datasource extension.
 * Data ID / group naming used to persist dashboard-edited rules to Nacos config center.
 */
package com.alibaba.csp.sentinel.dashboard.rule.nacos;

public final class NacosConfigUtil {

    public static final String GROUP_ID = "SENTINEL_GROUP";
    public static final String FLOW_DATA_ID_POSTFIX = "-flow-rules";

    private NacosConfigUtil() {
    }
}
