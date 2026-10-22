package ar.edu.um.limitadorum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ar.edu.um.limitadorum.domain.User;
import ar.edu.um.limitadorum.domain.UserData;
import ar.edu.um.limitadorum.dto.RegisterRequest;

@Mapper(componentModel = "spring")
public interface AuthMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "roles", ignore = true)
	@Mapping(target = "userData", ignore = true)
	@Mapping(target = "active", ignore = true)
	@Mapping(target = "authorities", ignore = true)
	User toUser(RegisterRequest request);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "user", ignore = true)
	UserData toUserData(RegisterRequest request);
}
