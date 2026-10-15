package ar.edu.um.limitadorum.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import ar.edu.um.limitadorum.domain.User;
import ar.edu.um.limitadorum.repository.RoleRepository;
import ar.edu.um.limitadorum.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserPasswordEncodingTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private RoleRepository roleRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserServiceImpl userService;

	private User userWithRawPassword(String rawPassword) {
		User user = new User();
		user.setUsername("jdoe");
		user.setEmail("jdoe@example.com");
		user.setPassword(rawPassword);
		return user;
	}

	@Test
	void save_shouldStoreBcryptHashInsteadOfPlainText() {
		String hash = "$2a$10$abcdefghijklmnopqrstuuXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
		when(passwordEncoder.encode("Secreto123")).thenReturn(hash);
		when(userRepository.saveAndFlush(any(User.class))).thenAnswer(i -> i.getArgument(0));

		User result = userService.save(userWithRawPassword("Secreto123"));

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(passwordEncoder).encode("Secreto123");
		verify(userRepository).saveAndFlush(captor.capture());
		assertThat(captor.getValue().getPassword()).isEqualTo(hash);
		assertThat(result.getPassword()).isEqualTo(hash);
		assertThat(result.getPassword()).isNotEqualTo("Secreto123");
	}

	@Test
	void save_withAlreadyEncodedPassword_shouldNotEncodeAgain() {
		String alreadyHashed = new BCryptPasswordEncoder().encode("Secreto123");
		when(userRepository.saveAndFlush(any(User.class))).thenAnswer(i -> i.getArgument(0));

		User result = userService.save(userWithRawPassword(alreadyHashed));

		verify(passwordEncoder, never()).encode(any());
		assertThat(result.getPassword()).isEqualTo(alreadyHashed);
	}

	@Test
	void save_withoutPassword_shouldNotFailAndNotEncode() {
		User user = new User();
		user.setUsername("legacy");
		user.setEmail("legacy@example.com");
		user.setPassword(null);
		when(userRepository.saveAndFlush(any(User.class))).thenAnswer(i -> i.getArgument(0));

		User result = userService.save(user);

		verify(passwordEncoder, never()).encode(any());
		assertThat(result.getPassword()).isNull();
	}

	@Test
	void update_withoutPassword_shouldPreserveExistingHash() {
		String existingHash = new BCryptPasswordEncoder().encode("Vieja123");

		User existing = new User();
		existing.setId(5L);
		existing.setUsername("jdoe");
		existing.setEmail("jdoe@example.com");
		existing.setPassword(existingHash);

		User incoming = new User();
		incoming.setUsername("jdoe");
		incoming.setEmail("nuevo@example.com");
		incoming.setPassword(null);

		when(userRepository.findById(5L)).thenReturn(Optional.of(existing));
		when(userRepository.saveAndFlush(any(User.class))).thenAnswer(i -> i.getArgument(0));

		User result = userService.update(5L, incoming);

		verify(passwordEncoder, never()).encode(any());
		assertThat(result.getPassword()).isEqualTo(existingHash);
	}

	@Test
	void bcryptMatches_shouldAcceptCorrectPasswordAndRejectWrongOne() {
		PasswordEncoder realEncoder = new BCryptPasswordEncoder();

		String hash = realEncoder.encode("Secreto123");

		assertThat(hash).isNotEqualTo("Secreto123");
		assertThat(realEncoder.matches("Secreto123", hash)).isTrue();
		assertThat(realEncoder.matches("OtraClave456", hash)).isFalse();
	}
}
