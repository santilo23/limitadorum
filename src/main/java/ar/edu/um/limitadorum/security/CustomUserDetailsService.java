package ar.edu.um.limitadorum.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.um.limitadorum.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		if (username == null || username.isBlank()) {
			throw new UsernameNotFoundException("El username no puede ser nulo ni vacío");
		}
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con username: " + username));
	}
}
