package com.mychance.backend_services.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IdpAuthRequest(
		@NotBlank @JsonProperty("id_token") String idToken,
		@Size(max = 255) @JsonProperty("nome") String nome,
		@JsonProperty("role") String role,
		@Size(max = 30) @JsonProperty("telefone") String telefone
) {
}
