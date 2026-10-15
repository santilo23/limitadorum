package ar.edu.um.limitadorum.services;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import java.util.regex.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.um.limitadorum.domain.Role;
import ar.edu.um.limitadorum.domain.User;
import ar.edu.um.limitadorum.exception.ResourceNotFoundException;
import ar.edu.um.limitadorum.repository.RoleRepository;
import ar.edu.um.limitadorum.repository.UserRepository;

@Service
@Transactional
public class UserServiceImpl implements UserService {

	private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.{53}$");

	private final UserRepository userRepository;

	private final RoleRepository roleRepository;

	private final PasswordEncoder passwordEncoder;

	public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
			PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional(readOnly = true)
	public List<User> findAll() {
		return userRepository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public Page<User> findPaged(Pageable pageable) {
		return userRepository.findAll(pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<User> findById(Long id) {
		return userRepository.findById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public ar.edu.um.limitadorum.dto.UserDetail getUserDetail(Long id) {
		validateUserId(id);
		return userRepository.findById(id)
				.map(User::getDetail)
				.orElseThrow(() -> new ResourceNotFoundException("User", id));
	}

	private void validateUserId(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("El id no puede ser nulo");
		}
		if (id <= 0) {
			throw new IllegalArgumentException("El id debe ser positivo y mayor a 0");
		}
	}

	@Override
	public User save(User user) {
		user.setPassword(encodeIfNecessary(user.getPassword()));
		user.setRoles(resolveRoles(user.getRoles()));
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
		if (user.getPassword() != null) {
			existing.setPassword(encodeIfNecessary(user.getPassword()));
		}
		if (user.getRoles() != null && !user.getRoles().isEmpty()) {
			existing.setRoles(resolveRoles(user.getRoles()));
		}
		if (user.getUserData() != null) {
			existing.setUserData(user.getUserData());
		}
		return userRepository.saveAndFlush(existing);
	}

	private String encodeIfNecessary(String password) {
		if (password == null || isAlreadyEncoded(password)) {
			return password;
		}
		return passwordEncoder.encode(password);
	}

	private static boolean isAlreadyEncoded(String password) {
		return BCRYPT_PATTERN.matcher(password).matches();
	}

	@Override
	public void deleteById(Long id) {
		if (!userRepository.existsById(id)) {
			throw new ResourceNotFoundException("User", id);
		}
		userRepository.deleteById(id);
	}

	/**
	 * Las peticiones traen los roles como referencias ({@code {"id": 1}}), sin
	 * el resto de los campos. Hay que reemplazarlas por las entidades reales de
	 * la base: si no, se guardan roles a medio construir y la respuesta sale con
	 * los campos en null.
	 */
	private Set<Role> resolveRoles(Set<Role> roles) {
		Set<Role> resolved = new HashSet<>();
		if (roles == null) {
			return resolved;
		}
		for (Role role : roles) {
			if (role.getId() == null) {
				throw new IllegalArgumentException("Cada rol debe indicar su id");
			}
			resolved.add(roleRepository.findById(role.getId())
					.orElseThrow(() -> new ResourceNotFoundException("Role", role.getId())));
		}
		return resolved;
	}
}
