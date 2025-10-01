package xpenshare.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import xpenshare.model.dto.expense.ExpenseDto;
import xpenshare.model.entity.ExpenseEntity;

@Mapper(componentModel = "jsr330", uses = {ExpenseShareMapper.class})
public interface ExpenseMapper {

    @Mapping(target = "expenseId", source = "id")
    @Mapping(target = "groupId", source = "group.groupId")
    @Mapping(target = "paidById", source = "paidBy.userId")
    @Mapping(target = "split", source = "shares")
    ExpenseDto toDto(ExpenseEntity entity);
}
