package ar.edu.um.limitadorum.mapper;

import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ar.edu.um.limitadorum.domain.Role;
import ar.edu.um.limitadorum.dto.RoleDto;

@Mapper(componentModel = "spring")
public interface RoleMapper {

	RoleDto toDto(Role role);

	@Mapping(target = "users", ignore = true)
	Role toEntity(RoleDto dto);

	List<RoleDto> toDtoList(List<Role> roles);

	List<Role> toEntityList(List<RoleDto> dtos);

	Set<RoleDto> toDtoSet(Set<Role> roles);

	Set<Role> toEntitySet(Set<RoleDto> dtos);
}
