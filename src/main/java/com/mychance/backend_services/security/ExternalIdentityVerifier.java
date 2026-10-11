package com.mychance.backend_services.security;

public interface ExternalIdentityVerifier {

	ExternalIdentity verify(String idToken);
}
