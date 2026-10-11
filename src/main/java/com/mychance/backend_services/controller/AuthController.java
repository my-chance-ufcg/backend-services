package com.mychance.backend_services.controller;

import com.mychance.backend_services.domain.enums.UserRole;
import com.mychance.backend_services.dto.request.LoginRequest;
import com.mychance.backend_services.dto.request.RegisterRequest;
import com.mychance.backend_services.dto.response.AuthResponse;
import com.mychance.backend_services.security.AuthenticatedUser;
import com.mychance.backend_services.service.AuthService;
import com.mychance.backend_services.telemetry.ProductEvent;
import com.mychance.backend_services.telemetry.ProductTelemetry;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;
	private final ProductTelemetry productTelemetry;

	public AuthController(AuthService authService, ProductTelemetry productTelemetry) {
		this.authService = authService;
		this.productTelemetry = productTelemetry;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
		AuthResponse response = authService.register(request);
		if (UserRole.CANDIDATE.name().equals(response.role())) {
			productTelemetry.record(ProductEvent.CADASTRO_CANDIDATO, "account_id", response.userId().toString());
		}
		return response;
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@GetMapping("/me")
	public AuthResponse me(@AuthenticationPrincipal AuthenticatedUser user) {
		return authService.getCurrentUser(user.id());
	}
}
