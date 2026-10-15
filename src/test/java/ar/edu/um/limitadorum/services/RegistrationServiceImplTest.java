package ar.edu.um.limitadorum.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.um.limitadorum.domain.Role;
import ar.edu.um.limitadorum.domain.User;
import ar.edu.um.limitadorum.domain.UserData;
import ar.edu.um.limitadorum.dto.RegisterRequest;
import ar.edu.um.limitadorum.exception.DuplicateUserException;
import ar.edu.um.limitadorum.mapper.AuthMapper;
import ar.edu.um.limitadorum.repository.RoleRepository;
import ar.edu.um.limitadorum.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private RoleRepository roleRepository;

	@Mock
	private UserService userService;

	@Mock
	private AuthMapper authMapper;

	@InjectMocks
	private RegistrationServiceImpl registrationService;

	private RegisterRequest request() {
		return new RegisterRequest("jperez", "jperez@um.edu.ar", "Secreto123",
				"Juan", "Perez", "+5492615551234", "Calle Falsa 123");
	}

	private UserData userDataEntity() {
		UserData userData = new UserData();
		userData.setFirstName("Juan");
		userData.setLastName("Perez");
		userData.setPhoneNumber("+5492615551234");
		userData.setAddress("Calle Falsa 123");
		return userData;
	}

	private User userEntity() {
		User user = new User();
		user.setUsername("jperez");
		user.setEmail("jperez@um.edu.ar");
		user.setPassword("Secreto123");
		return user;
	}

	@Test
	void register_withValidData_shouldPersistUserWithUserDataAndRole() {
		UserData userData = userDataEntity();
		User user = userEntity();
		Role userRole = new Role("USER");

		when(userRepository.existsByUsername("jperez")).thenReturn(false);
		when(userRepository.existsByEmail("jperez@um.edu.ar")).thenReturn(false);
		when(authMapper.toUserData(any(RegisterRequest.class))).thenReturn(userData);
		when(authMapper.toUser(any(RegisterRequest.class))).thenReturn(user);
		when(roleRepository.findByDescription("USER")).thenReturn(Optional.of(userRole));
		when(userService.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

		User result = registrationService.register(request());

		verify(userService).save(any(User.class));
		assertThat(result.getUserData()).isSameAs(userData);
		assertThat(result.getActive()).isTrue();
		assertThat(result.getRoles()).containsExactly(userRole);
	}

	@Test
	void register_whenUsernameAlreadyExists_shouldThrowDuplicateUserException() {
		when(userRepository.existsByUsername("jperez")).thenReturn(true);

		assertThatThrownBy(() -> registrationService.register(request()))
				.isInstanceOf(DuplicateUserException.class)
				.hasMessageContaining("username 'jperez' is already taken")
				.extracting(ex -> ((DuplicateUserException) ex).getField())
				.isEqualTo("username");

		verify(userRepository, never()).existsByEmail(any());
		verify(userService, never()).save(any());
	}

	@Test
	void register_whenEmailAlreadyExists_shouldThrowDuplicateUserException() {
		when(userRepository.existsByUsername("jperez")).thenReturn(false);
		when(userRepository.existsByEmail("jperez@um.edu.ar")).thenReturn(true);

		assertThatThrownBy(() -> registrationService.register(request()))
				.isInstanceOf(DuplicateUserException.class)
				.hasMessageContaining("email 'jperez@um.edu.ar' is already registered")
				.extracting(ex -> ((DuplicateUserException) ex).getField())
				.isEqualTo("email");

		verify(userService, never()).save(any());
	}

	@Test
	void register_whenRoleMissing_shouldCreateRoleBeforeSaving() {
		UserData userData = userDataEntity();
		User user = userEntity();
		Role createdRole = new Role("USER");
		createdRole.setId(5L);

		when(userRepository.existsByUsername("jperez")).thenReturn(false);
		when(userRepository.existsByEmail("jperez@um.edu.ar")).thenReturn(false);
		when(authMapper.toUserData(any(RegisterRequest.class))).thenReturn(userData);
		when(authMapper.toUser(any(RegisterRequest.class))).thenReturn(user);
		when(roleRepository.findByDescription("USER")).thenReturn(Optional.empty());
		when(roleRepository.save(any(Role.class))).thenReturn(createdRole);
		when(userService.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

		User result = registrationService.register(request());

		verify(roleRepository).save(any(Role.class));
		assertThat(result.getRoles()).containsExactly(createdRole);
	}
}
