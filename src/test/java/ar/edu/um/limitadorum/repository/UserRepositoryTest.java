package ar.edu.um.limitadorum.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import ar.edu.um.limitadorum.domain.User;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@Test
	void persistsUserAndGeneratesId() {
		var user = new User();
		user.setUsername("jperez");
		user.setEmail("jperez@um.edu.ar");
		user.setActive(true);

		User saved = userRepository.save(user);

		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getUsername()).isEqualTo("jperez");
		assertThat(saved.getEmail()).isEqualTo("jperez@um.edu.ar");
		assertThat(saved.getActive()).isTrue();
	}

	@Test
	void existsByUsername_whenUsernameTaken_returnsTrue() {
		var user = new User();
		user.setUsername("jperez");
		user.setEmail("jperez@um.edu.ar");
		userRepository.save(user);

		assertThat(userRepository.existsByUsername("jperez")).isTrue();
	}

	@Test
	void existsByUsername_whenUsernameFree_returnsFalse() {
		assertThat(userRepository.existsByUsername("noexiste")).isFalse();
	}

	@Test
	void existsByEmail_whenEmailTaken_returnsTrue() {
		var user = new User();
		user.setUsername("jperez");
		user.setEmail("jperez@um.edu.ar");
		userRepository.save(user);

		assertThat(userRepository.existsByEmail("jperez@um.edu.ar")).isTrue();
	}

	@Test
	void existsByEmail_whenEmailFree_returnsFalse() {
		assertThat(userRepository.existsByEmail("libre@um.edu.ar")).isFalse();
	}
}
