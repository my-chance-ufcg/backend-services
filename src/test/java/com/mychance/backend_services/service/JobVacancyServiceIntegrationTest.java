package com.mychance.backend_services.service;

import com.mychance.backend_services.dto.request.JobCreateRequest;
import com.mychance.backend_services.dto.request.JobLanguageRequirementRequest;
import com.mychance.backend_services.dto.request.JobRequirementRequest;
import com.mychance.backend_services.dto.response.JobCreateResponse;
import com.mychance.backend_services.dto.response.JobDetailResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class JobVacancyServiceIntegrationTest {

	@Autowired
	private JobVacancyService jobVacancyService;

	@Test
	void acceptsSalaryBandWhenNumericSalaryIsOmitted() {
		UUID recruiterId = UUID.randomUUID();
		JobCreateResponse created = jobVacancyService.createJob(recruiterId, jobRequest(null, null, "4k_a_7k"));

		JobDetailResponse detail = jobVacancyService.getOwnedJobDetail(recruiterId, created.vagaId());

		assertThat(detail.faixaSalarial()).isEqualTo("4k_a_7k");
		assertThat(detail.salarioMinimo()).isEqualTo(4001);
		assertThat(detail.salarioMaximo()).isEqualTo(7000);
		assertThat(detail.softSkills()).containsExactly("Comunicação");
		assertThat(detail.beneficios()).containsExactly("Plano de Saúde");
	}

	@Test
	void storesTheLabelsSentByTheFrontendAndStripsContactFromTheDescription() {
		UUID recruiterId = UUID.randomUUID();
		JobCreateRequest request = new JobCreateRequest(
				"Backend",
				"Fale com o time em vaga@empresa.com ou veja https://empresa.com/vaga",
				"Laboratório",
				"remoto",
				"clt",
				null,
				"pleno",
				8000,
				12000,
				List.of(new JobRequirementRequest("python", 4, true, 3)),
				List.of(new JobLanguageRequirementRequest("portugues", "fluente")),
				null,
				List.of("Comunicação", "Resiliência"),
				List.of("Vale Refeição (VR)", "Auxílio Creche")
		);

		JobCreateResponse created = jobVacancyService.createJob(recruiterId, request);
		JobDetailResponse detail = jobVacancyService.getOwnedJobDetail(recruiterId, created.vagaId());

		assertThat(detail.descricao()).doesNotContain("vaga@empresa.com").doesNotContain("https://");
		assertThat(detail.descricao()).contains("[email removido]", "[link removido]");
		assertThat(detail.softSkills()).containsExactly("Comunicação", "Resiliência");
		assertThat(detail.beneficios()).containsExactly("Auxílio Creche", "Vale Refeição (VR)");
	}

	@Test
	void keepsExplicitSalaryFromTheCurrentClient() {
		UUID recruiterId = UUID.randomUUID();
		JobCreateResponse created = jobVacancyService.createJob(recruiterId, jobRequest(8000, 12000, null));

		JobDetailResponse detail = jobVacancyService.getOwnedJobDetail(recruiterId, created.vagaId());

		assertThat(detail.salarioMinimo()).isEqualTo(8000);
		assertThat(detail.salarioMaximo()).isEqualTo(12000);
		assertThat(detail.faixaSalarial()).isNull();
	}

	@Test
	void rejectsAJobWithoutSalaryInformation() {
		assertThatThrownBy(() -> jobVacancyService.createJob(UUID.randomUUID(), jobRequest(null, null, null)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("faixa salarial");
	}

	private JobCreateRequest jobRequest(Integer minimum, Integer maximum, String salaryRange) {
		return new JobCreateRequest(
				"Backend",
				"APIs",
				"Laboratório",
				"remoto",
				"clt",
				null,
				"pleno",
				minimum,
				maximum,
				List.of(new JobRequirementRequest("python", 4, true, 3)),
				List.of(new JobLanguageRequirementRequest("portugues", "fluente")),
				salaryRange,
				salaryRange == null ? null : List.of("comunicacao"),
				salaryRange == null ? null : List.of("plano_saude")
		);
	}
}
