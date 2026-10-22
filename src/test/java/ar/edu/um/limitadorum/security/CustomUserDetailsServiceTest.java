package ar.edu.um.limitadorum.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import ar.edu.um.limitadorum.domain.Role;
import ar.edu.um.limitadorum.domain.User;
import ar.edu.um.limitadorum.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private CustomUserDetailsService userDetailsService;

	@Test
	@DisplayName("loadUserByUsername: debe retornar el UserDetails cuando el usuario existe")
	void loadUserByUsername_whenUserExists_shouldReturnUserDetails() {
		// Arrange
		User user = new User();
		user.setUsername("santi");
		user.setPassword("$2a$10$encodedPasswordHash");
		user.setActive(true);
		user.setRoles(Set.of(new Role("USER")));

		when(userRepository.findByUsername("santi")).thenReturn(Optional.of(user));

		// Act
		UserDetails result = userDetailsService.loadUserByUsername("santi");

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getUsername()).isEqualTo("santi");
		assertThat(result.getPassword()).isEqualTo("$2a$10$encodedPasswordHash");
		assertThat(result.isEnabled()).isTrue();
		assertThat(result.getAuthorities()).hasSize(1);
	}

	@Test
	@DisplayName("loadUserByUsername: debe lanzar UsernameNotFoundException si el usuario no existe")
	void loadUserByUsername_whenUserDoesNotExist_shouldThrowException() {
		// Arrange
		when(userRepository.findByUsername("fantasma")).thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> userDetailsService.loadUserByUsername("fantasma"))
				.isInstanceOf(UsernameNotFoundException.class)
				.hasMessageContaining("fantasma");
	}

	@Test
	@DisplayName("loadUserByUsername: debe lanzar UsernameNotFoundException si el username es nulo o vacío")
	void loadUserByUsername_withNullOrBlank_shouldThrowException() {
		assertThatThrownBy(() -> userDetailsService.loadUserByUsername(null))
				.isInstanceOf(UsernameNotFoundException.class);
		assertThatThrownBy(() -> userDetailsService.loadUserByUsername("   "))
				.isInstanceOf(UsernameNotFoundException.class);
	}
}
