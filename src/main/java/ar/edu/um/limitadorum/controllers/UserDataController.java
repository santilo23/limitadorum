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

import ar.edu.um.limitadorum.domain.UserData;
import ar.edu.um.limitadorum.services.UserDataService;

@RestController
@RequestMapping("/api/users-data")
public class UserDataController {

	private final UserDataService userDataService;

	public UserDataController(UserDataService userDataService) {
		this.userDataService = userDataService;
	}

	@GetMapping
	public ResponseEntity<List<UserData>> findAll() {
		return ResponseEntity.ok(userDataService.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<UserData> findById(@PathVariable Long id) {
		return userDataService.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<UserData> save(@RequestBody UserData userData) {
		return ResponseEntity.status(HttpStatus.CREATED).body(userDataService.save(userData));
	}

	@PutMapping("/{id}")
	public ResponseEntity<UserData> update(@PathVariable Long id, @RequestBody UserData userData) {
		return ResponseEntity.ok(userDataService.update(id, userData));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		userDataService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
