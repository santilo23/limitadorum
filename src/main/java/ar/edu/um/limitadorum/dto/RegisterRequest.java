package ar.edu.um.limitadorum.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank(message = "username is required")
		String username,

		@NotBlank(message = "email is required")
		@Email(message = "email must be valid")
		String email,

		@NotBlank(message = "password is required")
		@Size(min = 8, message = "password must be at least 8 characters")
		String password,

		@NotBlank(message = "firstName is required")
		String firstName,

		String lastName,

		@NotBlank(message = "phoneNumber is required")
		String phoneNumber,

		String address) {
}
