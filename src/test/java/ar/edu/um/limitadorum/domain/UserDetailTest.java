package ar.edu.um.limitadorum.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ar.edu.um.limitadorum.dto.UserDetail;

class UserDetailTest {

	@Test
	@DisplayName("Happy Path: Debe obtener los detalles completos del usuario cuando todos los datos están presentes")
	void getDetail_withCompleteUserAndData_shouldReturnFullImmutableDetail() {
		// Arrange (Organizar)
		User user = new User();
		user.setId(1L);
		user.setUsername("santi");
		user.setEmail("santi@um.edu.ar");
		user.setPassword("SecretPass123");
		user.setActive(true);

		Role roleUser = new Role("USER");
		Role roleAdmin = new Role("ADMIN");
		user.setRoles(Set.of(roleUser, roleAdmin));

		UserData userData = new UserData();
		userData.setFirstName("Santiago");
		userData.setLastName("Gonzalez");
		userData.setPhoneNumber("+5492615551234");
		userData.setAddress("Av. San Martín 1234");
		user.setUserData(userData);

		// Act (Actuar)
		UserDetail detail = user.getDetail();

		// Assert (Verificar)
		assertThat(detail).isNotNull();
		assertThat(detail.id()).isEqualTo(1L);
		assertThat(detail.username()).isEqualTo("santi");
		assertThat(detail.email()).isEqualTo("santi@um.edu.ar");
		assertThat(detail.fullName()).isEqualTo("Santiago Gonzalez");
		assertThat(detail.phoneNumber()).isEqualTo("+5492615551234");
		assertThat(detail.address()).isEqualTo("Av. San Martín 1234");
		assertThat(detail.active()).isTrue();
		assertThat(detail.roles()).containsExactlyInAnyOrder("USER", "ADMIN");
	}

	@Test
	@DisplayName("Caso borde: Cuando userData es null, debe retornar el detalle con los datos de perfil vacíos/nulos de forma segura")
	void getDetail_whenUserDataIsNull_shouldReturnDetailWithEmptyProfileFields() {
		// Arrange
		User user = new User();
		user.setId(2L);
		user.setUsername("juanp");
		user.setEmail("juanp@um.edu.ar");
		user.setActive(false);
		user.setUserData(null);

		// Act
		UserDetail detail = user.getDetail();

		// Assert
		assertThat(detail).isNotNull();
		assertThat(detail.id()).isEqualTo(2L);
		assertThat(detail.username()).isEqualTo("juanp");
		assertThat(detail.fullName()).isNull();
		assertThat(detail.phoneNumber()).isNull();
		assertThat(detail.address()).isNull();
		assertThat(detail.active()).isFalse();
	}

	@Test
	@DisplayName("Caso borde: Cuando los roles son nulos o vacíos, debe retornar un conjunto de roles vacío")
	void getDetail_whenRolesAreEmptyOrNull_shouldReturnEmptyRolesSet() {
		// Arrange
		User user = new User();
		user.setId(3L);
		user.setUsername("sinroles");
		user.setEmail("sinroles@um.edu.ar");
		user.setRoles(null);

		// Act
		UserDetail detail = user.getDetail();

		// Assert
		assertThat(detail.roles()).isNotNull().isEmpty();
	}

	@Test
	@DisplayName("Inmutabilidad: El conjunto de roles retornado debe ser inmutable y no permitir modificaciones")
	void getDetail_rolesSetShouldBeImmutable() {
		// Arrange
		User user = new User();
		user.setId(4L);
		user.setUsername("rolemut");
		user.setEmail("rolemut@um.edu.ar");
		user.setRoles(Set.of(new Role("USER")));

		// Act
		UserDetail detail = user.getDetail();

		// Assert
		assertThatThrownBy(() -> detail.roles().add("HACKER"))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	@DisplayName("Caso de error: Cuando faltan campos requeridos como username o email, debe lanzar IllegalStateException")
	void getDetail_whenRequiredUsernameOrEmailNull_shouldThrowIllegalStateException() {
		// Arrange
		User userWithoutUsername = new User();
		userWithoutUsername.setEmail("test@um.edu.ar");

		User userWithoutEmail = new User();
		userWithoutEmail.setUsername("testuser");

		// Act & Assert
		assertThatThrownBy(userWithoutUsername::getDetail)
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("username");

		assertThatThrownBy(userWithoutEmail::getDetail)
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("email");
	}

	@Test
	@DisplayName("Seguridad y Encapsulamiento: UserDetail nunca debe exponer la contraseña")
	void getDetail_shouldNeverExposePassword() {
		// Arrange
		User user = new User();
		user.setId(5L);
		user.setUsername("secureuser");
		user.setEmail("secure@um.edu.ar");
		user.setPassword("UltraSecretHash123");

		// Act
		UserDetail detail = user.getDetail();

		// Assert
		assertThat(detail.toString()).doesNotContain("UltraSecretHash123");
	}
}
