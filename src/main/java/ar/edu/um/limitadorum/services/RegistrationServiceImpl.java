package ar.edu.um.limitadorum.services;

import java.util.HashSet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.um.limitadorum.domain.Role;
import ar.edu.um.limitadorum.domain.User;
import ar.edu.um.limitadorum.domain.UserData;
import ar.edu.um.limitadorum.dto.RegisterRequest;
import ar.edu.um.limitadorum.exception.DuplicateUserException;
import ar.edu.um.limitadorum.mapper.AuthMapper;
import ar.edu.um.limitadorum.repository.RoleRepository;
import ar.edu.um.limitadorum.repository.UserRepository;

@Service
public class RegistrationServiceImpl implements RegistrationService {

	private static final String DEFAULT_ROLE = "USER";

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final UserService userService;
	private final AuthMapper authMapper;

	public RegistrationServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
			UserService userService, AuthMapper authMapper) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.userService = userService;
		this.authMapper = authMapper;
	}

	@Override
	@Transactional
	public User register(RegisterRequest request) {
		if (userRepository.existsByUsername(request.username())) {
			throw new DuplicateUserException("username",
					"username '" + request.username() + "' is already taken");
		}
		if (userRepository.existsByEmail(request.email())) {
			throw new DuplicateUserException("email",
					"email '" + request.email() + "' is already registered");
		}

		UserData userData = authMapper.toUserData(request);
		User user = authMapper.toUser(request);
		user.setUserData(userData);
		user.setActive(true);
		if (user.getRoles() == null) {
			user.setRoles(new HashSet<>());
		}
		user.getRoles().add(findOrCreateDefaultRole());

		return userService.save(user);
	}

	private Role findOrCreateDefaultRole() {
		return roleRepository.findByDescription(DEFAULT_ROLE)
				.orElseGet(() -> roleRepository.save(new Role(DEFAULT_ROLE)));
	}
}
