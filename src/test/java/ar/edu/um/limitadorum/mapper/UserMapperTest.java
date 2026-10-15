package ar.edu.um.limitadorum.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ar.edu.um.limitadorum.domain.User;
import ar.edu.um.limitadorum.domain.UserData;
import ar.edu.um.limitadorum.dto.UserDataDto;
import ar.edu.um.limitadorum.dto.UserDto;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { UserMapperImpl.class, RoleMapperImpl.class, UserDataMapperImpl.class })
class UserMapperTest {

	@Autowired
	private UserMapper userMapper;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private User user() {
		User user = new User();
		user.setId(1L);
		user.setUsername("jdoe");
		user.setEmail("jdoe@example.com");
		user.setPassword("Secreto123");
		user.setActive(true);
		user.setUserData(new UserData(10L, "John", "Doe", "Calle 123", "+5492615551234", null));
		return user;
	}

	@Test
	void toDto_shouldMapAllFieldsIncludingNestedUserData() {
		UserDto dto = userMapper.toDto(user());

		assertThat(dto).isEqualTo(new UserDto(1L, "jdoe", "jdoe@example.com", "Secreto123", true,
				java.util.Set.of(),
				new UserDataDto(10L, "John", "Doe", "Calle 123", "+5492615551234", 1L)));
	}

	@Test
	void toEntity_shouldMapAllFieldsIncludingNestedUserData() {
		User user = userMapper.toEntity(new UserDto(1L, "jdoe", "jdoe@example.com", "Secreto123",
				true, new UserDataDto(10L, "John", "Doe", "Calle 123", "+5492615551234")));

		assertThat(user.getId()).isEqualTo(1L);
		assertThat(user.getUsername()).isEqualTo("jdoe");
		assertThat(user.getEmail()).isEqualTo("jdoe@example.com");
		assertThat(user.getPassword()).isEqualTo("Secreto123");
		assertThat(user.getActive()).isTrue();
		assertThat(user.getUserData().getFirstName()).isEqualTo("John");
	}

	@Test
	void toDto_withNullUserData_shouldStayNull() {
		User user = user();
		user.setUserData(null);

		assertThat(userMapper.toDto(user).userData()).isNull();
		assertThat(userMapper.toEntity(
				new UserDto(1L, "jdoe", "jdoe@example.com", null, true, null)).getUserData())
				.isNull();
	}

	@Test
	void shouldMapLists() {
		List<UserDto> dtos = userMapper.toDtoList(List.of(user()));

		assertThat(dtos).hasSize(1);
		assertThat(dtos.get(0).username()).isEqualTo("jdoe");
	}

	@Test
	void shouldMapNullToNull() {
		assertThat(userMapper.toDto(null)).isNull();
		assertThat(userMapper.toEntity(null)).isNull();
	}

	@Test
	void passwordShouldNotBeExposedInDtoJson() throws Exception {
		String json = objectMapper.writeValueAsString(userMapper.toDto(user()));

		assertThat(json).doesNotContain("password");
		assertThat(json).doesNotContain("Secreto123");
		assertThat(objectMapper.readTree(json).has("password")).isFalse();
	}
}
