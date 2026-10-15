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

import ar.edu.um.limitadorum.dto.RoleDto;
import ar.edu.um.limitadorum.mapper.RoleMapper;
import ar.edu.um.limitadorum.services.RoleService;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

	private final RoleService roleService;
	private final RoleMapper roleMapper;

	public RoleController(RoleService roleService, RoleMapper roleMapper) {
		this.roleService = roleService;
		this.roleMapper = roleMapper;
	}

	@GetMapping
	public ResponseEntity<List<RoleDto>> findAll() {
		return ResponseEntity.ok(roleMapper.toDtoList(roleService.findAll()));
	}

	@GetMapping("/{id}")
	public ResponseEntity<RoleDto> findById(@PathVariable Long id) {
		return roleService.findById(id)
				.map(roleMapper::toDto)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<RoleDto> save(@RequestBody RoleDto dto) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(roleMapper.toDto(roleService.save(roleMapper.toEntity(dto))));
	}

	@PutMapping("/{id}")
	public ResponseEntity<RoleDto> update(@PathVariable Long id, @RequestBody RoleDto dto) {
		return ResponseEntity.ok(roleMapper.toDto(roleService.update(id, roleMapper.toEntity(dto))));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		roleService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
