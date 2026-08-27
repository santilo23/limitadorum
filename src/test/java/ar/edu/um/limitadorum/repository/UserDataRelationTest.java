package ar.edu.um.limitadorum.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import ar.edu.um.limitadorum.domain.User;
import ar.edu.um.limitadorum.domain.UserData;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserDataRelationTest {

	private final UserRepository userRepository;

	private final TestEntityManager entityManager;

	@Autowired
	public UserDataRelationTest(UserRepository userRepository, TestEntityManager entityManager) {
		this.userRepository = userRepository;
		this.entityManager = entityManager;
	}

	private User newUser() {
		User user = new User();
		user.setUsername("jperez");
		user.setEmail("jperez@um.edu.ar");
		user.setActive(true);
		return user;
	}

	/**
	 * Al guardar el usuario, la cascada debe persistir tambien sus datos
	 * personales, sin necesidad de guardarlos por separado.
	 */
	@Test
	void userDataIsPersistedByCascadeFromUser() {
		User user = newUser();
		user.setUserData(new UserData("Juan", "Perez", "San Martin 123", "2611234567"));

		userRepository.saveAndFlush(user);
		entityManager.clear();

		User loaded = userRepository.findById(user.getId()).orElseThrow();
		assertThat(loaded.getUserData()).isNotNull();
		assertThat(loaded.getUserData().getId()).isNotNull();
		assertThat(loaded.getUserData().getFullName()).isEqualTo("Juan Perez");
		assertThat(loaded.getUserData().getAddress()).isEqualTo("San Martin 123");
		assertThat(loaded.getUserData().getPhoneNumber()).isEqualTo("2611234567");
	}

	/**
	 * El setter de User debe dejar sincronizados ambos lados de la relacion, de
	 * modo que los datos personales apunten de vuelta a su usuario.
	 */
	@Test
	void userDataPointsBackToItsUser() {
		User user = newUser();
		user.setUserData(new UserData("Juan", "Perez", "San Martin 123", "2611234567"));

		userRepository.saveAndFlush(user);
		entityManager.clear();

		User loaded = userRepository.findById(user.getId()).orElseThrow();
		assertThat(loaded.getUserData().getUser().getId()).isEqualTo(loaded.getId());
	}

	/**
	 * La relacion es una composicion: al eliminar el usuario deben eliminarse
	 * tambien sus datos personales, sin quedar filas huerfanas.
	 */
	@Test
	void deletingUserAlsoDeletesItsUserData() {
		User user = newUser();
		user.setUserData(new UserData("Juan", "Perez", "San Martin 123", "2611234567"));
		userRepository.saveAndFlush(user);
		entityManager.clear();

		userRepository.delete(userRepository.findById(user.getId()).orElseThrow());
		userRepository.flush();

		Long remaining = entityManager.getEntityManager()
				.createQuery("select count(d) from UserData d", Long.class)
				.getSingleResult();
		assertThat(remaining).isZero();
	}
}
