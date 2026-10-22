package ar.edu.um.limitadorum.services;

import ar.edu.um.limitadorum.dto.LoginRequest;
import ar.edu.um.limitadorum.dto.LoginResponse;

public interface AuthService {

	LoginResponse login(LoginRequest request);
}
