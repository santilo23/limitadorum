package ar.edu.um.limitadorum.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ar.edu.um.limitadorum.domain.User;
import ar.edu.um.limitadorum.domain.UserData;
import ar.edu.um.limitadorum.dto.RegisterRequest;
import ar.edu.um.limitadorum.exception.DuplicateUserException;
import ar.edu.um.limitadorum.exception.GlobalExceptionHandler;
import ar.edu.um.limitadorum.mapper.AuthMapperImpl;
import ar.edu.um.limitadorum.mapper.RoleMapperImpl;
import ar.edu.um.limitadorum.mapper.UserDataMapperImpl;
import ar.edu.um.limitadorum.mapper.UserMapperImpl;
import ar.edu.um.limitadorum.services.RegistrationService;

@WebMvcTest(AuthController.class)
@Import({ GlobalExceptionHandler.class, UserMapperImpl.class, RoleMapperImpl.class, UserDataMapperImpl.class, AuthMapperImpl.class })
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private RegistrationService registrationService;

	@MockitoBean
	private ar.edu.um.limitadorum.services.AuthService authService;

	private static final String VALID_BODY = """
			{
			  "username": "jperez",
			  "email": "jperez@um.edu.ar",
			  "password": "Secreto123",
			  "firstName": "Juan",
			  "lastName": "Perez",
			  "phoneNumber": "+5492615551234",
			  "address": "Calle Falsa 123"
			}
			""";

	private User registeredUser() {
		User user = new User();
		user.setId(1L);
		user.setUsername("jperez");
		user.setEmail("jperez@um.edu.ar");
		user.setPassword("$2a$10$abcdefghijklmnopqrstuuXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		user.setActive(true);
		user.setUserData(new UserData(10L, "Juan", "Perez", "Calle Falsa 123", "+5492615551234", null));
		return user;
	}

	@Test
	void register_withValidData_shouldReturn201AndUserWithoutPassword() throws Exception {
		when(registrationService.register(any(RegisterRequest.class))).thenReturn(registeredUser());

		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_BODY))
				.andExpect(status().isCreated())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.username").value("jperez"))
				.andExpect(jsonPath("$.email").value("jperez@um.edu.ar"))
				.andExpect(jsonPath("$.active").value(true))
				.andExpect(jsonPath("$.userData.firstName").value("Juan"))
				.andExpect(jsonPath("$.userData.lastName").value("Perez"))
				.andExpect(jsonPath("$.userData.phoneNumber").value("+5492615551234"))
				.andExpect(jsonPath("$.userData.address").value("Calle Falsa 123"))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void register_responseJson_shouldNeverContainPasswordNorHash() throws Exception {
		when(registrationService.register(any(RegisterRequest.class))).thenReturn(registeredUser());

		String json = mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_BODY))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		org.assertj.core.api.Assertions.assertThat(json)
				.doesNotContain("password")
				.doesNotContain("$2a$10$");
	}

	@Test
	void register_withMissingRequiredFields_shouldReturn400ProblemDetail() throws Exception {
		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Bad Request"))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.errors.username").exists())
				.andExpect(jsonPath("$.errors.email").exists())
				.andExpect(jsonPath("$.errors.password").exists())
				.andExpect(jsonPath("$.errors.firstName").exists())
				.andExpect(jsonPath("$.errors.phoneNumber").exists());

		verify(registrationService, never()).register(any());
	}

	@Test
	void register_withInvalidEmail_shouldReturn400ProblemDetail() throws Exception {
		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "username": "jperez",
								  "email": "not-an-email",
								  "password": "Secreto123",
								  "firstName": "Juan",
								  "phoneNumber": "+5492615551234"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.errors.email").value("email must be valid"));

		verify(registrationService, never()).register(any());
	}

	@Test
	void register_withShortPassword_shouldReturn400ProblemDetail() throws Exception {
		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "username": "jperez",
								  "email": "jperez@um.edu.ar",
								  "password": "short",
								  "firstName": "Juan",
								  "phoneNumber": "+5492615551234"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.password").value("password must be at least 8 characters"));

		verify(registrationService, never()).register(any());
	}

	@Test
	void register_withBlankPhoneNumber_shouldReturn400ProblemDetail() throws Exception {
		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "username": "jperez",
								  "email": "jperez@um.edu.ar",
								  "password": "Secreto123",
								  "firstName": "Juan",
								  "phoneNumber": "  "
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.phoneNumber").exists());

		verify(registrationService, never()).register(any());
	}

	@Test
	void register_withDuplicatedUsername_shouldReturn409ProblemDetail() throws Exception {
		when(registrationService.register(any(RegisterRequest.class)))
				.thenThrow(new DuplicateUserException("username", "username 'jperez' is already taken"));

		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_BODY))
				.andExpect(status().isConflict())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Conflict"))
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.detail").value("username 'jperez' is already taken"))
				.andExpect(jsonPath("$.field").value("username"))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void register_withDuplicatedEmail_shouldReturn409ProblemDetail() throws Exception {
		when(registrationService.register(any(RegisterRequest.class)))
				.thenThrow(new DuplicateUserException("email", "email 'jperez@um.edu.ar' is already registered"));

		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_BODY))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.detail").value("email 'jperez@um.edu.ar' is already registered"))
				.andExpect(jsonPath("$.field").value("email"));
	}

	@Test
	void login_withValidCredentials_shouldReturn200AndTokenWithMessage() throws Exception {
		String validLoginJson = """
				{
				  "username": "jperez",
				  "password": "Secreto123",
				  "rememberMe": false
				}
				""";
		when(authService.login(any(ar.edu.um.limitadorum.dto.LoginRequest.class)))
				.thenReturn(new ar.edu.um.limitadorum.dto.LoginResponse("mocked.jwt.token", "Inicio de sesión exitoso"));

		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validLoginJson))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.token").value("mocked.jwt.token"))
				.andExpect(jsonPath("$.message").value("Inicio de sesión exitoso"))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void login_withInvalidCredentials_shouldReturn401ProblemDetailRfc9457() throws Exception {
		String invalidLoginJson = """
				{
				  "username": "jperez",
				  "password": "WrongPassword"
				}
				""";
		when(authService.login(any(ar.edu.um.limitadorum.dto.LoginRequest.class)))
				.thenThrow(new org.springframework.security.authentication.BadCredentialsException("Error de usuario y/o de password."));

		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(invalidLoginJson))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Unauthorized"))
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.detail").value("Error de usuario y/o de password."))
				.andExpect(jsonPath("$.type").value("https://limitador.edu.ar/problems/invalid-credentials"))
				.andExpect(jsonPath("$.instance").value("/auth/login"))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void login_withNonexistentUser_shouldReturnSame401ProblemDetailWithoutEnumeration() throws Exception {
		String nonexistentUserJson = """
				{
				  "username": "usuario_fantasma",
				  "password": "CualquierPassword"
				}
				""";
		when(authService.login(any(ar.edu.um.limitadorum.dto.LoginRequest.class)))
				.thenThrow(new org.springframework.security.authentication.BadCredentialsException("Error de usuario y/o de password."));

		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(nonexistentUserJson))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Unauthorized"))
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.detail").value("Error de usuario y/o de password."));
	}

	@Test
	void login_withEmptyOrBlankCredentials_shouldReturn401ProblemDetail() throws Exception {
		String emptyCredentialsJson = """
				{
				  "username": "",
				  "password": ""
				}
				""";
		when(authService.login(any(ar.edu.um.limitadorum.dto.LoginRequest.class)))
				.thenThrow(new org.springframework.security.authentication.BadCredentialsException("Error de usuario y/o de password."));

		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(emptyCredentialsJson))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.detail").value("Error de usuario y/o de password."));
	}
}
