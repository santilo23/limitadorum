package ar.edu.um.limitadorum.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import ar.edu.um.limitadorum.domain.User;
import ar.edu.um.limitadorum.dto.LoginRequest;
import ar.edu.um.limitadorum.dto.LoginResponse;
import ar.edu.um.limitadorum.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private JwtService jwtService;

	@Mock
	private Authentication authentication;

	@InjectMocks
	private AuthServiceImpl authService;

	@Test
	@DisplayName("login: credenciales válidas deben autenticar y retornar token con mensaje de éxito")
	void login_withValidCredentials_shouldReturnTokenAndSuccessMessage() {
		// Arrange
		LoginRequest request = new LoginRequest("santi", "password123", true);
		User user = new User();
		user.setUsername("santi");

		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(authentication);
		when(authentication.getPrincipal()).thenReturn(user);
		when(jwtService.generateToken("santi", true)).thenReturn("mocked.jwt.token");

		// Act
		LoginResponse response = authService.login(request);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.token()).isEqualTo("mocked.jwt.token");
		assertThat(response.message()).isEqualTo(LoginResponse.DEFAULT_SUCCESS_MESSAGE);
		verify(jwtService).generateToken("santi", true);
	}

	@Test
	@DisplayName("login: credenciales inválidas deben propagar BadCredentialsException sin revelar el detalle")
	void login_withBadCredentials_shouldThrowBadCredentialsException() {
		// Arrange
		LoginRequest request = new LoginRequest("santi", "wrong_password", false);
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new BadCredentialsException("Bad credentials"));

		// Act & Assert
		assertThatThrownBy(() -> authService.login(request))
				.isInstanceOf(BadCredentialsException.class);
	}

	@Test
	@DisplayName("login: credenciales nulas o vacías deben lanzar BadCredentialsException directamente")
	void login_withNullOrBlankCredentials_shouldThrowBadCredentialsException() {
		assertThatThrownBy(() -> authService.login(null))
				.isInstanceOf(BadCredentialsException.class)
				.hasMessage("Error de usuario y/o de password.");

		assertThatThrownBy(() -> authService.login(new LoginRequest(null, "pass")))
				.isInstanceOf(BadCredentialsException.class)
				.hasMessage("Error de usuario y/o de password.");

		assertThatThrownBy(() -> authService.login(new LoginRequest("user", null)))
				.isInstanceOf(BadCredentialsException.class)
				.hasMessage("Error de usuario y/o de password.");

		assertThatThrownBy(() -> authService.login(new LoginRequest("", "")))
				.isInstanceOf(BadCredentialsException.class)
				.hasMessage("Error de usuario y/o de password.");
	}
}
