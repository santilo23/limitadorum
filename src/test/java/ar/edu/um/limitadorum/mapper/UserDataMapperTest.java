package ar.edu.um.limitadorum.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ar.edu.um.limitadorum.domain.UserData;
import ar.edu.um.limitadorum.dto.UserDataDto;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = UserDataMapperImpl.class)
class UserDataMapperTest {

	@Autowired
	private UserDataMapper userDataMapper;

	@Test
	void toDto_shouldMapAllFields() {
		UserData userData = new UserData(1L, "John", "Doe", "Calle 123", "+5492615551234", null);

		UserDataDto dto = userDataMapper.toDto(userData);

		assertThat(dto).isEqualTo(new UserDataDto(1L, "John", "Doe", "Calle 123", "+5492615551234"));
	}

	@Test
	void toEntity_shouldMapAllFieldsAndIgnoreBackReference() {
		UserData userData = userDataMapper
				.toEntity(new UserDataDto(2L, "Jane", "Doe", "Calle 456", "+5492615555678"));

		assertThat(userData.getId()).isEqualTo(2L);
		assertThat(userData.getFirstName()).isEqualTo("Jane");
		assertThat(userData.getLastName()).isEqualTo("Doe");
		assertThat(userData.getAddress()).isEqualTo("Calle 456");
		assertThat(userData.getPhoneNumber()).isEqualTo("+5492615555678");
		assertThat(userData.getUser()).isNull();
	}

	@Test
	void shouldMapLists() {
		List<UserDataDto> dtos = userDataMapper.toDtoList(List.of(
				new UserData(1L, "John", "Doe", "Calle 123", "+5492615551234", null)));

		assertThat(dtos).containsExactly(
				new UserDataDto(1L, "John", "Doe", "Calle 123", "+5492615551234"));
	}

	@Test
	void shouldMapNullToNull() {
		assertThat(userDataMapper.toDto(null)).isNull();
		assertThat(userDataMapper.toEntity(null)).isNull();
	}
}
