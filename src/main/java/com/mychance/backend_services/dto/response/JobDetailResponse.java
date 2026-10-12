package com.mychance.backend_services.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record JobDetailResponse(
		@JsonProperty("vaga_id") UUID vagaId,
		@JsonProperty("titulo") String titulo,
		@JsonProperty("descricao") String descricao,
		@JsonProperty("empresa_instituicao") String empresaInstituicao,
		@JsonProperty("modalidade") String modalidade,
		@JsonProperty("tipo_vinculo") String tipoVinculo,
		@JsonProperty("local") String local,
		@JsonProperty("senioridade") String senioridade,
		@JsonProperty("salario_minimo") Integer salarioMinimo,
		@JsonProperty("salario_maximo") Integer salarioMaximo,
		@JsonProperty("requisitos") List<JobRequirementDetailResponse> requisitos,
		@JsonProperty("idiomas") List<JobLanguageRequirementDetailResponse> idiomas,
		@JsonProperty("faixa_salarial") String faixaSalarial,
		@JsonProperty("soft_skills") List<String> softSkills,
		@JsonProperty("beneficios") List<String> beneficios
) {
}
