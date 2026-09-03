package ar.edu.um.limitadorum.services;

import java.util.List;
import java.util.Optional;

import ar.edu.um.limitadorum.domain.Role;

public interface RoleService {

	List<Role> findAll();

	Optional<Role> findById(Long id);

	Role save(Role role);

	Role update(Long id, Role role);

	void deleteById(Long id);
}
