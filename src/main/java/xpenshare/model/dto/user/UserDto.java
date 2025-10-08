package xpenshare.model.dto.user;
import io.micronaut.serde.annotation.Serdeable;

import lombok.*;
import java.time.Instant;

@Serdeable

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long userId;
    private String name;
    private String email;
    private String mobileNumber;
    private AddressDto address;
    private Instant createdAt;
}

