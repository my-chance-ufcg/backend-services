package com.mychance.backend_services.dto.nlp;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record MatchingRankRequest(
		@JsonProperty("vocabulario") List<String> vocabulario,
		@JsonProperty("vaga") JobMatchingPayload vaga,
		@JsonProperty("candidatos") List<CandidateMatchingPayload> candidatos
) {
	public record JobMatchingPayload(
			@JsonProperty("competencias") Map<String, JobRequirementPayload> competencias,
			@JsonProperty("senioridade") int senioridade,
			@JsonProperty("idiomas") Map<String, Integer> idiomas,
			@JsonProperty("competencias_comportamentais") List<String> competenciasComportamentais
	) {
	}

	public record JobRequirementPayload(
			@JsonProperty("peso") int peso,
			@JsonProperty("obrigatoria") boolean obrigatoria,
			@JsonProperty("nivel_min") int nivelMin
	) {
	}

	public record CandidateMatchingPayload(
			@JsonProperty("candidato_id") String candidatoId,
			@JsonProperty("competencias") Map<String, Integer> competencias,
			@JsonProperty("projetos") int projetos,
			@JsonProperty("anos_experiencia") double anosExperiencia,
			@JsonProperty("senioridade") int senioridade,
			@JsonProperty("idiomas") Map<String, Integer> idiomas,
			@JsonProperty("competencias_comportamentais") List<String> competenciasComportamentais
	) {
	}
}
