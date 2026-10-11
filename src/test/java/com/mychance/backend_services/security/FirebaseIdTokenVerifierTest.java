package com.mychance.backend_services.security;

import com.mychance.backend_services.exception.InvalidCredentialsException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FirebaseIdTokenVerifierTest {

	private static final String AUDIENCE = "demo-project";
	private static final String ISSUER = "https://securetoken.google.com/demo-project";

	private RSAKey rsaKey;
	private FirebaseIdTokenVerifier verifier;

	@BeforeEach
	void setUp() throws Exception {
		rsaKey = new RSAKeyGenerator(2048).keyID("test-key").generate();
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
		decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(ISSUER));
		verifier = new FirebaseIdTokenVerifier(decoder, AUDIENCE);
	}

	@Test
	void acceptsATokenIssuedForThisProject() throws Exception {
		ExternalIdentity identity = verifier.verify(token("uid-1", "ana@example.com", true, AUDIENCE));

		assertThat(identity.subject()).isEqualTo("uid-1");
		assertThat(identity.email()).isEqualTo("ana@example.com");
		assertThat(identity.emailVerified()).isTrue();
	}

	@Test
	void rejectsATokenIssuedForAnotherProject() {
		assertThatThrownBy(() -> verifier.verify(token("uid-1", "ana@example.com", true, "other-project")))
				.isInstanceOf(InvalidCredentialsException.class);
	}

	private String token(String subject, String email, boolean verified, String audience) throws Exception {
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.subject(subject)
				.issuer(ISSUER)
				.audience(audience)
				.claim("email", email)
				.claim("email_verified", verified)
				.issueTime(new Date())
				.expirationTime(Date.from(Instant.now().plusSeconds(600)))
				.build();
		SignedJWT signedJwt = new SignedJWT(
				new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
				claims
		);
		signedJwt.sign(new RSASSASigner(rsaKey));
		return signedJwt.serialize();
	}
}
