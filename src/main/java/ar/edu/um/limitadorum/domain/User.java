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
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
@Table(name = "users")
public class User implements UserDetails {

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

	/**
	 * Genera una vista inmutable de detalles del usuario para consultas seguras (User Detail).
	 * Aplica encapsulamiento y nunca expone contraseñas ni hashes.
	 */
	public ar.edu.um.limitadorum.dto.UserDetail getDetail() {
		validateRequiredFieldsForDetail();

		String fullName = this.userData != null ? this.userData.getFullName() : null;
		String phoneNumber = this.userData != null ? this.userData.getPhoneNumber() : null;
		String address = this.userData != null ? this.userData.getAddress() : null;

		java.util.Set<String> roleDescriptions = extractRoleDescriptions();

		return new ar.edu.um.limitadorum.dto.UserDetail(
				this.id,
				this.username,
				this.email,
				fullName,
				phoneNumber,
				address,
				roleDescriptions,
				Boolean.TRUE.equals(this.active)
		);
	}

	private void validateRequiredFieldsForDetail() {
		if (this.username == null || this.username.isBlank()) {
			throw new IllegalStateException("El usuario debe tener un username válido para generar el detalle");
		}
		if (this.email == null || this.email.isBlank()) {
			throw new IllegalStateException("El usuario debe tener un email válido para generar el detalle");
		}
	}

	private java.util.Set<String> extractRoleDescriptions() {
		if (this.roles == null || this.roles.isEmpty()) {
			return java.util.Collections.emptySet();
		}
		return this.roles.stream()
				.filter(java.util.Objects::nonNull)
				.map(Role::getDescription)
				.filter(java.util.Objects::nonNull)
				.collect(java.util.stream.Collectors.toUnmodifiableSet());
	}

	@Override
	@JsonIgnore
	public Collection<? extends GrantedAuthority> getAuthorities() {
		if (this.roles == null || this.roles.isEmpty()) {
			return Collections.emptyList();
		}
		return this.roles.stream()
				.filter(Objects::nonNull)
				.map(Role::getDescription)
				.filter(Objects::nonNull)
				.map(SimpleGrantedAuthority::new)
				.toList();
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	@JsonIgnore
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	@JsonIgnore
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	@JsonIgnore
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	@JsonIgnore
	public boolean isEnabled() {
		return Boolean.TRUE.equals(this.active);
	}
}
