package ar.edu.um.limitadorum.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserDataDto(
		Long id,
		String firstName,
		String lastName,
		String address,
		String phoneNumber,
		@JsonProperty("userId") Long userId,
		@JsonProperty(value = "user", access = JsonProperty.Access.WRITE_ONLY) UserReferenceDto user
) {

	public UserDataDto(Long id, String firstName, String lastName, String address, String phoneNumber) {
		this(id, firstName, lastName, address, phoneNumber, null, null);
	}

	public UserDataDto(Long id, String firstName, String lastName, String address, String phoneNumber, Long userId) {
		this(id, firstName, lastName, address, phoneNumber, userId, null);
	}

	public Long resolvedUserId() {
		if (userId != null) {
			return userId;
		}
		return user != null ? user.id() : null;
	}
}
