package xpenshare.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import xpenshare.model.dto.settlement.SettlementDto;
import xpenshare.model.entity.SettlementEntity;

@Mapper(componentModel = "jsr330")
public interface SettlementMapper {

    @Mapping(target = "settlementId", source = "id")
    @Mapping(target = "groupId", source = "group.groupId")
    @Mapping(target = "fromUserId", source = "fromUser.userId")
    @Mapping(target = "toUserId", source = "toUser.userId")
    SettlementDto toDto(SettlementEntity entity);
}
