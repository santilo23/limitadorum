package ar.edu.um.limitadorum.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import ar.edu.um.limitadorum.domain.Role;
import ar.edu.um.limitadorum.domain.User;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRoleRelationTest {

	private final UserRepository userRepository;

	private final RoleRepository roleRepository;

	@Autowired
	public UserRoleRelationTest(UserRepository userRepository, RoleRepository roleRepository) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
	}

	/**
	 * Test that a User with an assigned Role can be persisted and retrieved
	 * correctly, ensuring the many-to-many relationship is maintained through the
	 * persistence layer.
	 * 
	 */
	@Test
	void userAssignedRoleRoundTripsThroughPersistence() {
		Role admin = roleRepository.save(new Role("Administrador"));
		User user = new User();
		user.setUsername("jperez");
		user.setEmail("jperez@um.edu.ar");
		user.setActive(true);
		user.getRoles().add(admin);
		userRepository.saveAndFlush(user);

		User loaded = userRepository.findById(user.getId()).orElseThrow();
		assertThat(loaded.getRoles()).extracting(Role::getDescription).containsExactly("Administrador");
	}
}
