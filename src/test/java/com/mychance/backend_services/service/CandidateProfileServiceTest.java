package com.mychance.backend_services.service;

import com.mychance.backend_services.domain.entity.Account;
import com.mychance.backend_services.domain.entity.AnonymousProfile;
import com.mychance.backend_services.dto.request.ProfileCreateRequest;
import com.mychance.backend_services.exception.InvalidSkillException;
import com.mychance.backend_services.repository.AccountRepository;
import com.mychance.backend_services.repository.AnonymousProfileRepository;
import com.mychance.backend_services.support.AccountTestBuilder;
import com.mychance.backend_services.support.ProfileRequestTestBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CandidateProfileServiceTest {

	@Autowired
	private CandidateProfileService candidateProfileService;

	@Autowired
	private AnonymousProfileRepository anonymousProfileRepository;

	@Autowired
	private AccountRepository accountRepository;

	@Test
	void rejectsUnknownSkills() {
		Account account = accountRepository.save(AccountTestBuilder.candidateAccount("skills-test@test.local"));
		ProfileCreateRequest request = ProfileRequestTestBuilder.basicProfile(
				Map.of("haskell", 5),
				List.of(ProfileRequestTestBuilder.currentExperience("Dev", "junior", 12))
		);

		assertThatThrownBy(() -> candidateProfileService.createProfile(account.getId(), request))
				.isInstanceOf(InvalidSkillException.class)
				.hasMessageContaining("haskell");
	}

	@Test
	void sanitizesProjectDescriptionsBeforePersisting() {
		Account account = accountRepository.save(AccountTestBuilder.candidateAccount("sanitize-test@test.local"));
		ProfileCreateRequest base = ProfileRequestTestBuilder.basicProfile(
				Map.of("python", 3),
				List.of(ProfileRequestTestBuilder.currentExperience("Dev", "junior", 12))
		);
		ProfileCreateRequest request = new ProfileCreateRequest(
				base.competencias(),
				base.experiencias(),
				List.of("Contato: dev@email.com — veja github.com/dev/portfolio"),
				base.nivelEscolaridade(),
				base.estado(),
				base.cursoArea(),
				base.pretensaoSalarialMinima(),
				base.modalidadesPreferidas(),
				base.vinculosPreferidos(),
				base.idiomas()
		);

		candidateProfileService.createProfile(account.getId(), request);

		AnonymousProfile profile = anonymousProfileRepository.findAll().get(0);
		String savedDescription = profile.getProjects().get(0).getDescription();

		assertThat(savedDescription).contains("[email removido]");
		assertThat(savedDescription).contains("[link removido]");
		assertThat(savedDescription).doesNotContain("dev@email.com");
	}

	@Test
	void sanitizesExperienceTitleAndStudyAreaBeforePersisting() {
		Account account = accountRepository.save(AccountTestBuilder.candidateAccount("sanitize-exp@test.local"));
		ProfileCreateRequest base = ProfileRequestTestBuilder.basicProfile(
				Map.of("python", 3),
				List.of(ProfileRequestTestBuilder.currentExperience("Engenheiro na Acme Ltda", "junior", 12))
		);
		ProfileCreateRequest request = new ProfileCreateRequest(
				base.competencias(),
				base.experiencias(),
				base.projetosDestaque(),
				base.nivelEscolaridade(),
				base.estado(),
				"Computação na empresa Nubank",
				base.pretensaoSalarialMinima(),
				base.modalidadesPreferidas(),
				base.vinculosPreferidos(),
				base.idiomas()
		);

		candidateProfileService.createProfile(account.getId(), request);

		AnonymousProfile profile = anonymousProfileRepository.findByCandidateId(account.getId()).orElseThrow();
		assertThat(profile.getExperiences()).singleElement()
				.extracting(experience -> experience.getRoleTitle())
				.satisfies(title -> {
					assertThat(title).contains("[empresa removida]");
					assertThat(title).doesNotContain("Acme");
				});
		assertThat(profile.getStudyArea()).contains("[empresa removida]").doesNotContain("Nubank");
	}

	@Test
	void persistsSkillsExperiencesAndProjects() {
		Account account = accountRepository.save(AccountTestBuilder.candidateAccount("persist-test@test.local"));
		ProfileCreateRequest request = ProfileRequestTestBuilder.basicProfile(
				Map.of("python", 4, "sql", 5),
				List.of(
						ProfileRequestTestBuilder.currentExperience("Backend Júnior", "junior", 14),
						ProfileRequestTestBuilder.experience("Pesquisador", "estagio", 1, 2022, 6, 2022, false)
				)
		);

		candidateProfileService.createProfile(account.getId(), request);

		AnonymousProfile profile = anonymousProfileRepository.findAll().get(0);

		assertThat(profile.getSkills()).hasSize(2);
		assertThat(profile.getExperiences()).hasSize(2);
		assertThat(profile.getProjects()).hasSize(1);
		assertThat(profile.getLanguages()).hasSize(2);
		assertThat(profile.getPreferredModalities()).isNotEmpty();
		assertThat(profile.getCandidate().getFullName()).isEqualTo("Candidato Teste");
	}
}
