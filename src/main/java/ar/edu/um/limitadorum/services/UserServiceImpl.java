package ar.edu.um.limitadorum.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.um.limitadorum.domain.User;
import ar.edu.um.limitadorum.exception.ResourceNotFoundException;
import ar.edu.um.limitadorum.repository.UserRepository;

@Service
@Transactional
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;

	public UserServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<User> findAll() {
		return userRepository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<User> findById(Long id) {
		return userRepository.findById(id);
	}

	@Override
	public User save(User user) {
		return userRepository.saveAndFlush(user);
	}

	/**
	 * Actualiza los campos recibidos sobre el usuario existente. Los datos
	 * personales y los roles solo se reemplazan si vienen en la peticion: de lo
	 * contrario un PUT sin esos campos borraria el UserData asociado, porque la
	 * relacion tiene orphanRemoval.
	 */
	@Override
	public User update(Long id, User user) {
		User existing = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User", id));
		existing.setUsername(user.getUsername());
		existing.setEmail(user.getEmail());
		existing.setActive(user.getActive());
		if (user.getRoles() != null && !user.getRoles().isEmpty()) {
			existing.setRoles(user.getRoles());
		}
		if (user.getUserData() != null) {
			existing.setUserData(user.getUserData());
		}
		return userRepository.saveAndFlush(existing);
	}

	@Override
	public void deleteById(Long id) {
		if (!userRepository.existsById(id)) {
			throw new ResourceNotFoundException("User", id);
		}
		userRepository.deleteById(id);
	}
}
