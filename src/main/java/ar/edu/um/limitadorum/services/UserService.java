package ar.edu.um.limitadorum.services;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import ar.edu.um.limitadorum.domain.User;

public interface UserService {

	List<User> findAll();

	Page<User> findPaged(Pageable pageable);

	Optional<User> findById(Long id);

	User save(User user);

	User update(Long id, User user);

	void deleteById(Long id);
}
