package com.mychance.backend_services.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record JobCreateRequest(
		@NotBlank @JsonProperty("titulo") String titulo,
		@Size(max = 2000) @JsonProperty("descricao") String descricao,
		@NotBlank @Size(max = 255) @JsonProperty("empresa_instituicao") String empresaInstituicao,
		@NotBlank @JsonProperty("modalidade") String modalidade,
		@NotBlank @JsonProperty("tipo_vinculo") String tipoVinculo,
		@Size(max = 150) @JsonProperty("local") String local,
		@NotBlank @JsonProperty("senioridade") String senioridade,
		@Min(1) @JsonProperty("salario_minimo") Integer salarioMinimo,
		@Min(1) @JsonProperty("salario_maximo") Integer salarioMaximo,
		@NotEmpty @Valid @JsonProperty("requisitos") List<JobRequirementRequest> requisitos,
		@NotNull @Valid @JsonProperty("idiomas") List<JobLanguageRequirementRequest> idiomas,
		@JsonProperty("faixa_salarial") String faixaSalarial,
		@JsonProperty("soft_skills") List<String> softSkills,
		@JsonProperty("beneficios") List<String> beneficios
) {
}
