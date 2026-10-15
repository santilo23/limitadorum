package ar.edu.um.limitadorum.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import ar.edu.um.limitadorum.domain.User;

@DataJpaTest
class UserRepositoryPagedTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TestEntityManager entityManager;

	private void persistUsers(int count) {
		IntStream.rangeClosed(1, count).forEach(i -> {
			var user = new User();
			user.setUsername("paged" + i);
			user.setEmail("paged" + i + "@um.edu.ar");
			user.setActive(true);
			userRepository.save(user);
		});
		entityManager.clear();
	}

	@Test
	void secondPage_shouldReturnCorrespondingSliceWithTotals() {
		persistUsers(5);

		Page<User> page = userRepository.findAll(PageRequest.of(1, 2, Sort.by(Sort.Direction.ASC, "id")));

		assertThat(page.getContent()).extracting(User::getUsername)
				.containsExactly("paged3", "paged4");
		assertThat(page.getNumber()).isEqualTo(1);
		assertThat(page.getSize()).isEqualTo(2);
		assertThat(page.getTotalElements()).isEqualTo(5L);
		assertThat(page.getTotalPages()).isEqualTo(3);
	}

	@Test
	void firstPage_shouldReturnFirstSlice() {
		persistUsers(5);

		Page<User> page = userRepository.findAll(PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id")));

		assertThat(page.getContent()).hasSize(5);
		assertThat(page.getTotalElements()).isEqualTo(5L);
		assertThat(page.getTotalPages()).isEqualTo(1);
	}
}
