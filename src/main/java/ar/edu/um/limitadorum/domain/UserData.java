package ar.edu.um.limitadorum.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Datos personales de un usuario. Forma una composicion con {@link User}: una
 * instancia de UserData no tiene sentido por si sola y su ciclo de vida esta
 * atado al del usuario que la contiene.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user_data")
public class UserData {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String firstName;

	@Column(nullable = false)
	private String lastName;

	private String address;

	private String phoneNumber;

	/**
	 * WRITE_ONLY: se acepta al crear o actualizar (para indicar a que usuario
	 * pertenecen los datos), pero no se serializa en las respuestas. Sin esto,
	 * User -> userData -> user -> ... entraria en recursion infinita al generar
	 * el JSON.
	 */
	@OneToOne
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private User user;

	/** Expone solo el id del usuario, en lugar del objeto completo. */
	@JsonProperty("userId")
	public Long getUserId() {
		return user != null ? user.getId() : null;
	}

	public UserData(String firstName, String lastName, String address, String phoneNumber) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.address = address;
		this.phoneNumber = phoneNumber;
	}

	public String getFullName() {
		return firstName + " " + lastName;
	}
}
