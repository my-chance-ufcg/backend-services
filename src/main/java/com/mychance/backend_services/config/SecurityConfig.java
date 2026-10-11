package com.mychance.backend_services.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/health", "/actuator/health", "/actuator/health/**", "/actuator/prometheus").permitAll()
						.requestMatchers("/api/v1/auth/**", "/h2-console/**").permitAll()
						.requestMatchers("/api/v1/dev/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/admin/reset").hasRole("ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/v1/candidates/profiles").hasRole("CANDIDATE")
						.requestMatchers(HttpMethod.PUT, "/api/v1/candidates/profiles/me").hasRole("CANDIDATE")
						.requestMatchers(HttpMethod.GET, "/api/v1/candidates/me/**").hasRole("CANDIDATE")
						.requestMatchers(HttpMethod.POST, "/api/v1/invites/*/accept", "/api/v1/invites/*/reject")
						.hasRole("CANDIDATE")
						.requestMatchers(HttpMethod.POST, "/api/v1/jobs").hasRole("RECRUITER")
						.requestMatchers(HttpMethod.GET, "/api/v1/jobs/mine", "/api/v1/jobs/*/recommendations",
								"/api/v1/jobs/*/invites")
						.hasRole("RECRUITER")
						.requestMatchers(HttpMethod.POST, "/api/v1/jobs/*/invites").hasRole("RECRUITER")
						.requestMatchers(HttpMethod.POST, "/api/v1/invites/*/schedule").hasRole("RECRUITER")
						.requestMatchers(HttpMethod.POST, "/api/v1/invites/*/schedule/confirm").hasAnyRole("RECRUITER", "CANDIDATE")
						.requestMatchers(HttpMethod.POST, "/api/v1/invites/*/schedule/counter-propose").hasRole("CANDIDATE")
						.requestMatchers(HttpMethod.GET, "/api/v1/jobs/*").hasRole("RECRUITER")
						.requestMatchers(HttpMethod.PUT, "/api/v1/jobs/*").hasRole("RECRUITER")
						.anyRequest().authenticated()
				)
				.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
