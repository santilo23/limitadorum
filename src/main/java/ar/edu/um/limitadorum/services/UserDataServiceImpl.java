package ar.edu.um.limitadorum.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.um.limitadorum.domain.User;
import ar.edu.um.limitadorum.domain.UserData;
import ar.edu.um.limitadorum.exception.ResourceNotFoundException;
import ar.edu.um.limitadorum.repository.UserDataRepository;
import ar.edu.um.limitadorum.repository.UserRepository;

@Service
@Transactional
public class UserDataServiceImpl implements UserDataService {

	private final UserDataRepository userDataRepository;

	private final UserRepository userRepository;

	public UserDataServiceImpl(UserDataRepository userDataRepository, UserRepository userRepository) {
		this.userDataRepository = userDataRepository;
		this.userRepository = userRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserData> findAll() {
		return userDataRepository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<UserData> findById(Long id) {
		return userDataRepository.findById(id);
	}

	/**
	 * Los datos personales no existen sin un usuario, asi que la peticion debe
	 * indicar a cual pertenecen. Se resuelve el usuario contra la base para
	 * fallar con un 404 claro si el id no existe, en lugar de con un error de
	 * clave foranea.
	 */
	@Override
	public UserData save(UserData userData) {
		userData.setUser(resolveUser(userData));
		return userDataRepository.saveAndFlush(userData);
	}

	@Override
	public UserData update(Long id, UserData userData) {
		UserData existing = userDataRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("UserData", id));
		existing.setFirstName(userData.getFirstName());
		existing.setLastName(userData.getLastName());
		existing.setAddress(userData.getAddress());
		existing.setPhoneNumber(userData.getPhoneNumber());
		if (userData.getUser() != null && userData.getUser().getId() != null) {
			existing.setUser(resolveUser(userData));
		}
		return userDataRepository.saveAndFlush(existing);
	}

	@Override
	public void deleteById(Long id) {
		if (!userDataRepository.existsById(id)) {
			throw new ResourceNotFoundException("UserData", id);
		}
		userDataRepository.deleteById(id);
	}

	private User resolveUser(UserData userData) {
		if (userData.getUser() == null || userData.getUser().getId() == null) {
			throw new IllegalArgumentException(
					"UserData requiere el id del usuario al que pertenece");
		}
		Long userId = userData.getUser().getId();
		return userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User", userId));
	}
}
