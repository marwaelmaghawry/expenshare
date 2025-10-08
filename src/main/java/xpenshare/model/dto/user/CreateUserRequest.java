package xpenshare.model.dto.user;

import jakarta.validation.constraints.*;
import lombok.*;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {

    @NotBlank
    private String name;

    @Email(message = "Invalid email format")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.com$")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$")
    private String mobileNumber;

    @NotNull
    private AddressDto address; // <-- use standalone AddressDto
}
