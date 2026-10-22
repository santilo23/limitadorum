package ar.edu.um.limitadorum.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginRequest(
		String username,
		String password,
		@JsonProperty("rememberMe") Boolean rememberMe
) {

	public LoginRequest(String username, String password) {
		this(username, password, false);
	}

	public boolean isRememberMe() {
		return Boolean.TRUE.equals(rememberMe);
	}
}
