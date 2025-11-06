package org.fsm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 255, message = "Full name must be at most 255 characters")
    private String fullName;

    // Optional, but if provided must be digits 10-15
    @Pattern(regexp = "^(?:\\+?\\d{10,15})?$", message = "Phone must be 10–15 digits (optional)")
    private String phone;

    @NotBlank(message = "Default address is required")
    private String defaultAddress;

    private String avatarUrl;
}
