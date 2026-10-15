package ar.edu.um.limitadorum.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import ar.edu.um.limitadorum.dto.RegisterRequest;
import ar.edu.um.limitadorum.dto.UserDto;
import ar.edu.um.limitadorum.mapper.UserMapper;
import ar.edu.um.limitadorum.services.RegistrationService;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final RegistrationService registrationService;
	private final UserMapper userMapper;

	public AuthController(RegistrationService registrationService, UserMapper userMapper) {
		this.registrationService = registrationService;
		this.userMapper = userMapper;
	}

	@PostMapping("/register")
	public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(userMapper.toDto(registrationService.register(request)));
	}
}
