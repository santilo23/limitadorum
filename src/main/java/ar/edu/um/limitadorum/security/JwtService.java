package ar.edu.um.limitadorum.security;

import java.util.Date;

import io.jsonwebtoken.Claims;

public interface JwtService {

	/**
	 * Genera un JWT firmado con HMAC-SHA256 que contiene ÚNICAMENTE el username
	 * como claim de negocio/identidad y exp para el vencimiento.
	 *
	 * @param username   nombre del usuario autenticado
	 * @param rememberMe si es true, expiración a 1 año; si es false, duración normal
	 * @return token JWT compacto y firmado
	 */
	String generateToken(String username, boolean rememberMe);

	/**
	 * Extrae el username contenido en el token.
	 */
	String extractUsername(String token);

	/**
	 * Extrae la fecha de expiración del token.
	 */
	Date extractExpiration(String token);

	/**
	 * Extrae todos los claims verificando la firma del token.
	 */
	Claims extractAllClaims(String token);

	/**
	 * Valida que el token corresponda al username indicado y no haya expirado.
	 */
	boolean isTokenValid(String token, String username);

	/**
	 * Determina si el token se encuentra expirado.
	 */
	boolean isTokenExpired(String token);

	/**
	 * Retorna la duración en milisegundos correspondiente a rememberMe.
	 */
	long getExpirationDuration(boolean rememberMe);
}
