package ar.edu.um.limitadorum.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ar.edu.um.limitadorum.domain.User;
import ar.edu.um.limitadorum.dto.UserDto;

@Mapper(componentModel = "spring", uses = {RoleMapper.class, UserDataMapper.class})
public interface UserMapper {

	UserDto toDto(User user);

	@Mapping(target = "authorities", ignore = true)
	User toEntity(UserDto dto);

	List<UserDto> toDtoList(List<User> users);

	List<User> toEntityList(List<UserDto> dtos);
}
