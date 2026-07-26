package com.coco8talk.pm.payment.mapper;

import com.coco8talk.pm.payment.model.entity.MembershipOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

/**
 * @author coco8talk
 * @description 针对表【membership_order】的数据库操作Mapper
 * @createDate 2025-10-20 09:35:19
 * @Entity com.coco8talk.pm.payment.model.entity.MembershipOrder
 */
public interface MembershipOrderMapper extends BaseMapper<MembershipOrder> {
    
    /**
     * 根据商户订单号查询订单金额
     *
     * @param outTradeNo 商户订单号
     * @return 订单金额
     */
    @Select("SELECT * FROM membership_order WHERE out_order_no = #{outTradeNo}")
    MembershipOrder selectByOutTradeNo(String outTradeNo);

    /**
     * 查询用户当前有效的已支付会员订单。
     * 条件：userId、payment_status='PAID'、当前时间在 start_time ~ end_time 区间内。
     *
     * @param userId 用户 ID
     * @return 有效会员订单，无则返回 null
     */
    @Select("SELECT * FROM membership_order WHERE user_id = #{userId} AND payment_status = 'PAID' AND start_time <= NOW() AND end_time >= NOW() ORDER BY end_time DESC LIMIT 1")
    MembershipOrder selectActivePaidByUserId(@Param("userId") Long userId);
}


