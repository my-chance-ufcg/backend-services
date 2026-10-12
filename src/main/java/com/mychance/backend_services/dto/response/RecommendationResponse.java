package com.mychance.backend_services.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RecommendationResponse(
		@JsonProperty("posicao") int posicao,
		@JsonProperty("candidato_id") String candidatoId,
		@JsonProperty("compatibilidade_score") double compatibilidadeScore,
		@JsonProperty("compatibilidade") String compatibilidade,
		@JsonProperty("competencias_tecnicas") List<String> competenciasTecnicas,
		@JsonProperty("experiencias") List<ExperienceResponse> experiencias,
		@JsonProperty("projetos_destaque") List<String> projetosDestaque,
		@JsonProperty("convite_status") String conviteStatus,
		@JsonProperty("faixa_salarial") String faixaSalarial,
		@JsonProperty("soft_skills") List<String> softSkills,
		@JsonProperty("beneficios") List<String> beneficios,
		@JsonProperty("explicacao") List<String> explicacao
) {
}
