package xpenshare.model.dto.group;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable

@Data
public class AddMembersRequest {

    @NotEmpty(message = "Must provide at least one member to add")
    private List<Long> members;


}

