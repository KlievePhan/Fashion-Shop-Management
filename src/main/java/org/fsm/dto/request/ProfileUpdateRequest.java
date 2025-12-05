package org.fsm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ProfileUpdateRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    // Phone validation: Vietnamese phone numbers (10-11 digits, may start with 0 or +84)
    @Pattern(regexp = "^(?:0[3|5|7|8|9][0-9]{8}|\\+84[3|5|7|8|9][0-9]{8}|[3|5|7|8|9][0-9]{8})?$", 
             message = "Invalid phone number. Please enter a valid Vietnamese phone number (10-11 digits)")
    private String phone;

    @NotBlank(message = "Address is required")
    @Size(min = 10, max = 500, message = "Address must be between 10 and 500 characters")
    private String defaultAddress;

    // Keep this to store the URL after processing
    @Size(max = 512, message = "Avatar URL is too long")
    private String avatarUrl;

    // Add this to handle file upload
    private MultipartFile avatar; // Matches <input type="file" name="avatar">
}