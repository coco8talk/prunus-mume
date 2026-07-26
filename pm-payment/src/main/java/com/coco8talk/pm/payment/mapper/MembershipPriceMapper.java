package com.coco8talk.pm.payment.mapper;

import com.coco8talk.pm.payment.model.entity.MembershipPrice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
* @author ASUS
* @description 针对表【membership_price(会员定价配置表)】的数据库操作Mapper
* @createDate 2025-10-20 21:51:11
* @Entity com.coco8talk.pm.payment.model.entity.MembershipPrice
*/
public interface MembershipPriceMapper extends BaseMapper<MembershipPrice> {
    
    /**
     * 根据会员等级、币种、时长类型和时长值查询对应的价格
     * @param levelId 会员等级ID
     * @param currency 币种
     * @param durationType 时长类型
     * @param durationValue 时长值
     * @return 对应的价格
     */
    @Select("SELECT amount " +
        "from membership_price " +
        "where level_id = #{levelId} " +
        "and currency = #{currency} " +
        "and duration_type = #{durationType} " +
        "and duration_value = #{durationValue} " +
        "and status = 1")
    BigDecimal selectPriceByParam(Byte levelId, String currency, String durationType, Integer durationValue);
}




