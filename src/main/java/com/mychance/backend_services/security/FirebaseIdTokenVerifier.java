package com.mychance.backend_services.security;

import com.mychance.backend_services.exception.InvalidCredentialsException;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

public class FirebaseIdTokenVerifier implements ExternalIdentityVerifier {

	private final JwtDecoder jwtDecoder;
	private final String audience;

	public FirebaseIdTokenVerifier(JwtDecoder jwtDecoder, String audience) {
		this.jwtDecoder = jwtDecoder;
		this.audience = audience;
	}

	public static FirebaseIdTokenVerifier fromJwkSet(String issuer, String audience, String jwksUri) {
		if (issuer == null || issuer.isBlank() || audience == null || audience.isBlank()) {
			throw new IllegalStateException(
					"MYCHANCE_IDP_ISSUER e MYCHANCE_IDP_AUDIENCE são obrigatórios quando o IdP está habilitado."
			);
		}
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwksUri).build();
		OAuth2TokenValidator<Jwt> validator = JwtValidators.createDefaultWithIssuer(issuer);
		decoder.setJwtValidator(validator);
		return new FirebaseIdTokenVerifier(decoder, audience);
	}

	@Override
	public ExternalIdentity verify(String idToken) {
		Jwt jwt;
		try {
			jwt = jwtDecoder.decode(idToken);
		} catch (JwtException exception) {
			throw new InvalidCredentialsException();
		}

		if (jwt.getSubject() == null || jwt.getSubject().isBlank()) {
			throw new InvalidCredentialsException();
		}
		if (jwt.getAudience() == null || !jwt.getAudience().contains(audience)) {
			throw new InvalidCredentialsException();
		}

		String email = jwt.getClaimAsString("email");
		if (email == null || email.isBlank()) {
			throw new InvalidCredentialsException();
		}

		return new ExternalIdentity(jwt.getSubject(), email, Boolean.TRUE.equals(jwt.getClaim("email_verified")));
	}
}
