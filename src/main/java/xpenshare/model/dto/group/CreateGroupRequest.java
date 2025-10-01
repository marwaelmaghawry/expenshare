package xpenshare.model.dto.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateGroupRequest {

    @NotBlank
    @Size(min = 1, max = 100)
    private String name;

    @NotEmpty(message = "Group must have at least one member")
    private List<Long> members;
}
