package ar.edu.um.limitadorum.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.um.limitadorum.domain.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {

}
