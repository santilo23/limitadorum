package ar.edu.um.limitadorum.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import ar.edu.um.limitadorum.domain.User;
import ar.edu.um.limitadorum.repository.RoleRepository;
import ar.edu.um.limitadorum.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServicePagedTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private RoleRepository roleRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserServiceImpl userService;

	private User user(Long id, String username) {
		User user = new User();
		user.setId(id);
		user.setUsername(username);
		user.setEmail(username + "@um.edu.ar");
		user.setActive(true);
		return user;
	}

	@Test
	void findPaged_shouldDelegatePageableAndPreserveTotals() {
		Pageable pageable = PageRequest.of(2, 10, Sort.by(Sort.Direction.ASC, "id"));
		Page<User> page = new PageImpl<>(List.of(user(21L, "user21"), user(22L, "user22"),
				user(23L, "user23"), user(24L, "user24"), user(25L, "user25")), pageable, 25L);
		when(userRepository.findAll(pageable)).thenReturn(page);

		Page<User> result = userService.findPaged(pageable);

		verify(userRepository).findAll(pageable);
		assertThat(result.getContent()).extracting(User::getUsername)
				.containsExactly("user21", "user22", "user23", "user24", "user25");
		assertThat(result.getNumber()).isEqualTo(2);
		assertThat(result.getSize()).isEqualTo(10);
		assertThat(result.getTotalElements()).isEqualTo(25L);
		assertThat(result.getTotalPages()).isEqualTo(3);
	}

	@Test
	void findPaged_emptyResult_shouldReturnEmptyPage() {
		Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id"));
		when(userRepository.findAll(pageable)).thenReturn(Page.empty(pageable));

		Page<User> result = userService.findPaged(pageable);

		assertThat(result.getContent()).isEmpty();
		assertThat(result.getTotalElements()).isZero();
		assertThat(result.getTotalPages()).isZero();
	}
}
