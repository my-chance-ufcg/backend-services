package com.mychance.backend_services.security;

public record ExternalIdentity(String subject, String email, boolean emailVerified) {
}
