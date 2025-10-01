package xpenshare.model.dto.user;

import lombok.*;
import java.time.Instant;

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

