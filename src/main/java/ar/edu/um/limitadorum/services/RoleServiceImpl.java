package ar.edu.um.limitadorum.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.um.limitadorum.domain.Role;
import ar.edu.um.limitadorum.exception.ResourceNotFoundException;
import ar.edu.um.limitadorum.repository.RoleRepository;

@Service
@Transactional
public class RoleServiceImpl implements RoleService {

	private final RoleRepository roleRepository;

	public RoleServiceImpl(RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Role> findAll() {
		return roleRepository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Role> findById(Long id) {
		return roleRepository.findById(id);
	}

	@Override
	public Role save(Role role) {
		return roleRepository.saveAndFlush(role);
	}

	@Override
	public Role update(Long id, Role role) {
		Role existing = roleRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Role", id));
		existing.setDescription(role.getDescription());
		return roleRepository.saveAndFlush(existing);
	}

	@Override
	public void deleteById(Long id) {
		if (!roleRepository.existsById(id)) {
			throw new ResourceNotFoundException("Role", id);
		}
		roleRepository.deleteById(id);
	}
}
