package ar.edu.um.limitadorum.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.um.limitadorum.dto.UserDataDto;
import ar.edu.um.limitadorum.mapper.UserDataMapper;
import ar.edu.um.limitadorum.services.UserDataService;

@RestController
@RequestMapping("/api/users-data")
public class UserDataController {

	private final UserDataService userDataService;
	private final UserDataMapper userDataMapper;

	public UserDataController(UserDataService userDataService, UserDataMapper userDataMapper) {
		this.userDataService = userDataService;
		this.userDataMapper = userDataMapper;
	}

	@GetMapping
	public ResponseEntity<List<UserDataDto>> findAll() {
		return ResponseEntity.ok(userDataMapper.toDtoList(userDataService.findAll()));
	}

	@GetMapping("/{id}")
	public ResponseEntity<UserDataDto> findById(@PathVariable Long id) {
		return userDataService.findById(id)
				.map(userDataMapper::toDto)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<UserDataDto> save(@RequestBody UserDataDto dto) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(userDataMapper.toDto(userDataService.save(userDataMapper.toEntity(dto))));
	}

	@PutMapping("/{id}")
	public ResponseEntity<UserDataDto> update(@PathVariable Long id, @RequestBody UserDataDto dto) {
		return ResponseEntity.ok(
				userDataMapper.toDto(userDataService.update(id, userDataMapper.toEntity(dto))));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		userDataService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
