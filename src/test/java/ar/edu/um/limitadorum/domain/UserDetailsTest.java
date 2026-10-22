package ar.edu.um.limitadorum.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class UserDetailsTest {

	@Test
	@DisplayName("getAuthorities: debe mapear las descripciones de los roles existentes a GrantedAuthority")
	void getAuthorities_withRoles_shouldReturnMappedAuthorities() {
		// Arrange
		User user = new User();
		Role userRole = new Role("USER");
		Role adminRole = new Role("ADMIN");
		user.setRoles(Set.of(userRole, adminRole));

		// Act
		Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

		// Assert
		assertThat(authorities).hasSize(2);
		assertThat(authorities.stream().map(GrantedAuthority::getAuthority))
				.containsExactlyInAnyOrder("USER", "ADMIN");
	}

	@Test
	@DisplayName("getAuthorities: debe retornar coleccion vacia cuando no tiene roles o el conjunto es nulo")
	void getAuthorities_withNullOrEmptyRoles_shouldReturnEmptyCollection() {
		// Arrange
		User userWithEmptyRoles = new User();
		userWithEmptyRoles.setRoles(Set.of());

		User userWithNullRoles = new User();
		userWithNullRoles.setRoles(null);

		// Act & Assert
		assertThat(userWithEmptyRoles.getAuthorities()).isEmpty();
		assertThat(userWithNullRoles.getAuthorities()).isEmpty();
	}

	@Test
	@DisplayName("isEnabled: debe ser true cuando active es true, y false cuando active es false o null")
	void isEnabled_shouldReflectActiveField() {
		// Arrange
		User activeUser = new User();
		activeUser.setActive(true);

		User inactiveUser = new User();
		inactiveUser.setActive(false);

		User userWithNullActive = new User();
		userWithNullActive.setActive(null);

		// Act & Assert
		assertThat(activeUser.isEnabled()).isTrue();
		assertThat(inactiveUser.isEnabled()).isFalse();
		assertThat(userWithNullActive.isEnabled()).isFalse();
	}

	@Test
	@DisplayName("isAccountNonExpired, isAccountNonLocked, isCredentialsNonExpired: deben retornar true por defecto")
	void accountStatusMethods_shouldReturnTrueByDefault() {
		// Arrange
		User user = new User();

		// Act & Assert
		assertThat(user.isAccountNonExpired()).isTrue();
		assertThat(user.isAccountNonLocked()).isTrue();
		assertThat(user.isCredentialsNonExpired()).isTrue();
	}

	@Test
	@DisplayName("getUsername y getPassword: deben retornar los valores del usuario")
	void credentialsMethods_shouldReturnEntityValues() {
		// Arrange
		User user = new User();
		user.setUsername("santi");
		user.setPassword("$2a$10$encodedPasswordHashValueHere");

		// Act & Assert
		assertThat(user.getUsername()).isEqualTo("santi");
		assertThat(user.getPassword()).isEqualTo("$2a$10$encodedPasswordHashValueHere");
	}
}
