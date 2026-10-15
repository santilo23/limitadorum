package ar.edu.um.limitadorum.dto;

import java.util.Set;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UserDto(
		Long id,
		String username,
		String email,
		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String password,
		Boolean active,
		Set<RoleDto> roles,
		UserDataDto userData
) {

	public UserDto(Long id, String username, String email, String password, Boolean active, UserDataDto userData) {
		this(id, username, email, password, active, null, userData);
	}
}
