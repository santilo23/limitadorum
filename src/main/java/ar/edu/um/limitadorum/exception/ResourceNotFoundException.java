package ar.edu.um.limitadorum.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Se lanza cuando se pide una entidad que no existe. La anotacion
 * {@code @ResponseStatus} hace que Spring responda 404 en lugar de 500, que es
 * lo que corresponde para un recurso inexistente.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String entity, Long id) {
		super(entity + " no encontrado con id: " + id);
	}
}
