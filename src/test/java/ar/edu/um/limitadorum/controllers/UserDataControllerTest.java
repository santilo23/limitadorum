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

import ar.edu.um.limitadorum.domain.UserData;
import ar.edu.um.limitadorum.mapper.UserDataMapperImpl;
import ar.edu.um.limitadorum.services.UserDataService;

@WebMvcTest(UserDataController.class)
@Import(UserDataMapperImpl.class)
class UserDataControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserDataService userDataService;

	private UserData userData(Long id, String firstName, String phoneNumber) {
		UserData ud = new UserData();
		ud.setId(id);
		ud.setFirstName(firstName);
		ud.setPhoneNumber(phoneNumber);
		return ud;
	}

	@Test
	void findAll_shouldReturnAllUserData() throws Exception {
		when(userDataService.findAll())
				.thenReturn(List.of(userData(1L, "Alice", "555-1234"), userData(2L, "Bob", "555-5678")));

		mockMvc.perform(get("/api/users-data"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].firstName").value("Alice"))
				.andExpect(jsonPath("$[1].phoneNumber").value("555-5678"));
	}

	@Test
	void findById_whenUserDataExists_shouldReturnIt() throws Exception {
		when(userDataService.findById(1L)).thenReturn(Optional.of(userData(1L, "Alice", "555-1234")));

		mockMvc.perform(get("/api/users-data/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.firstName").value("Alice"));
	}

	@Test
	void findById_whenUserDataDoesNotExist_shouldReturnNotFound() throws Exception {
		when(userDataService.findById(99L)).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/users-data/99"))
				.andExpect(status().isNotFound());
	}

	@Test
	void save_shouldReturnCreatedUserData() throws Exception {
		when(userDataService.save(any(UserData.class))).thenReturn(userData(1L, "Alice", "555-1234"));

		mockMvc.perform(post("/api/users-data")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"firstName\":\"Alice\",\"phoneNumber\":\"555-1234\"}"))
				.andExpect(status().isCreated())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.firstName").value("Alice"));
	}

	@Test
	void update_shouldReturnUpdatedUserData() throws Exception {
		when(userDataService.update(any(Long.class), any(UserData.class)))
				.thenReturn(userData(1L, "Alicia", "555-9999"));

		mockMvc.perform(put("/api/users-data/1")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"firstName\":\"Alicia\",\"phoneNumber\":\"555-9999\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.firstName").value("Alicia"));
	}

	@Test
	void deleteById_shouldReturnNoContent() throws Exception {
		mockMvc.perform(delete("/api/users-data/1"))
				.andExpect(status().isNoContent());

		verify(userDataService).deleteById(1L);
	}
}
