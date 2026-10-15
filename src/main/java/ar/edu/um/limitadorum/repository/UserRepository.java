package ar.edu.um.limitadorum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.um.limitadorum.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);

	@EntityGraph(attributePaths = "userData")
	@Override
	Page<User> findAll(Pageable pageable);
}
