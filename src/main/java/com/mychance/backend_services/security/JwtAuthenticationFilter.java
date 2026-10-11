package com.mychance.backend_services.security;

import com.mychance.backend_services.domain.entity.Account;
import com.mychance.backend_services.repository.AccountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final AccountRepository accountRepository;
	private final ObjectProvider<ExternalIdentityVerifier> identityVerifiers;

	public JwtAuthenticationFilter(
			JwtService jwtService,
			AccountRepository accountRepository,
			ObjectProvider<ExternalIdentityVerifier> identityVerifiers
	) {
		this.jwtService = jwtService;
		this.accountRepository = accountRepository;
		this.identityVerifiers = identityVerifiers;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		String authorizationHeader = request.getHeader("Authorization");

		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			String token = authorizationHeader.substring(7);
			AuthenticatedUser user = resolveUser(token);
			if (user != null) {
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						user,
						null,
						user.getAuthorities()
				);
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		}

		filterChain.doFilter(request, response);
	}

	private AuthenticatedUser resolveUser(String token) {
		if (jwtService.isValid(token)) {
			return jwtService.parseUser(token);
		}

		ExternalIdentityVerifier verifier = identityVerifiers.getIfAvailable();
		if (verifier == null) {
			return null;
		}

		try {
			ExternalIdentity identity = verifier.verify(token);
			return accountRepository.findByIdpSubject(identity.subject())
					.map(this::toAuthenticatedUser)
					.orElse(null);
		} catch (RuntimeException exception) {
			return null;
		}
	}

	private AuthenticatedUser toAuthenticatedUser(Account account) {
		return new AuthenticatedUser(account.getId(), account.getEmail(), account.getRole());
	}
}
