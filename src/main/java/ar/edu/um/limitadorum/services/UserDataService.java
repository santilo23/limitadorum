package ar.edu.um.limitadorum.services;

import java.util.List;
import java.util.Optional;

import ar.edu.um.limitadorum.domain.UserData;

public interface UserDataService {

	List<UserData> findAll();

	Optional<UserData> findById(Long id);

	UserData save(UserData userData);

	UserData update(Long id, UserData userData);

	void deleteById(Long id);
}
