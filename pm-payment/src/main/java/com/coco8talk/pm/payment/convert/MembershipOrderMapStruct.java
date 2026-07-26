package com.coco8talk.pm.payment.convert;

import com.coco8talk.pm.payment.model.dto.MembershipOrderCreateDTO;
import com.coco8talk.pm.payment.model.entity.MembershipOrder;
import com.coco8talk.pm.payment.model.vo.MembershipOrderCreateVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * @author coco8talk
 * @since 2025/10/20 21:59
 **/
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MembershipOrderMapStruct {
    MembershipOrderMapStruct INSTANCE = Mappers.getMapper(MembershipOrderMapStruct.class);
    
    /**
     * 将 MembershipOrderCreateDTO 对象转换为 MembershipOrder
     *
     * @param membershipOrderCreateDTO 待转换的MembershipOrderCreateDTO对象
     * @return 转化完成的MembershipOrder对象
     */
    MembershipOrder createDtoToEntity(MembershipOrderCreateDTO membershipOrderCreateDTO);
    
    /**
     * 将 MembershipOrder 实体转换为 MembershipOrderCreateVO 视图对象
     *
     * @param membershipOrder 待转换的MembershipOrder实体对象
     * @return 转化完成的MembershipOrderCreateVO视图对象
     */
    @Mapping(target = "outTradeNo", source = "outOrderNo")
    @Mapping(target = "status", source = "paymentStatus")
    @Mapping(target = "createTime", source = "createdAt")
    MembershipOrderCreateVO entityToCreateVo(MembershipOrder membershipOrder);
}
