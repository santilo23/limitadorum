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

import ar.edu.um.limitadorum.dto.UserDto;
import ar.edu.um.limitadorum.mapper.UserMapper;
import ar.edu.um.limitadorum.services.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;
	private final UserMapper userMapper;

	public UserController(UserService userService, UserMapper userMapper) {
		this.userService = userService;
		this.userMapper = userMapper;
	}

	@GetMapping
	public ResponseEntity<List<UserDto>> findAll() {
		return ResponseEntity.ok(userMapper.toDtoList(userService.findAll()));
	}

	@GetMapping("/{id}")
	public ResponseEntity<UserDto> findById(@PathVariable Long id) {
		return userService.findById(id)
				.map(userMapper::toDto)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<UserDto> save(@RequestBody UserDto dto) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(userMapper.toDto(userService.save(userMapper.toEntity(dto))));
	}

	@PutMapping("/{id}")
	public ResponseEntity<UserDto> update(@PathVariable Long id, @RequestBody UserDto dto) {
		return ResponseEntity.ok(userMapper.toDto(userService.update(id, userMapper.toEntity(dto))));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		userService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
