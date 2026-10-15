package ar.edu.um.limitadorum.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import ar.edu.um.limitadorum.domain.User;
import ar.edu.um.limitadorum.domain.UserData;
import ar.edu.um.limitadorum.dto.RegisterRequest;
import ar.edu.um.limitadorum.exception.DuplicateUserException;
import ar.edu.um.limitadorum.mapper.AuthMapperImpl;
import ar.edu.um.limitadorum.mapper.RoleMapperImpl;
import ar.edu.um.limitadorum.mapper.UserDataMapperImpl;
import ar.edu.um.limitadorum.mapper.UserMapperImpl;
import ar.edu.um.limitadorum.repository.RoleRepository;
import ar.edu.um.limitadorum.repository.UserDataRepository;
import ar.edu.um.limitadorum.repository.UserRepository;

@DataJpaTest
@Import({ RegistrationServiceImpl.class, UserServiceImpl.class,
		AuthMapperImpl.class, UserMapperImpl.class, UserDataMapperImpl.class, RoleMapperImpl.class,
		RegistrationIntegrationTest.TestEncoderConfig.class })
class RegistrationIntegrationTest {

	@TestConfiguration
	static class TestEncoderConfig {
		@Bean
		PasswordEncoder passwordEncoder() {
			return new BCryptPasswordEncoder();
		}
	}

	@Autowired
	private RegistrationService registrationService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserDataRepository userDataRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private RegisterRequest request(String username, String email) {
		return new RegisterRequest(username, email, "Secreto123",
				"Juan", "Perez", "+5492615551234", "Calle Falsa 123");
	}

	@Test
	void register_shouldPersistBothEntitiesLinkedAndHashPassword() {
		User saved = registrationService.register(request("jperez", "jperez@um.edu.ar"));
		entityManager.clear();

		User reloaded = userRepository.findById(saved.getId()).orElseThrow();
		assertThat(reloaded.getUsername()).isEqualTo("jperez");
		assertThat(reloaded.getEmail()).isEqualTo("jperez@um.edu.ar");
		assertThat(reloaded.getActive()).isTrue();
		assertThat(reloaded.getPassword()).isNotEqualTo("Secreto123");
		assertThat(reloaded.getPassword()).matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
		assertThat(passwordEncoder.matches("Secreto123", reloaded.getPassword())).isTrue();

		assertThat(reloaded.getUserData()).isNotNull();
		UserData data = reloaded.getUserData();
		assertThat(data.getId()).isNotNull();
		assertThat(data.getFirstName()).isEqualTo("Juan");
		assertThat(data.getLastName()).isEqualTo("Perez");
		assertThat(data.getPhoneNumber()).isEqualTo("+5492615551234");
		assertThat(data.getAddress()).isEqualTo("Calle Falsa 123");

		assertThat(userDataRepository.findById(data.getId())).isPresent();
		assertThat(userRepository.count()).isEqualTo(1);
		assertThat(userDataRepository.count()).isEqualTo(1);
	}

	@Test
	void register_shouldAssignUserRoleCreatingItWhenMissing() {
		User saved = registrationService.register(request("jperez", "jperez@um.edu.ar"));
		entityManager.clear();

		User reloaded = userRepository.findById(saved.getId()).orElseThrow();
		assertThat(reloaded.getRoles())
				.extracting(role -> role.getDescription())
				.containsExactly("USER");
		assertThat(roleRepository.findAll()).hasSize(1);
	}

	@Test
	void register_whenDuplicateUsername_shouldThrowAndNotPersist() {
		registrationService.register(request("jperez", "jperez@um.edu.ar"));

		assertThatThrownBy(() -> registrationService.register(request("jperez", "otro@um.edu.ar")))
				.isInstanceOf(DuplicateUserException.class)
				.hasMessageContaining("username 'jperez' is already taken");

		assertThat(userRepository.count()).isEqualTo(1);
	}

	@Test
	void register_whenDuplicateEmail_shouldThrowAndNotPersist() {
		registrationService.register(request("jperez", "jperez@um.edu.ar"));

		assertThatThrownBy(() -> registrationService.register(request("otro", "jperez@um.edu.ar")))
				.isInstanceOf(DuplicateUserException.class)
				.hasMessageContaining("email 'jperez@um.edu.ar' is already registered");

		assertThat(userRepository.count()).isEqualTo(1);
	}
}
