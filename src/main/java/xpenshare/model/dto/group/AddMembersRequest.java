package xpenshare.model.dto.group;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AddMembersRequest {

    @NotEmpty(message = "Must provide at least one member to add")
    private List<Long> members;
}

