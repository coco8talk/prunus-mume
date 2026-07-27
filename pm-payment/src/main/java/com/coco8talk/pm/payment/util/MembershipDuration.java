package com.coco8talk.pm.payment.util;

import com.coco8talk.pm.common.exception.BizException;
import com.coco8talk.pm.common.result.http.HttpStatusEnum;

import java.time.Period;

/** 会员时长换算：durationValue 视为月数（TRIAL 视为天数）。 */
public final class MembershipDuration {
    private MembershipDuration() {}

    public static Period toPeriod(String durationType, int durationValue) {
        if (durationType == null) {
            throw new BizException(HttpStatusEnum.BAD_REQUEST.getCode(), "无效的会员时长类型");
        }
        return switch (durationType) {
            case "MONTH", "QUARTER", "YEAR" -> Period.ofMonths(durationValue);
            case "TRIAL" -> Period.ofDays(durationValue);
            default -> throw new BizException(HttpStatusEnum.BAD_REQUEST.getCode(), "无效的会员时长类型");
        };
    }
}
