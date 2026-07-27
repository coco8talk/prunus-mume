package com.coco8talk.pm.payment.service;

import com.coco8talk.pm.common.result.Result;
import com.coco8talk.pm.payment.model.dto.MembershipOrderCreateDTO;
import com.coco8talk.pm.payment.model.entity.MembershipOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coco8talk.pm.payment.model.vo.MembershipOrderCreateVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author coco8talk
 * @description 针对表【membership_order】的数据库操作Service
 * @createDate 2025-10-20 09:35:19
 */
public interface MembershipOrderService extends IService<MembershipOrder> {

    /**
     * 提交会员订单
     *
     * @param membershipOrderCreateDTO 会员订单创建请求参数
     * @return 会员订单创建响应视图对象
     */
    Result<MembershipOrderCreateVO> createOrder(MembershipOrderCreateDTO membershipOrderCreateDTO);

    /**
     * 支付会员订单
     *
     * @param outTradeNo 商户订单号
     * @return 支付页面链接
     */
    String pay(String outTradeNo);

    /**
     * 支付宝支付回调处理
     *
     * @param request HttpServletRequest对象，包含支付宝回调请求参数
     * @return "success" 表示处理成功，"failure" 表示处理失败
     */
    String alipayCallback(HttpServletRequest request);

    /**
     * 支付宝同步返回处理（查询支付状态并开通会员）
     *
     * @param request HttpServletRequest对象，包含支付宝同步返回参数
     * @return 处理结果描述
     */
    String handleReturn(HttpServletRequest request);
}
