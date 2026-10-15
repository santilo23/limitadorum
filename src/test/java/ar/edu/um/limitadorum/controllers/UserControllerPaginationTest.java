package ar.edu.um.limitadorum.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ar.edu.um.limitadorum.domain.User;
import ar.edu.um.limitadorum.mapper.RoleMapperImpl;
import ar.edu.um.limitadorum.mapper.UserDataMapperImpl;
import ar.edu.um.limitadorum.mapper.UserMapperImpl;
import ar.edu.um.limitadorum.services.UserService;

@WebMvcTest(UserController.class)
@Import({ UserMapperImpl.class, RoleMapperImpl.class, UserDataMapperImpl.class })
class UserControllerPaginationTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	private User user(long id) {
		User user = new User();
		user.setId(id);
		user.setUsername("user" + id);
		user.setEmail("user" + id + "@um.edu.ar");
		user.setActive(true);
		return user;
	}

	private List<User> users(long from, long to) {
		return IntStream.rangeClosed((int) from, (int) to).mapToObj(this::user).toList();
	}

	@Test
	void paged_shouldReturnRequestedPageWithTotals() throws Exception {
		Pageable pageable = PageRequest.of(2, 10, Sort.by(Sort.Direction.ASC, "id"));
		when(userService.findPaged(any(Pageable.class)))
				.thenReturn(new PageImpl<>(users(21, 25), pageable, 25L));

		mockMvc.perform(get("/api/users").param("page", "2").param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(5))
				.andExpect(jsonPath("$.content[0].username").value("user21"))
				.andExpect(jsonPath("$.page").value(2))
				.andExpect(jsonPath("$.size").value(10))
				.andExpect(jsonPath("$.totalElements").value(25))
				.andExpect(jsonPath("$.totalPages").value(3));

		ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
		verify(userService).findPaged(captor.capture());
		assertThat(captor.getValue().getPageNumber()).isEqualTo(2);
		assertThat(captor.getValue().getPageSize()).isEqualTo(10);
	}

	@Test
	void paged_withoutParams_shouldUseDefaults() throws Exception {
		Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "id"));
		when(userService.findPaged(any(Pageable.class)))
				.thenReturn(new PageImpl<>(users(1, 20), pageable, 20L));

		mockMvc.perform(get("/api/users"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.size").value(20))
				.andExpect(jsonPath("$.totalElements").value(20))
				.andExpect(jsonPath("$.totalPages").value(1));

		ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
		verify(userService).findPaged(captor.capture());
		assertThat(captor.getValue().getPageNumber()).isZero();
		assertThat(captor.getValue().getPageSize()).isEqualTo(20);
	}

	@Test
	void paged_withNegativePage_shouldReturnBadRequest() throws Exception {
		mockMvc.perform(get("/api/users").param("page", "-1").param("size", "10"))
				.andExpect(status().isBadRequest());

		verify(userService, never()).findPaged(any());
	}

	@Test
	void paged_withZeroSize_shouldReturnBadRequest() throws Exception {
		mockMvc.perform(get("/api/users").param("page", "0").param("size", "0"))
				.andExpect(status().isBadRequest());

		verify(userService, never()).findPaged(any());
	}

	@Test
	void paged_withOversizedPage_shouldReturnBadRequest() throws Exception {
		mockMvc.perform(get("/api/users").param("page", "0").param("size", "1000"))
				.andExpect(status().isBadRequest());

		verify(userService, never()).findPaged(any());
	}

	@Test
	void paged_withNonNumericPage_shouldReturnBadRequest() throws Exception {
		mockMvc.perform(get("/api/users").param("page", "abc").param("size", "10"))
				.andExpect(status().isBadRequest());

		verify(userService, never()).findPaged(any());
	}
}
