package ar.edu.um.limitadorum.controllers;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ar.edu.um.limitadorum.dto.PagedResponse;
import ar.edu.um.limitadorum.dto.UserDto;
import ar.edu.um.limitadorum.mapper.UserMapper;
import ar.edu.um.limitadorum.services.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private static final int MAX_PAGE_SIZE = 100;

	private final UserService userService;
	private final UserMapper userMapper;

	public UserController(UserService userService, UserMapper userMapper) {
		this.userService = userService;
		this.userMapper = userMapper;
	}

	@GetMapping
	public ResponseEntity<PagedResponse<UserDto>> findPaged(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		if (page < 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be >= 0");
		}
		if (size < 1 || size > MAX_PAGE_SIZE) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"size must be between 1 and " + MAX_PAGE_SIZE);
		}
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
		return ResponseEntity.ok(PagedResponse
				.of(userService.findPaged(pageable).map(userMapper::toDto)));
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
