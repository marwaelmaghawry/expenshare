package xpenshare.model.dto.group;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class GroupDto {
    private Long groupId;
    private String name;
    private List<Long> members;
    private Instant createdAt;
}


