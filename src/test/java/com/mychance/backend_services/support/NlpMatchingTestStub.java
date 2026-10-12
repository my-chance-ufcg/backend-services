package com.mychance.backend_services.support;

import com.mychance.backend_services.dto.nlp.MatchingRankRequest;
import com.mychance.backend_services.dto.nlp.MatchingRankResponse;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class NlpMatchingTestStub {

	private NlpMatchingTestStub() {
	}

	public static MatchingRankResponse fromRequest(MatchingRankRequest request) {
		var ranking = request.candidatos().stream()
				.map(candidate -> {
					Evaluation evaluation = evaluate(candidate.competencias(), request.vaga().competencias());
					return new MatchingRankResponse.RankedCandidatePayload(
							candidate.candidatoId(),
							evaluation.score(),
							evaluation.label(),
							evaluation.approved(),
							evaluation.filterScore(),
							List.of()
					);
				})
				.sorted(Comparator.comparingDouble(MatchingRankResponse.RankedCandidatePayload::compatibilidadeScore).reversed())
				.toList();

		return new MatchingRankResponse(ranking);
	}

	private static Evaluation evaluate(
			Map<String, Integer> candidateSkills,
			Map<String, MatchingRankRequest.JobRequirementPayload> jobRequirements
	) {
		boolean approved = true;
		double weightedSum = 0.0;
		double weightTotal = 0.0;

		for (var entry : jobRequirements.entrySet()) {
			String skill = entry.getKey();
			var requirement = entry.getValue();
			int level = candidateSkills.getOrDefault(skill, 0);
			weightedSum += requirement.peso() * level;
			weightTotal += requirement.peso() * 5.0;

			if (requirement.obrigatoria() && level < requirement.nivelMin()) {
				approved = false;
			}
		}

		double filterScore = weightTotal == 0.0 ? 0.0 : weightedSum / weightTotal;
		double score = approved ? Math.min(1.0, filterScore + 0.05) : 0.0;
		String label = !approved ? "Eliminado" : score >= 0.85 ? "Excelente" : "Alta";

		return new Evaluation(approved, score, label, filterScore);
	}

	private record Evaluation(boolean approved, double score, String label, double filterScore) {
	}
}
