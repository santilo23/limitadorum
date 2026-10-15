package ar.edu.um.limitadorum.services;

import ar.edu.um.limitadorum.domain.User;
import ar.edu.um.limitadorum.dto.RegisterRequest;

public interface RegistrationService {

	User register(RegisterRequest request);
}
