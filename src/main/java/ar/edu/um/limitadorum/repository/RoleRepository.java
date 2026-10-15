package ar.edu.um.limitadorum.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.um.limitadorum.domain.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

	Optional<Role> findByDescription(String description);
}
