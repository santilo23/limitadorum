package ar.edu.um.limitadorum.dto;

public record LoginResponse(
		String token,
		String message
) {

	public static final String DEFAULT_SUCCESS_MESSAGE = "Inicio de sesión exitoso";

	public LoginResponse(String token) {
		this(token, DEFAULT_SUCCESS_MESSAGE);
	}
}
