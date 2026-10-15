package ar.edu.um.limitadorum.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String username;

	@Column(nullable = false)
	private String email;

	// Nullable para no romper filas existentes (ddl-auto=update agrega la columna
	// sin asignar contrasenas conocidas). WRITE_ONLY: se recibe en POST/PUT pero
	// nunca se serializa en respuestas. Excluido del toString para no filtrarlo en logs.
	@ToString.Exclude
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column
	private String password;

	private Boolean active;

	@ManyToMany
	@JoinTable(name = "user_roles",
			joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles = new HashSet<>();

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private UserData userData;

	/**
	 * Asigna los datos personales manteniendo sincronizados ambos lados de la
	 * relacion. Lombok no genera este setter porque ya esta declarado aca.
	 */
	public void setUserData(UserData userData) {
		if (userData != null) {
			userData.setUser(this);
		}
		this.userData = userData;
	}

}
