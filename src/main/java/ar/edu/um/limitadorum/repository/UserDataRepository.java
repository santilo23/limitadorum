package ar.edu.um.limitadorum.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.um.limitadorum.domain.UserData;

public interface UserDataRepository extends JpaRepository<UserData, Long> {

}
