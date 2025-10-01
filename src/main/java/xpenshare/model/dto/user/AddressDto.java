package xpenshare.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDto {

    @NotBlank(message = "Line1 is required when address is present")
    @Size(max = 150)
    private String line1;

    private String line2;

    @Size(max = 80)
    private String city;

    @Size(max = 80)
    private String state;

    @Size(max = 20)
    private String postalCode;

    @Size(min = 2, max = 2, message = "Country must be ISO alpha-2 (e.g., EG, SA)")
    private String country;
}

