package com.mychance.backend_services.config;

import com.mychance.backend_services.security.ExternalIdentityVerifier;
import com.mychance.backend_services.security.FirebaseIdTokenVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdpConfig {

	@Bean
	@ConditionalOnProperty(name = "mychance.idp.enabled", havingValue = "true")
	ExternalIdentityVerifier externalIdentityVerifier(
			@Value("${mychance.idp.issuer}") String issuer,
			@Value("${mychance.idp.audience}") String audience,
			@Value("${mychance.idp.jwks-uri}") String jwksUri
	) {
		return FirebaseIdTokenVerifier.fromJwkSet(issuer, audience, jwksUri);
	}
}
