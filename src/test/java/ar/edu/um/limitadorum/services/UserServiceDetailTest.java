package ar.edu.um.limitadorum.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import ar.edu.um.limitadorum.domain.Role;
import ar.edu.um.limitadorum.domain.User;
import ar.edu.um.limitadorum.domain.UserData;
import ar.edu.um.limitadorum.dto.UserDetail;
import ar.edu.um.limitadorum.exception.ResourceNotFoundException;
import ar.edu.um.limitadorum.repository.RoleRepository;
import ar.edu.um.limitadorum.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceDetailTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private RoleRepository roleRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserServiceImpl userService;

	@Test
	@DisplayName("Happy Path: Debe retornar UserDetail correctamente cuando el usuario existe")
	void getUserDetail_whenUserExists_shouldReturnUserDetail() {
		// Arrange (Organizar)
		Long userId = 1L;
		User user = new User();
		user.setId(userId);
		user.setUsername("marian");
		user.setEmail("marian@um.edu.ar");
		user.setActive(true);
		user.setRoles(Set.of(new Role("USER")));

		UserData userData = new UserData();
		userData.setFirstName("Mariana");
		userData.setLastName("Lopez");
		userData.setPhoneNumber("+5492614445555");
		userData.setAddress("Calle Colón 500");
		user.setUserData(userData);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		// Act (Actuar)
		UserDetail detail = userService.getUserDetail(userId);

		// Assert (Verificar)
		verify(userRepository).findById(userId);
		assertThat(detail).isNotNull();
		assertThat(detail.id()).isEqualTo(userId);
		assertThat(detail.username()).isEqualTo("marian");
		assertThat(detail.email()).isEqualTo("marian@um.edu.ar");
		assertThat(detail.fullName()).isEqualTo("Mariana Lopez");
		assertThat(detail.roles()).containsExactly("USER");
	}

	@Test
	@DisplayName("Caso de error: Debe lanzar ResourceNotFoundException cuando el ID no existe en la base de datos")
	void getUserDetail_whenUserNotFound_shouldThrowResourceNotFoundException() {
		// Arrange
		Long nonExistentId = 999L;
		when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> userService.getUserDetail(nonExistentId))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("999");

		verify(userRepository).findById(nonExistentId);
	}

	@Test
	@DisplayName("Caso borde / validación: Debe lanzar IllegalArgumentException si el ID es nulo")
	void getUserDetail_whenIdIsNull_shouldThrowIllegalArgumentException() {
		// Arrange
		Long nullId = null;

		// Act & Assert
		assertThatThrownBy(() -> userService.getUserDetail(nullId))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("El id no puede ser nulo");

		verify(userRepository, never()).findById(any());
	}

	@Test
	@DisplayName("Caso borde / validación: Debe lanzar IllegalArgumentException si el ID es menor o igual a 0")
	void getUserDetail_whenIdIsZeroOrNegative_shouldThrowIllegalArgumentException() {
		// Arrange
		Long zeroId = 0L;
		Long negativeId = -5L;

		// Act & Assert
		assertThatThrownBy(() -> userService.getUserDetail(zeroId))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("El id debe ser positivo");

		assertThatThrownBy(() -> userService.getUserDetail(negativeId))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("El id debe ser positivo");

		verify(userRepository, never()).findById(any());
	}
}
