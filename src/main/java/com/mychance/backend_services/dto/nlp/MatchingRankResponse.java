package com.mychance.backend_services.dto.nlp;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record MatchingRankResponse(
		@JsonProperty("ranking") List<RankedCandidatePayload> ranking
) {
	public record RankedCandidatePayload(
			@JsonProperty("candidato_id") String candidatoId,
			@JsonProperty("compatibilidade_score") double compatibilidadeScore,
			@JsonProperty("compatibilidade") String compatibilidade,
			@JsonProperty("aprovado_filtragem") boolean aprovadoFiltragem,
			@JsonProperty("score_filtragem") Double scoreFiltragem,
			@JsonProperty("explicacao") List<String> explicacao
	) {
	}
}
