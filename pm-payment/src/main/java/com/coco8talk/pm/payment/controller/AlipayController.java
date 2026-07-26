package com.coco8talk.pm.payment.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.coco8talk.pm.common.Result;
import com.coco8talk.pm.common.HttpStatusEnum;
import com.coco8talk.pm.common.ThrowUtils;
import com.coco8talk.pm.payment.model.dto.MembershipOrderCreateDTO;
import com.coco8talk.pm.payment.model.vo.MembershipOrderCreateVO;
import com.coco8talk.pm.payment.service.MembershipOrderService;
import com.coco8talk.pm.common.util.ObjectMyUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

/**
 * Alipay Trade Page Pay Controller
 *
 * @author coco8talk
 * @since 2025/10/20 10:00
 */
@RestController
@RequestMapping("/alipay")
@Log4j2
@Tag(name = "支付宝支付控制器", description = "提供会员订单创建、支付宝网页支付及异步和同步支付结果处理能力")
public class AlipayController {
    
    private final MembershipOrderService membershipOrderService;
    
    public AlipayController(MembershipOrderService membershipOrderService) {
        this.membershipOrderService = membershipOrderService;
    }
    
    /**
     * 提交会员订单
     *
     * @param membershipOrderCreateDTO 会员订单创建请求参数
     * @return 会员订单创建响应视图对象
     */
    @PostMapping("/submit")
    @Operation(summary = "提交会员订单", description = "当前登录用户按会员等级、时长和币种创建待支付会员订单")
    public Result<MembershipOrderCreateVO> createOrder(@RequestBody MembershipOrderCreateDTO membershipOrderCreateDTO) {
        ObjectMyUtil.throwIfAllFieldsAreEmptyOrBlank(membershipOrderCreateDTO, HttpStatusEnum.BAD_REQUEST, "请求参数不能为空");
        boolean login = StpUtil.isLogin();
        ThrowUtils.throwIfFalse(login, HttpStatusEnum.UNAUTHORIZED, "用户未登录");
        return membershipOrderService.createOrder(membershipOrderCreateDTO);
    }
    
    /**
     * 支付会员订单
     *
     * @param outTradeNo 商户订单号
     * @return 支付页面链接
     */
    @GetMapping(value = "/pay/{outTradeNo}", produces = "text/html;charset=UTF-8")
    @Operation(summary = "支付会员订单", description = "校验订单归属后向支付宝发起网页支付并返回支付页面内容")
    public String pay(@PathVariable(value = "outTradeNo") String outTradeNo){
        boolean login = StpUtil.isLogin();
        ThrowUtils.throwIfFalse(login, HttpStatusEnum.UNAUTHORIZED, "用户未登录");
        return membershipOrderService.pay(outTradeNo);
    }
    
    /**
     * 支付宝支付回调处理
     *
     * @param request HttpServletRequest对象，包含支付宝回调请求参数
     * @return 处理结果，true表示成功，false表示失败
     */
    @PostMapping("/callback")
    public String alipayCallback(HttpServletRequest request) {
        ThrowUtils.throwIfNull(request, HttpStatusEnum.BAD_REQUEST, "请求不能为空");
        return membershipOrderService.alipayCallback(request);
    }

    /**
     * 支付宝同步返回处理（支付完成后浏览器跳转回调）
     *
     * @param request HttpServletRequest对象，包含支付宝同步返回参数
     * @return 处理结果
     */
    @GetMapping("/return")
    public String alipayReturn(HttpServletRequest request) {
        ThrowUtils.throwIfNull(request, HttpStatusEnum.BAD_REQUEST, "请求不能为空");
        String result = membershipOrderService.handleReturn(request);
        if ("success".equals(result)) {
            return "<html><body><script>window.location.href='/';</script><p>支付成功，正在跳转...</p></body></html>";
        }
        return "<html><body><h2>支付确认失败</h2><p>" + result + "</p></body></html>";
    }
}
