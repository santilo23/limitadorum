package ar.edu.um.limitadorum.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;

class JwtServiceTest {

	private static final String SECRET_KEY_256_BITS = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
	private static final long NORMAL_EXPIRATION_MS = 86_400_000L; // 24 horas
	private static final long REMEMBER_ME_EXPIRATION_MS = 31_536_000_000L; // 365 días

	private JwtService jwtService;

	@BeforeEach
	void setUp() {
		jwtService = new JwtServiceImpl(SECRET_KEY_256_BITS, NORMAL_EXPIRATION_MS, REMEMBER_ME_EXPIRATION_MS);
	}

	@Test
	@DisplayName("generateToken: debe contener ÚNICAMENTE username y exp en el payload")
	void generateToken_shouldContainOnlyUsernameAndExpirationClaims() {
		// Arrange
		String username = "usuario_test";

		// Act
		String token = jwtService.generateToken(username, false);
		Claims claims = jwtService.extractAllClaims(token);

		// Assert
		assertThat(claims.get("username", String.class)).isEqualTo("usuario_test");
		assertThat(claims.getExpiration()).isNotNull();

		// Requisito 4 estricto: sin sub, iss, aud, iat, jti ni datos de usuario adicionales
		assertThat(claims.getSubject()).isNull();
		assertThat(claims.getIssuedAt()).isNull();
		assertThat(claims.getIssuer()).isNull();
		assertThat(claims.getAudience()).isNull();
		assertThat(claims.getId()).isNull();

		assertThat(claims.keySet()).containsExactlyInAnyOrder("username", "exp");
	}

	@Test
	@DisplayName("generateToken con rememberMe=false: la expiración debe coincidir con la duración normal (24h)")
	void generateToken_rememberMeFalse_shouldSetNormalExpiration() {
		// Arrange
		long before = System.currentTimeMillis();

		// Act
		String token = jwtService.generateToken("santi", false);
		Date expiration = jwtService.extractExpiration(token);
		long after = System.currentTimeMillis();

		// Assert: la expiración debe estar dentro de la ventana de tiempo esperada (+24h)
		long expectedMin = before + NORMAL_EXPIRATION_MS - 2000L;
		long expectedMax = after + NORMAL_EXPIRATION_MS + 2000L;
		assertThat(expiration.getTime()).isBetween(expectedMin, expectedMax);
	}

	@Test
	@DisplayName("generateToken con rememberMe=true: la expiración debe ser de 1 año (365 días)")
	void generateToken_rememberMeTrue_shouldSetOneYearExpiration() {
		// Arrange
		long before = System.currentTimeMillis();

		// Act
		String token = jwtService.generateToken("santi", true);
		Date expiration = jwtService.extractExpiration(token);
		long after = System.currentTimeMillis();

		// Assert: la expiración debe ser de 1 año (+365 días)
		long expectedMin = before + REMEMBER_ME_EXPIRATION_MS - 2000L;
		long expectedMax = after + REMEMBER_ME_EXPIRATION_MS + 2000L;
		assertThat(expiration.getTime()).isBetween(expectedMin, expectedMax);
	}

	@Test
	@DisplayName("extractUsername: extrae correctamente el nombre de usuario del claim")
	void extractUsername_shouldReturnCorrectUsername() {
		// Arrange
		String token = jwtService.generateToken("juanperez", false);

		// Act
		String extracted = jwtService.extractUsername(token);

		// Assert
		assertThat(extracted).isEqualTo("juanperez");
	}

	@Test
	@DisplayName("isTokenValid: retorna true si coincide el username y false si difiere")
	void isTokenValid_shouldCheckUsernameCorrectly() {
		// Arrange
		String token = jwtService.generateToken("maria", false);

		// Act & Assert
		assertThat(jwtService.isTokenValid(token, "maria")).isTrue();
		assertThat(jwtService.isTokenValid(token, "otra_persona")).isFalse();
		assertThat(jwtService.isTokenValid(token, null)).isFalse();
	}

	@Test
	@DisplayName("isTokenValid: retorna false ante tokens corruptos o manipulados")
	void isTokenValid_withTamperedToken_shouldReturnFalse() {
		// Arrange
		String token = jwtService.generateToken("maria", false);
		String tamperedToken = token + "xyz";

		// Act & Assert
		assertThat(jwtService.isTokenValid(tamperedToken, "maria")).isFalse();
		assertThat(jwtService.isTokenValid("token-invalido", "maria")).isFalse();
	}

	@Test
	@DisplayName("generateToken con username nulo o vacío debe lanzar IllegalArgumentException")
	void generateToken_withNullOrBlankUsername_shouldThrowException() {
		assertThatThrownBy(() -> jwtService.generateToken(null, false))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> jwtService.generateToken("   ", false))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
