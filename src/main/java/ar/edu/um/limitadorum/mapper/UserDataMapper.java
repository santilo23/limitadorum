package ar.edu.um.limitadorum.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ar.edu.um.limitadorum.domain.User;
import ar.edu.um.limitadorum.domain.UserData;
import ar.edu.um.limitadorum.dto.UserDataDto;

@Mapper(componentModel = "spring")
public interface UserDataMapper {

	@Mapping(target = "userId", source = "user.id")
	@Mapping(target = "user", ignore = true)
	UserDataDto toDto(UserData userData);

	@Mapping(target = "user", expression = "java(mapUser(dto))")
	UserData toEntity(UserDataDto dto);

	default User mapUser(UserDataDto dto) {
		if (dto == null || dto.resolvedUserId() == null) {
			return null;
		}
		User u = new User();
		u.setId(dto.resolvedUserId());
		return u;
	}

	List<UserDataDto> toDtoList(List<UserData> entities);

	List<UserData> toEntityList(List<UserDataDto> dtos);
}
