package ar.edu.um.limitadorum.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Role {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String description;

	/**
	 * No se serializa: User ya expone sus roles, y devolver aca los usuarios de
	 * cada rol provocaria recursion infinita al generar el JSON.
	 */
	@ManyToMany(mappedBy = "roles")
	@JsonIgnore
	private Set<User> users = new HashSet<>();


	public Role(String description) {
		this.description = description;
	}

	/**
	 * Ejemplo de un metodo. Lleva {@code @JsonIgnore} porque Jackson serializa
	 * cualquier getter: si un Role llega sin description cargada, la
	 * serializacion fallaria con NullPointerException.
	 */
	@JsonIgnore
	public String getUpperCaseDescription() {
		return description != null ? description.toUpperCase() : null;
	}
}
