package ar.edu.um.limitadorum.dto;

import java.util.Collections;
import java.util.Set;

public record UserDetail(
		Long id,
		String username,
		String email,
		String fullName,
		String phoneNumber,
		String address,
		Set<String> roles,
		boolean active
) {
	public UserDetail {
		roles = roles != null ? Collections.unmodifiableSet(roles) : Collections.emptySet();
	}
}
