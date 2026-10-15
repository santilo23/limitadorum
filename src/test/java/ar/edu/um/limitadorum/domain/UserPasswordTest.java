package ar.edu.um.limitadorum.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserPasswordTest {

	@Test
	void passwordShouldNotLeakThroughToString() {
		var user = new User();
		user.setUsername("jdoe");
		user.setPassword("Secreto123");

		assertThat(user.toString()).doesNotContain("Secreto123");
	}
}
