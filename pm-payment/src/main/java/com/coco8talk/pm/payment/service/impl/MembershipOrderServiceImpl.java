package com.coco8talk.pm.payment.service.impl;
import com.coco8talk.pm.payment.service.MembershipOrderService;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coco8talk.pm.payment.enums.TradeStatusEnum;
import com.coco8talk.pm.payment.service.AlipaySignatureVerifier;
import com.coco8talk.pm.payment.util.MembershipDuration;
import com.coco8talk.pm.common.Result;
import com.coco8talk.pm.common.HttpStatusEnum;
import com.coco8talk.pm.common.BizException;
import com.coco8talk.pm.common.ThrowUtils;
import com.coco8talk.pm.payment.mapper.MembershipOrderMapper;
import com.coco8talk.pm.payment.mapper.MembershipPriceMapper;
import com.coco8talk.pm.payment.convert.MembershipOrderMapStruct;
import com.coco8talk.pm.payment.model.dto.MembershipOrderCreateDTO;
import com.coco8talk.pm.payment.model.entity.MembershipOrder;
import com.coco8talk.pm.payment.model.vo.MembershipOrderCreateVO;
import com.coco8talk.pm.api.auth.service.AuthSessionApi;
import com.coco8talk.pm.api.auth.dto.SessionUserDTO;
import com.coco8talk.pm.common.util.ObjectMyUtil;
import com.coco8talk.pm.api.user.service.UserApi;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Log4j2
public class MembershipOrderServiceImpl extends ServiceImpl<MembershipOrderMapper, MembershipOrder>
    implements MembershipOrderService {

    private final MembershipPriceMapper membershipPriceMapper;
    private final MembershipOrderMapper membershipOrderMapper;
    private final AlipayClient alipayClient;
    private final UserApi userApi;
    private final AuthSessionApi authSessionApi;
    private final AlipaySignatureVerifier signatureVerifier;

    @Value("${coco8talk.snowflake.workId}")
    private long workId;

    @Value("${coco8talk.alipay.notify-url:http://localhost:8080/api/alipay/callback}")
    private String notifyUrl;
    @Value("${coco8talk.alipay.return-url:http://localhost:8080/membership/result}")
    private String returnUrl;
    @Value("${coco8talk.alipay.app-id}")
    private String appId;

    public MembershipOrderServiceImpl(MembershipPriceMapper membershipPriceMapper,
                                       MembershipOrderMapper membershipOrderMapper,
                                       AlipayClient alipayClient,
                                       UserApi userApi,
                                       AuthSessionApi authSessionApi,
                                       AlipaySignatureVerifier signatureVerifier) {
        this.membershipPriceMapper = membershipPriceMapper;
        this.membershipOrderMapper = membershipOrderMapper;
        this.alipayClient = alipayClient;
        this.userApi = userApi;
        this.authSessionApi = authSessionApi;
        this.signatureVerifier = signatureVerifier;
    }

    @Override
    public Result<MembershipOrderCreateVO> createOrder(MembershipOrderCreateDTO membershipOrderCreateDTO) {
        ObjectMyUtil.throwIfAllFieldsAreEmptyOrBlank(membershipOrderCreateDTO, HttpStatusEnum.BAD_REQUEST, "请求参数不能为空");

        Byte levelId = membershipOrderCreateDTO.getLevelId();
        String currency = membershipOrderCreateDTO.getCurrency();
        String durationType = membershipOrderCreateDTO.getDurationType();
        Integer durationValue = membershipOrderCreateDTO.getDurationValue();

        ThrowUtils.throwIfTrue(levelId == null || levelId <= 0, HttpStatusEnum.BAD_REQUEST, "会员等级ID不能为空或小于等于0");
        ThrowUtils.throwIfTrue(StrUtil.isBlankOrUndefined(durationType), HttpStatusEnum.BAD_REQUEST, "会员时长类型不能为空");
        ThrowUtils.throwIfTrue(durationValue == null || durationValue <= 0, HttpStatusEnum.BAD_REQUEST, "会员时长值不能为空");

        BigDecimal amount = membershipPriceMapper.selectPriceByParam(levelId, currency, durationType, durationValue);
        ThrowUtils.throwIfTrue(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0, HttpStatusEnum.BAD_REQUEST, "未找到对应的会员价格信息");

        MembershipOrder membershipOrder = MembershipOrderMapStruct.INSTANCE.createDtoToEntity(membershipOrderCreateDTO);
        long loginIdAsLong = StpUtil.getLoginIdAsLong();
        membershipOrder.setUserId(loginIdAsLong);
        Snowflake snowflake = IdUtil.getSnowflake(workId);
        long outOrderNoId = snowflake.nextId();
        membershipOrder.setOutOrderNo(String.valueOf(outOrderNoId));
        membershipOrder.setAmount(amount);
        membershipOrder.setPaymentStatus("PENDING");
        membershipOrder.setPaymentMethod("ALIPAY");
        LocalDateTime now = LocalDateTime.now();
        membershipOrder.setStartTime(now);
        membershipOrder.setEndTime(now.plus(
            MembershipDuration.toPeriod(durationType, durationValue)));

        int insertResult = membershipOrderMapper.insert(membershipOrder);
        ThrowUtils.throwIfTrue(insertResult <= 0, HttpStatusEnum.INTERNAL_SERVER_ERROR, "创建会员订单失败，请稍后重试");

        MembershipOrderCreateVO vo = MembershipOrderMapStruct.INSTANCE.entityToCreateVo(membershipOrder);
        return Result.success(HttpStatusEnum.OK, vo);
    }

    @Override
    public String pay(String outTradeNo) {
        ThrowUtils.throwIfTrue(StrUtil.isBlankOrUndefined(outTradeNo), HttpStatusEnum.BAD_REQUEST, "订单号不能为空");
        MembershipOrder order = membershipOrderMapper.selectByOutTradeNo(outTradeNo);
        ThrowUtils.throwIfNull(order, HttpStatusEnum.NOT_FOUND, "订单不存在");
        long currentUserId = StpUtil.getLoginIdAsLong();
        ThrowUtils.throwIfTrue(!order.getUserId().equals(currentUserId), HttpStatusEnum.FORBIDDEN, "无权支付该订单");

        AlipayTradePagePayRequest request = getAlipayTradePagePayRequest(outTradeNo, order.getAmount());
        request.setReturnUrl(returnUrl);
        request.setNotifyUrl(notifyUrl);
        try {
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            return response.getBody();
        } catch (AlipayApiException e) {
            log.error("调用支付宝支付接口异常，outTradeNo：{}", outTradeNo, e);
            throw new BizException(HttpStatusEnum.INTERNAL_SERVER_ERROR.getCode(), "调用支付宝支付接口异常，请稍后重试");
        }
    }

    @Override
    @Transactional
    public String alipayCallback(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v.length > 0 ? v[0] : ""));

        if (!signatureVerifier.verify(params)) {
            log.warn("支付宝回调验签失败：{}", params.get("out_trade_no"));
            return "failure";
        }
        if (!appId.equals(params.get("app_id"))) {
            log.warn("支付宝回调app_id不匹配: expected={}, actual={}", appId, params.get("app_id"));
            return "failure";
        }
        String tradeStatus = params.get("trade_status");
        if (!TradeStatusEnum.TRADE_SUCCESS.getCode().equals(tradeStatus)
            && !TradeStatusEnum.TRADE_FINISHED.getCode().equals(tradeStatus)) {
            return "success";
        }
        String outTradeNo = params.get("out_trade_no");
        MembershipOrder order = membershipOrderMapper.selectByOutTradeNo(outTradeNo);
        if (order == null) {
            log.warn("回调未找到订单：{}", outTradeNo);
            return "failure";
        }
        if ("PAID".equals(order.getPaymentStatus())) {
            return "success";
        }
        BigDecimal notifyAmount = new BigDecimal(params.get("total_amount"));
        if (order.getAmount().compareTo(notifyAmount) != 0) {
            log.error("回调金额不一致 order={} notify={}", order.getAmount(), notifyAmount);
            return "failure";
        }
        order.setOrderNo(params.get("trade_no"));
        order.setPaymentStatus("PAID");
        order.setUpdatedAt(LocalDateTime.now());
        membershipOrderMapper.updateById(order);
        userApi.grantVip(order.getUserId(),
            MembershipDuration.toPeriod(order.getDurationType(), order.getDurationValue()));
        refreshSessionIfActive(order.getUserId());
        log.info("会员开通成功 user={} outTradeNo={}", order.getUserId(), outTradeNo);
        return "success";
    }

    @Override
    @Transactional
    public String handleReturn(HttpServletRequest request) {
        String outTradeNo = request.getParameter("out_trade_no");
        if (StrUtil.isBlank(outTradeNo)) {
            log.warn("同步返回缺少out_trade_no参数");
            return "参数错误";
        }

        MembershipOrder order = membershipOrderMapper.selectByOutTradeNo(outTradeNo);
        if (order == null) {
            log.warn("同步返回未找到订单：{}", outTradeNo);
            return "订单不存在";
        }

        if ("PAID".equals(order.getPaymentStatus())) {
            return "success";
        }

        // 主动查询支付宝确认支付状态
        AlipayTradeQueryRequest queryRequest = new AlipayTradeQueryRequest();
        JSONObject queryBiz = new JSONObject();
        queryBiz.put("out_trade_no", outTradeNo);
        queryRequest.setBizContent(queryBiz.toString());
        try {
            AlipayTradeQueryResponse queryResponse = alipayClient.execute(queryRequest);
            if (!queryResponse.isSuccess()) {
                log.warn("支付状态查询失败 outTradeNo={} code={} msg={}",
                    outTradeNo, queryResponse.getCode(), queryResponse.getMsg());
                return "支付状态查询失败：" + queryResponse.getMsg();
            }
            String tradeStatus = queryResponse.getTradeStatus();
            if (!TradeStatusEnum.TRADE_SUCCESS.getCode().equals(tradeStatus)
                && !TradeStatusEnum.TRADE_FINISHED.getCode().equals(tradeStatus)) {
                return "订单未支付或支付处理中";
            }
            BigDecimal alipayAmount = new BigDecimal(queryResponse.getTotalAmount());
            if (order.getAmount().compareTo(alipayAmount) != 0) {
                log.error("查询返回金额不一致 order={} alipay={}", order.getAmount(), alipayAmount);
                return "金额校验失败";
            }
            order.setOrderNo(queryResponse.getTradeNo());
            order.setPaymentStatus("PAID");
            order.setUpdatedAt(LocalDateTime.now());
            membershipOrderMapper.updateById(order);
            userApi.grantVip(order.getUserId(),
                MembershipDuration.toPeriod(order.getDurationType(), order.getDurationValue()));
            refreshSessionIfActive(order.getUserId());
            log.info("同步返回处理成功，会员开通 user={} outTradeNo={}", order.getUserId(), outTradeNo);
            return "success";
        } catch (AlipayApiException e) {
            log.error("支付宝查询接口异常 outTradeNo={}", outTradeNo, e);
            return "支付状态查询异常";
        }
    }

    /**
     * 查询 membership_order 表确认用户存在有效 VIP 后刷新 Session。
     */
    private void refreshSessionIfActive(Long userId) {
        MembershipOrder activeOrder = membershipOrderMapper.selectActivePaidByUserId(userId);
        if (activeOrder == null) {
            return;
        }
        SessionUserDTO sessionDto = userApi.toSessionDto(userId);
        if (sessionDto == null) {
            return;
        }
        authSessionApi.refreshSession(userId, sessionDto);
    }
    private AlipayTradePagePayRequest getAlipayTradePagePayRequest(String outTradeNo, BigDecimal amount) {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", outTradeNo);
        bizContent.put("total_amount", amount);
        bizContent.put("subject", "面试刷题平台会员");
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setReturnUrl(returnUrl);
        request.setBizContent(bizContent.toString());
        request.setNotifyUrl(notifyUrl);
        return request;
    }
}
