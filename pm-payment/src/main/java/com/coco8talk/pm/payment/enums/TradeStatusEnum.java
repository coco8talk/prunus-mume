package com.coco8talk.pm.payment.enums;

/**
 * 支付宝交易状态枚举类
 * 定义了支付宝交易过程中可能遇到的各种状态
 *
 * @author coco8talk
 * @since 2025/10/20 23:35
 */
public enum TradeStatusEnum {
    /**
     * 等待买家付款状态
     * 交易创建，等待买家付款
     */
    WAIT_BUYER_PAY("WAIT_BUYER_PAY", "交易创建，等待买家付款"),
    
    /**
     * 交易关闭状态
     * 未付款交易超时关闭，或支付完成后全额退款
     */
    TRADE_CLOSED("TRADE_CLOSED", "未付款交易超时关闭，或支付完成后全额退款"),
    
    /**
     * 交易成功状态
     * 交易支付成功
     */
    TRADE_SUCCESS("TRADE_SUCCESS", "交易支付成功"),
    
    /**
     * 交易结束状态
     * 交易结束，不可退款
     */
    TRADE_FINISHED("TRADE_FINISHED", "交易结束，不可退款");
    
    private final String code;
    private final String description;
    
    /**
     * 构造一个交易状态枚举实例
     *
     * @param code        状态码
     * @param description 状态描述
     */
    TradeStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 获取状态码
     *
     * @return 状态码字符串
     */
    public String getCode() {
        return code;
    }
    
    /**
     * 获取状态描述
     *
     * @return 状态描述字符串
     */
    public String getDescription() {
        return description;
    }
}
