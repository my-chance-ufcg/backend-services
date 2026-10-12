package com.mychance.backend_services.service;

import com.mychance.backend_services.domain.entity.Account;
import com.mychance.backend_services.domain.entity.AnonymousProfile;
import com.mychance.backend_services.domain.entity.Candidate;
import com.mychance.backend_services.domain.entity.CandidateExperience;
import com.mychance.backend_services.domain.entity.CandidateLanguage;
import com.mychance.backend_services.domain.entity.CandidateProject;
import com.mychance.backend_services.domain.entity.CandidateSkill;
import com.mychance.backend_services.domain.enums.Benefit;
import com.mychance.backend_services.domain.enums.BrazilianState;
import com.mychance.backend_services.domain.enums.EducationLevel;
import com.mychance.backend_services.domain.enums.EmploymentType;
import com.mychance.backend_services.domain.enums.LanguageLevel;
import com.mychance.backend_services.domain.enums.LanguageName;
import com.mychance.backend_services.domain.enums.SalaryRange;
import com.mychance.backend_services.domain.enums.SeniorityLevel;
import com.mychance.backend_services.domain.enums.SkillName;
import com.mychance.backend_services.domain.enums.SoftSkill;
import com.mychance.backend_services.domain.enums.UserRole;
import com.mychance.backend_services.domain.enums.WorkModality;
import com.mychance.backend_services.dto.request.ExperienceRequest;
import com.mychance.backend_services.dto.request.LanguageProficiencyRequest;
import com.mychance.backend_services.dto.request.ProfileCreateRequest;
import com.mychance.backend_services.dto.response.ExperienceResponse;
import com.mychance.backend_services.dto.response.LanguageProficiencyResponse;
import com.mychance.backend_services.dto.response.MyProfileResponse;
import com.mychance.backend_services.dto.response.ProfileCreateResponse;
import com.mychance.backend_services.exception.InvalidEducationLevelException;
import com.mychance.backend_services.exception.InvalidStateException;
import com.mychance.backend_services.exception.InvalidSkillException;
import com.mychance.backend_services.exception.ProfileAlreadyExistsException;
import com.mychance.backend_services.exception.ProfileNotFoundException;
import com.mychance.backend_services.exception.ResourceAccessDeniedException;
import com.mychance.backend_services.repository.AccountRepository;
import com.mychance.backend_services.repository.AnonymousProfileRepository;
import com.mychance.backend_services.repository.CandidateRepository;
import com.mychance.backend_services.util.PublicIdFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class CandidateProfileService {

	private final AccountRepository accountRepository;
	private final CandidateRepository candidateRepository;
	private final AnonymousProfileRepository anonymousProfileRepository;
	private final TextSanitizerService textSanitizerService;
	private final InterviewInviteService interviewInviteService;

	public CandidateProfileService(
			AccountRepository accountRepository,
			CandidateRepository candidateRepository,
			AnonymousProfileRepository anonymousProfileRepository,
			TextSanitizerService textSanitizerService,
			InterviewInviteService interviewInviteService
	) {
		this.accountRepository = accountRepository;
		this.candidateRepository = candidateRepository;
		this.anonymousProfileRepository = anonymousProfileRepository;
		this.textSanitizerService = textSanitizerService;
		this.interviewInviteService = interviewInviteService;
	}

	@Transactional
	public ProfileCreateResponse createProfile(UUID accountId, ProfileCreateRequest request) {
		Account account = getCandidateAccount(accountId);
		if (anonymousProfileRepository.findByCandidateId(accountId).isPresent()) {
			throw new ProfileAlreadyExistsException();
		}

		Candidate candidate = candidateRepository.save(new Candidate(account));
		AnonymousProfile profile = new AnonymousProfile(candidate);
		applyProfileData(request, profile);

		AnonymousProfile saved = anonymousProfileRepository.save(profile);
		return new ProfileCreateResponse(
				PublicIdFormatter.toPublicCandidateId(saved.getId()),
				"Perfil anonimizado criado com sucesso"
		);
	}

	@Transactional
	public ProfileCreateResponse updateProfile(UUID accountId, ProfileCreateRequest request) {
		AnonymousProfile profile = getOwnedProfile(accountId);
		initializeProfileDetails(profile);
		profile.getSkills().clear();
		profile.getExperiences().clear();
		profile.getProjects().clear();
		profile.getLanguages().clear();
		applyProfileData(request, profile);

		AnonymousProfile saved = anonymousProfileRepository.save(profile);
		interviewInviteService.invalidatePendingInvitesForProfile(saved);

		return new ProfileCreateResponse(
				PublicIdFormatter.toPublicCandidateId(saved.getId()),
				"Perfil anonimizado atualizado com sucesso"
		);
	}

	@Transactional(readOnly = true)
	public MyProfileResponse getMyProfile(UUID accountId) {
		AnonymousProfile profile = getOwnedProfile(accountId);
		initializeProfileDetails(profile);

		Map<String, Integer> competencias = new LinkedHashMap<>();
		profile.getSkills().forEach(skill ->
				competencias.put(skill.getSkillName().getKey(), skill.getSkillLevel())
		);

		List<ExperienceResponse> experiencias = profile.getExperiences().stream()
				.map(this::toExperienceResponse)
				.toList();

		List<String> projetos = profile.getProjects().stream()
				.map(CandidateProject::getDescription)
				.toList();

		List<String> modalidades = profile.getPreferredModalities().stream()
				.map(WorkModality::getKey)
				.sorted()
				.toList();

		List<String> vinculos = 				profile.getPreferredEmploymentTypes().stream()
				.map(EmploymentType::getKey)
				.sorted()
				.toList();

		List<String> softSkills = profile.getSoftSkills().stream()
				.map(SoftSkill::getKey)
				.sorted()
				.toList();

		List<String> beneficios = profile.getDesiredBenefits().stream()
				.map(Benefit::getKey)
				.sorted()
				.toList();

		List<LanguageProficiencyResponse> idiomas = profile.getLanguages().stream()
				.map(language -> new LanguageProficiencyResponse(
						language.getLanguageName().getKey(),
						language.getLanguageLevel().getKey()
				))
				.toList();

		return new MyProfileResponse(
				PublicIdFormatter.toPublicCandidateId(profile.getId()),
				competencias,
				experiencias,
				projetos,
				profile.getEducationLevel() != null ? profile.getEducationLevel().getKey() : null,
				profile.getRegionState() != null ? profile.getRegionState().getKey() : null,
				profile.getStudyArea(),
				profile.getSalaryExpectationMin(),
				modalidades,
				vinculos,
				idiomas,
				profile.getSalaryRange() != null ? profile.getSalaryRange().getKey() : null,
				softSkills,
				beneficios
		);
	}

	private Account getCandidateAccount(UUID accountId) {
		Account account = accountRepository.findById(accountId)
				.orElseThrow(() -> new ResourceAccessDeniedException("Account not found"));
		if (account.getRole() != UserRole.CANDIDATE) {
			throw new ResourceAccessDeniedException("Only candidates can manage profiles");
		}
		return account;
	}

	private AnonymousProfile getOwnedProfile(UUID accountId) {
		getCandidateAccount(accountId);
		return anonymousProfileRepository.findByCandidateId(accountId)
				.orElseThrow(() -> new ProfileNotFoundException("me"));
	}

	private void applyProfileData(ProfileCreateRequest request, AnonymousProfile profile) {
		mapSkills(request.competencias(), profile);
		mapExperiences(request, profile);
		mapProjects(request, profile);
		mapLanguages(request.idiomas(), profile);
		mapAnonymizedMetadata(request, profile);
		applyCompensationPreferences(request, profile);
		profile.setPreferredModalities(parseModalities(request.modalidadesPreferidas()));
		profile.setPreferredEmploymentTypes(parseEmploymentTypes(request.vinculosPreferidos()));
	}

	private void applyCompensationPreferences(ProfileCreateRequest request, AnonymousProfile profile) {
		SalaryRange salaryRange = null;
		if (request.faixaSalarial() != null && !request.faixaSalarial().isBlank()) {
			salaryRange = SalaryRange.fromKey(request.faixaSalarial());
		}
		profile.setSalaryRange(salaryRange);
		if (salaryRange != null && salaryRange.getMinimum() != null) {
			profile.setSalaryExpectationMin(salaryRange.getMinimum());
		} else {
			profile.setSalaryExpectationMin(request.pretensaoSalarialMinima());
		}
		profile.setSoftSkills(SoftSkill.parseAll(request.softSkills()));
		profile.setDesiredBenefits(Benefit.parseAll(request.beneficios()));
	}

	private void mapAnonymizedMetadata(ProfileCreateRequest request, AnonymousProfile profile) {
		if (request.nivelEscolaridade() != null && !request.nivelEscolaridade().isBlank()) {
			if (!EducationLevel.isValidKey(request.nivelEscolaridade())) {
				throw new InvalidEducationLevelException(request.nivelEscolaridade());
			}
			profile.setEducationLevel(EducationLevel.fromKey(request.nivelEscolaridade()));
		}

		if (request.estado() != null && !request.estado().isBlank()) {
			if (!BrazilianState.isValidKey(request.estado())) {
				throw new InvalidStateException(request.estado());
			}
			profile.setRegionState(BrazilianState.fromKey(request.estado()));
		} else {
			profile.setRegionState(null);
		}

		if (request.cursoArea() != null && !request.cursoArea().isBlank()) {
			profile.setStudyArea(textSanitizerService.sanitize(request.cursoArea().trim()));
		} else {
			profile.setStudyArea(null);
		}
	}

	private AnonymousProfile findProfileByPublicId(String publicCandidateId) {
		String prefix = PublicIdFormatter.extractPrefix(publicCandidateId);
		return anonymousProfileRepository.findByPublicIdPrefix(prefix)
				.orElseThrow(() -> new ProfileNotFoundException(publicCandidateId));
	}

	private void mapSkills(Map<String, Integer> competencias, AnonymousProfile profile) {
		for (Map.Entry<String, Integer> entry : competencias.entrySet()) {
			if (!SkillName.isValidKey(entry.getKey())) {
				throw new InvalidSkillException(entry.getKey());
			}
			SkillName skillName = SkillName.fromKey(entry.getKey());
			profile.addSkill(new CandidateSkill(skillName, entry.getValue()));
		}
	}

	private void mapExperiences(ProfileCreateRequest request, AnonymousProfile profile) {
		request.experiencias().forEach(experience -> profile.addExperience(toExperienceEntity(experience)));
	}

	private CandidateExperience toExperienceEntity(ExperienceRequest experience) {
		if (!SeniorityLevel.isValidKey(experience.senioridade())) {
			throw new IllegalArgumentException("Senioridade inválida: " + experience.senioridade());
		}

		boolean current = Boolean.TRUE.equals(experience.atual());
		Integer endMonth = current ? null : experience.fimMes();
		Integer endYear = current ? null : experience.fimAno();
		if (!current && (endMonth == null || endYear == null)) {
			throw new IllegalArgumentException("Informe mês/ano de fim ou marque a experiência como atual.");
		}

		return new CandidateExperience(
				sanitizeFreeText(experience.cargo()),
				SeniorityLevel.fromKey(experience.senioridade()),
				experience.inicioMes(),
				experience.inicioAno(),
				endMonth,
				endYear,
				current
		);
	}

	private void mapProjects(ProfileCreateRequest request, AnonymousProfile profile) {
		request.projetosDestaque().forEach(projectDescription ->
				profile.addProject(new CandidateProject(sanitizeFreeText(projectDescription)))
		);
	}

	private String sanitizeFreeText(String value) {
		String sanitized = textSanitizerService.sanitize(value == null ? null : value.trim());
		if (sanitized == null || sanitized.isBlank()) {
			return "[conteúdo removido]";
		}
		return sanitized;
	}

	private void mapLanguages(List<LanguageProficiencyRequest> idiomas, AnonymousProfile profile) {
		for (LanguageProficiencyRequest idioma : idiomas) {
			if (!LanguageName.isValidKey(idioma.idioma())) {
				throw new IllegalArgumentException("Idioma inválido: " + idioma.idioma());
			}
			if (!LanguageLevel.isValidKey(idioma.nivel())) {
				throw new IllegalArgumentException("Nível de idioma inválido: " + idioma.nivel());
			}
			profile.addLanguage(new CandidateLanguage(
					LanguageName.fromKey(idioma.idioma()),
					LanguageLevel.fromKey(idioma.nivel())
			));
		}
	}

	private Set<WorkModality> parseModalities(List<String> values) {
		Set<WorkModality> modalities = new LinkedHashSet<>();
		for (String value : values) {
			if (!WorkModality.isValidKey(value)) {
				throw new IllegalArgumentException("Modalidade inválida: " + value);
			}
			modalities.add(WorkModality.fromKey(value));
		}
		return modalities;
	}

	private Set<EmploymentType> parseEmploymentTypes(List<String> values) {
		Set<EmploymentType> types = new LinkedHashSet<>();
		for (String value : values) {
			if (!EmploymentType.isValidKey(value)) {
				throw new IllegalArgumentException("Tipo de vínculo inválido: " + value);
			}
			types.add(EmploymentType.fromKey(value));
		}
		return types;
	}

	private ExperienceResponse toExperienceResponse(CandidateExperience experience) {
		return new ExperienceResponse(
				experience.getRoleTitle(),
				experience.getSeniorityLevel().getKey(),
				experience.getStartMonth(),
				experience.getStartYear(),
				experience.getEndMonth(),
				experience.getEndYear(),
				experience.isCurrent(),
				experience.getDurationMonths()
		);
	}

	private void initializeProfileDetails(AnonymousProfile profile) {
		profile.getSkills().size();
		profile.getProjects().size();
		profile.getExperiences().size();
		profile.getLanguages().size();
		profile.getPreferredModalities().size();
		profile.getPreferredEmploymentTypes().size();
		profile.getSoftSkills().size();
		profile.getDesiredBenefits().size();
	}

	AnonymousProfile findProfileByPublicIdForInternalUse(String publicCandidateId) {
		return findProfileByPublicId(publicCandidateId);
	}
}
