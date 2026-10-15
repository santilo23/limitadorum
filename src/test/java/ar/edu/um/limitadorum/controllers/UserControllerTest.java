package ar.edu.um.limitadorum.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ar.edu.um.limitadorum.domain.User;
import ar.edu.um.limitadorum.mapper.RoleMapperImpl;
import ar.edu.um.limitadorum.mapper.UserDataMapperImpl;
import ar.edu.um.limitadorum.mapper.UserMapperImpl;
import ar.edu.um.limitadorum.services.UserService;

@WebMvcTest(UserController.class)
@Import({ UserMapperImpl.class, RoleMapperImpl.class, UserDataMapperImpl.class })
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	private User user(Long id, String username, String email) {
		User user = new User();
		user.setId(id);
		user.setUsername(username);
		user.setEmail(email);
		user.setActive(true);
		return user;
	}

	@Test
	void findAll_shouldReturnAllUsers() throws Exception {
		when(userService.findAll())
				.thenReturn(List.of(user(1L, "jperez", "jperez@um.edu.ar"), user(2L, "asmith", "asmith@um.edu.ar")));

		mockMvc.perform(get("/api/users"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].username").value("jperez"))
				.andExpect(jsonPath("$[1].email").value("asmith@um.edu.ar"));
	}

	@Test
	void findById_whenUserExists_shouldReturnIt() throws Exception {
		when(userService.findById(1L)).thenReturn(Optional.of(user(1L, "jperez", "jperez@um.edu.ar")));

		mockMvc.perform(get("/api/users/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.username").value("jperez"));
	}

	@Test
	void findById_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
		when(userService.findById(99L)).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/users/99"))
				.andExpect(status().isNotFound());
	}

	@Test
	void save_shouldReturnCreatedUser() throws Exception {
		when(userService.save(any(User.class))).thenReturn(user(1L, "nuevo", "nuevo@um.edu.ar"));

		mockMvc.perform(post("/api/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"nuevo\",\"email\":\"nuevo@um.edu.ar\",\"active\":true}"))
				.andExpect(status().isCreated())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.username").value("nuevo"));
	}

	@Test
	void update_shouldReturnUpdatedUser() throws Exception {
		when(userService.update(any(Long.class), any(User.class)))
				.thenReturn(user(1L, "actualizado", "actualizado@um.edu.ar"));

		mockMvc.perform(put("/api/users/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"actualizado\",\"email\":\"actualizado@um.edu.ar\",\"active\":true}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("actualizado"));
	}

	@Test
	void deleteById_shouldReturnNoContent() throws Exception {
		mockMvc.perform(delete("/api/users/1"))
				.andExpect(status().isNoContent());

		verify(userService).deleteById(1L);
	}

	@Test
	void save_shouldCreateOnlyWhenPersisted() throws Exception {
		when(userService.save(any(User.class))).thenReturn(user(1L, "nuevo", "nuevo@um.edu.ar"));

		mockMvc.perform(post("/api/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"nuevo\",\"email\":\"nuevo@um.edu.ar\",\"active\":true}"))
				.andExpect(status().isCreated());

		verify(userService).save(any(User.class));
		verify(userService, never()).update(any(Long.class), any(User.class));
	}
}
