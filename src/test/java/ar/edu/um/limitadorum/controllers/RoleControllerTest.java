package ar.edu.um.limitadorum.controllers;

import static org.mockito.ArgumentMatchers.any;
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

import ar.edu.um.limitadorum.domain.Role;
import ar.edu.um.limitadorum.mapper.RoleMapperImpl;
import ar.edu.um.limitadorum.services.RoleService;

@WebMvcTest(RoleController.class)
@Import(RoleMapperImpl.class)
class RoleControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private RoleService roleService;

	private Role role(Long id, String description) {
		Role role = new Role(description);
		role.setId(id);
		return role;
	}

	@Test
	void findAll_shouldReturnAllRoles() throws Exception {
		when(roleService.findAll()).thenReturn(List.of(role(1L, "ADMIN"), role(2L, "MANAGER")));

		mockMvc.perform(get("/api/roles"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].description").value("ADMIN"))
				.andExpect(jsonPath("$[1].description").value("MANAGER"));
	}

	@Test
	void findById_whenRoleExists_shouldReturnIt() throws Exception {
		when(roleService.findById(1L)).thenReturn(Optional.of(role(1L, "ADMIN")));

		mockMvc.perform(get("/api/roles/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.description").value("ADMIN"));
	}

	@Test
	void findById_whenRoleDoesNotExist_shouldReturnNotFound() throws Exception {
		when(roleService.findById(99L)).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/roles/99"))
				.andExpect(status().isNotFound());
	}

	@Test
	void save_shouldReturnCreatedRole() throws Exception {
		when(roleService.save(any(Role.class))).thenReturn(role(1L, "ADMIN"));

		mockMvc.perform(post("/api/roles")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"description\":\"ADMIN\"}"))
				.andExpect(status().isCreated())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.description").value("ADMIN"));
	}

	@Test
	void update_shouldReturnUpdatedRole() throws Exception {
		when(roleService.update(any(Long.class), any(Role.class))).thenReturn(role(1L, "SUPERADMIN"));

		mockMvc.perform(put("/api/roles/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"description\":\"SUPERADMIN\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.description").value("SUPERADMIN"));
	}

	@Test
	void deleteById_shouldReturnNoContent() throws Exception {
		mockMvc.perform(delete("/api/roles/1"))
				.andExpect(status().isNoContent());

		verify(roleService).deleteById(1L);
	}
}
