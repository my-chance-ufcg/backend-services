package com.mychance.backend_services.support;

import com.mychance.backend_services.dto.request.ExperienceRequest;
import com.mychance.backend_services.dto.request.LanguageProficiencyRequest;
import com.mychance.backend_services.dto.request.ProfileCreateRequest;

import java.util.List;
import java.util.Map;

public final class ProfileRequestTestBuilder {

	private ProfileRequestTestBuilder() {
	}

	public static ExperienceRequest experience(
			String cargo,
			String senioridade,
			int inicioMes,
			int inicioAno,
			Integer fimMes,
			Integer fimAno,
			boolean atual
	) {
		return new ExperienceRequest(cargo, senioridade, inicioMes, inicioAno, fimMes, fimAno, atual);
	}

	public static ExperienceRequest currentExperience(String cargo, String senioridade, int monthsAgoApprox) {
		int startYear = java.time.LocalDate.now().getYear();
		int startMonth = 1;
		if (monthsAgoApprox >= 12) {
			startYear -= monthsAgoApprox / 12;
		}
		return experience(cargo, senioridade, startMonth, startYear, null, null, true);
	}

	public static ProfileCreateRequest basicProfile(Map<String, Integer> skills, List<ExperienceRequest> experiences) {
		return new ProfileCreateRequest(
				skills,
				experiences,
				List.of("Projeto de API em Python com testes e Docker."),
				"graduacao_concluida",
				"pb",
				"ciencia_computacao",
				7000,
				List.of("remoto", "hibrido"),
				List.of("clt", "bolsa_projeto"),
				List.of(new LanguageProficiencyRequest("portugues", "fluente"), new LanguageProficiencyRequest("ingles", "intermediario")),
				null,
				null,
				null
		);
	}
}
