package ar.edu.um.limitadorum.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import ar.edu.um.limitadorum.dto.LoginRequest;
import ar.edu.um.limitadorum.dto.LoginResponse;
import ar.edu.um.limitadorum.security.JwtService;

@Service
public class AuthServiceImpl implements AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	public AuthServiceImpl(AuthenticationManager authenticationManager, JwtService jwtService) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
	}

	@Override
	public LoginResponse login(LoginRequest request) {
		if (request == null || request.username() == null || request.username().isBlank()
				|| request.password() == null || request.password().isBlank()) {
			throw new BadCredentialsException("Error de usuario y/o de password.");
		}

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.username(), request.password())
		);

		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String token = jwtService.generateToken(userDetails.getUsername(), request.isRememberMe());

		return new LoginResponse(token, LoginResponse.DEFAULT_SUCCESS_MESSAGE);
	}
}
