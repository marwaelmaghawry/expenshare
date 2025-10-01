package xpenshare.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import xpenshare.model.dto.expense.ShareDto;
import xpenshare.model.entity.ExpenseShareEntity;

@Mapper(componentModel = "jsr330")
public interface ExpenseShareMapper {

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "shareAmount", source = "shareAmount")
    @Mapping(target = "percent", ignore = true)
    ShareDto toDto(ExpenseShareEntity entity);
}
