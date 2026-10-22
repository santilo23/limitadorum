package ar.edu.um.limitadorum.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtServiceImpl implements JwtService {

	private final long normalExpirationMs;
	private final long rememberMeExpirationMs;
	private final SecretKey signingKey;

	public JwtServiceImpl(
			@Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}") String secret,
			@Value("${jwt.expiration:86400000}") long normalExpirationMs,
			@Value("${jwt.remember-me-expiration:31536000000}") long rememberMeExpirationMs) {
		this.normalExpirationMs = normalExpirationMs;
		this.rememberMeExpirationMs = rememberMeExpirationMs;
		this.signingKey = initSigningKey(secret);
	}

	private SecretKey initSigningKey(String secret) {
		byte[] keyBytes;
		try {
			keyBytes = Decoders.BASE64.decode(secret);
			if (keyBytes.length < 32) {
				keyBytes = secret.getBytes(StandardCharsets.UTF_8);
			}
		} catch (IllegalArgumentException ex) {
			keyBytes = secret.getBytes(StandardCharsets.UTF_8);
		}
		if (keyBytes.length < 32) {
			throw new IllegalStateException("La clave secreta JWT debe tener al menos 256 bits (32 bytes)");
		}
		return Keys.hmacShaKeyFor(keyBytes);
	}

	@Override
	public String generateToken(String username, boolean rememberMe) {
		if (username == null || username.isBlank()) {
			throw new IllegalArgumentException("El username no puede ser nulo ni vacío al generar un JWT");
		}
		long duration = getExpirationDuration(rememberMe);
		Date now = new Date();
		Date expiration = new Date(now.getTime() + duration);

		// Exigencia estricta: únicamente el username como dato de negocio/identidad
		// y exp para la expiración. Sin claims estándar adicionales (sub, iss, aud, iat, jti)
		// ni datos sensibles (id, email, roles, password).
		return Jwts.builder()
				.claims(Map.of("username", username))
				.expiration(expiration)
				.signWith(signingKey)
				.compact();
	}

	@Override
	public String extractUsername(String token) {
		return extractAllClaims(token).get("username", String.class);
	}

	@Override
	public Date extractExpiration(String token) {
		return extractAllClaims(token).getExpiration();
	}

	@Override
	public Claims extractAllClaims(String token) {
		return Jwts.parser()
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	@Override
	public boolean isTokenValid(String token, String username) {
		try {
			String extracted = extractUsername(token);
			return extracted != null && extracted.equals(username) && !isTokenExpired(token);
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	@Override
	public boolean isTokenExpired(String token) {
		try {
			Date expiration = extractExpiration(token);
			return expiration != null && expiration.before(new Date());
		} catch (ExpiredJwtException e) {
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return true;
		}
	}

	@Override
	public long getExpirationDuration(boolean rememberMe) {
		return rememberMe ? rememberMeExpirationMs : normalExpirationMs;
	}
}
