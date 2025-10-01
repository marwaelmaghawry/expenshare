package xpenshare.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import xpenshare.model.dto.group.GroupDto;
import xpenshare.model.entity.GroupEntity;
import xpenshare.model.entity.GroupMemberEntity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "jsr330")
public interface GroupMapper {

    @Mapping(target = "groupId", source = "groupId")
    @Mapping(target = "members", source = "members", qualifiedByName = "mapMembersSet")
    GroupDto toDto(GroupEntity entity);

    @Named("mapMembersSet")
    default List<Long> mapMembersSet(Set<GroupMemberEntity> members) {
        if (members == null) return null;
        return members.stream()
                .map(m -> m.getUser().getUserId())
                .collect(Collectors.toList());
    }
}
