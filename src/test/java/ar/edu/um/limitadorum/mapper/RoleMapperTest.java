package ar.edu.um.limitadorum.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ar.edu.um.limitadorum.domain.Role;
import ar.edu.um.limitadorum.dto.RoleDto;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RoleMapperImpl.class)
class RoleMapperTest {

	@Autowired
	private RoleMapper roleMapper;

	@Test
	void toDto_shouldMapAllFields() {
		Role role = new Role("ADMIN");
		role.setId(1L);

		RoleDto dto = roleMapper.toDto(role);

		assertThat(dto).isEqualTo(new RoleDto(1L, "ADMIN"));
	}

	@Test
	void toEntity_shouldMapAllFields() {
		Role role = roleMapper.toEntity(new RoleDto(2L, "MANAGER"));

		assertThat(role.getId()).isEqualTo(2L);
		assertThat(role.getDescription()).isEqualTo("MANAGER");
	}

	@Test
	void shouldMapLists() {
		Role admin = new Role("ADMIN");
		admin.setId(1L);
		Role manager = new Role("MANAGER");
		manager.setId(2L);

		List<RoleDto> dtos = roleMapper.toDtoList(List.of(admin, manager));

		assertThat(dtos).containsExactly(new RoleDto(1L, "ADMIN"), new RoleDto(2L, "MANAGER"));
		assertThat(roleMapper.toEntityList(dtos)).extracting(Role::getDescription)
				.containsExactly("ADMIN", "MANAGER");
	}

	@Test
	void shouldMapNullToNull() {
		assertThat(roleMapper.toDto(null)).isNull();
		assertThat(roleMapper.toEntity(null)).isNull();
	}
}
