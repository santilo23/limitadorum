package ar.edu.um.limitadorum.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import ar.edu.um.limitadorum.domain.User;

@DataJpaTest
class UserPasswordPersistenceTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	void persistsBcryptHash() {
		String hash = "$2a$10$abcdefghijklmnopqrstuuXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";

		var user = new User();
		user.setUsername("conhash");
		user.setEmail("conhash@um.edu.ar");
		user.setActive(true);
		user.setPassword(hash);

		User saved = userRepository.saveAndFlush(user);
		entityManager.clear();

		User reloaded = userRepository.findById(saved.getId()).orElseThrow();

		assertThat(reloaded.getPassword()).isEqualTo(hash);
	}

	@Test
	void legacyUsersWithoutPasswordStillPersist() {
		var user = new User();
		user.setUsername("legacy");
		user.setEmail("legacy@um.edu.ar");
		user.setActive(true);
		user.setPassword(null);

		User saved = userRepository.saveAndFlush(user);

		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getPassword()).isNull();
	}
}
