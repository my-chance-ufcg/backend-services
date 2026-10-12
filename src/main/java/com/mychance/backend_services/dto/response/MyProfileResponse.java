package com.mychance.backend_services.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record MyProfileResponse(
		@JsonProperty("candidato_id") String candidatoId,
		@JsonProperty("competencias") Map<String, Integer> competencias,
		@JsonProperty("experiencias") List<ExperienceResponse> experiencias,
		@JsonProperty("projetos_destaque") List<String> projetosDestaque,
		@JsonProperty("nivel_escolaridade") String nivelEscolaridade,
		@JsonProperty("estado") String estado,
		@JsonProperty("curso_area") String cursoArea,
		@JsonProperty("pretensao_salarial_minima") Integer pretensaoSalarialMinima,
		@JsonProperty("modalidades_preferidas") List<String> modalidadesPreferidas,
		@JsonProperty("vinculos_preferidos") List<String> vinculosPreferidos,
		@JsonProperty("idiomas") List<LanguageProficiencyResponse> idiomas,
		@JsonProperty("faixa_salarial") String faixaSalarial,
		@JsonProperty("soft_skills") List<String> softSkills,
		@JsonProperty("beneficios") List<String> beneficios
) {
}
