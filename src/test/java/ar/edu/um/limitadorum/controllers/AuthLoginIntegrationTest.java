package ar.edu.um.limitadorum.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import ar.edu.um.limitadorum.domain.User;
import ar.edu.um.limitadorum.dto.RegisterRequest;
import ar.edu.um.limitadorum.repository.UserRepository;
import ar.edu.um.limitadorum.security.JwtService;
import ar.edu.um.limitadorum.services.RegistrationService;
import io.jsonwebtoken.Claims;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthLoginIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private RegistrationService registrationService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private JwtService jwtService;

	@BeforeEach
	void setUp() {
		// Registramos un usuario de prueba para autenticación
		RegisterRequest registerRequest = new RegisterRequest(
				"testuser",
				"testuser@um.edu.ar",
				"Password123!",
				"Test",
				"User",
				"+5492615559999",
				"Av. San Martin 1000"
		);
		registrationService.register(registerRequest);
	}

	@Test
	@DisplayName("Login exitoso con rememberMe=false: 200 OK, mensaje exitoso y token con expiración normal (24h)")
	void login_withValidCredentials_rememberMeFalse_shouldReturn200AndValidJwt() throws Exception {
		String loginJson = """
				{
				  "username": "testuser",
				  "password": "Password123!",
				  "rememberMe": false
				}
				""";

		long before = System.currentTimeMillis();
		MvcResult result = mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginJson))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.message").value("Inicio de sesión exitoso"))
				.andExpect(jsonPath("$.token").isString())
				.andExpect(jsonPath("$.password").doesNotExist())
				.andReturn();

		String responseBody = result.getResponse().getContentAsString();
		String token = JsonPath.read(responseBody, "$.token");

		// Verificación de los claims del JWT (Requisito 4 estricto)
		Claims claims = jwtService.extractAllClaims(token);
		assertThat(claims.get("username", String.class)).isEqualTo("testuser");
		assertThat(claims.getExpiration()).isNotNull();
		assertThat(claims.keySet()).containsExactlyInAnyOrder("username", "exp");
		assertThat(claims.getSubject()).isNull();
		assertThat(claims.getIssuedAt()).isNull();

		// Verificación de expiración normal (24 horas)
		Date expiration = claims.getExpiration();
		long expected24h = 86_400_000L;
		assertThat(expiration.getTime()).isBetween(before + expected24h - 5000L, before + expected24h + 5000L);
	}

	@Test
	@DisplayName("Login exitoso con rememberMe=true: 200 OK y token con expiración de 1 año")
	void login_withValidCredentials_rememberMeTrue_shouldReturn200AndOneYearJwt() throws Exception {
		String loginJson = """
				{
				  "username": "testuser",
				  "password": "Password123!",
				  "rememberMe": true
				}
				""";

		long before = System.currentTimeMillis();
		MvcResult result = mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Inicio de sesión exitoso"))
				.andExpect(jsonPath("$.token").isString())
				.andReturn();

		String responseBody = result.getResponse().getContentAsString();
		String token = JsonPath.read(responseBody, "$.token");

		Claims claims = jwtService.extractAllClaims(token);
		Date expiration = claims.getExpiration();
		long expectedOneYear = 31_536_000_000L; // 365 días en ms
		assertThat(expiration.getTime()).isBetween(before + expectedOneYear - 5000L, before + expectedOneYear + 5000L);
	}

	@Test
	@DisplayName("Login con contraseña incorrecta: 401 Unauthorized en formato RFC 9457")
	void login_withWrongPassword_shouldReturn401ProblemDetailRfc9457() throws Exception {
		String loginJson = """
				{
				  "username": "testuser",
				  "password": "WrongPassword!"
				}
				""";

		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginJson))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Unauthorized"))
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.detail").value("Error de usuario y/o de password."))
				.andExpect(jsonPath("$.type").value("https://limitador.edu.ar/problems/invalid-credentials"))
				.andExpect(jsonPath("$.instance").value("/auth/login"))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.stackTrace").doesNotExist());
	}

	@Test
	@DisplayName("Login con usuario inexistente: 401 idéntico sin filtrar si existe el username")
	void login_withNonexistentUser_shouldReturn401ProblemDetailRfc9457() throws Exception {
		String loginJson = """
				{
				  "username": "noexiste",
				  "password": "Password123!"
				}
				""";

		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginJson))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Unauthorized"))
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.detail").value("Error de usuario y/o de password."));
	}

	@Test
	@DisplayName("Login con usuario inactivo: 401 Unauthorized sin revelar estado de cuenta")
	void login_withInactiveUser_shouldReturn401ProblemDetail() throws Exception {
		User user = userRepository.findByUsername("testuser").orElseThrow();
		user.setActive(false);
		userRepository.save(user);

		String loginJson = """
				{
				  "username": "testuser",
				  "password": "Password123!"
				}
				""";

		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginJson))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.detail").value("Error de usuario y/o de password."));
	}

	@Test
	@DisplayName("Login con campos nulos o vacíos: 401 Unauthorized RFC 9457")
	void login_withEmptyOrBlankCredentials_shouldReturn401ProblemDetail() throws Exception {
		String emptyJson = """
				{
				  "username": "",
				  "password": ""
				}
				""";

		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(emptyJson))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.detail").value("Error de usuario y/o de password."));
	}
}
