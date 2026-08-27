package ar.edu.um.limitadorum.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.um.limitadorum.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
