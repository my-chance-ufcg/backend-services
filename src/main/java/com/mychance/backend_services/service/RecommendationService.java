package com.mychance.backend_services.service;

import com.mychance.backend_services.domain.entity.AnonymousProfile;
import com.mychance.backend_services.domain.entity.CandidateLanguage;
import com.mychance.backend_services.domain.entity.InterviewInvite;
import com.mychance.backend_services.domain.entity.JobLanguageRequirement;
import com.mychance.backend_services.domain.entity.JobVacancy;
import com.mychance.backend_services.domain.enums.InviteStatus;
import com.mychance.backend_services.domain.enums.LanguageLevel;
import com.mychance.backend_services.domain.enums.LanguageName;
import com.mychance.backend_services.domain.enums.WorkModality;
import com.mychance.backend_services.dto.nlp.MatchingRankRequest;
import com.mychance.backend_services.dto.nlp.MatchingRankResponse;
import com.mychance.backend_services.dto.response.ExperienceResponse;
import com.mychance.backend_services.dto.response.RecommendationResponse;
import com.mychance.backend_services.exception.JobNotFoundException;
import com.mychance.backend_services.repository.AnonymousProfileRepository;
import com.mychance.backend_services.repository.InterviewInviteRepository;
import com.mychance.backend_services.repository.JobVacancyRepository;
import com.mychance.backend_services.util.PublicIdFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

	private final JobVacancyRepository jobVacancyRepository;
	private final AnonymousProfileRepository anonymousProfileRepository;
	private final MatchingRequestBuilder matchingRequestBuilder;
	private final NlpMatchingClient nlpMatchingClient;
	private final InterviewInviteRepository interviewInviteRepository;

	public RecommendationService(
			JobVacancyRepository jobVacancyRepository,
			AnonymousProfileRepository anonymousProfileRepository,
			MatchingRequestBuilder matchingRequestBuilder,
			NlpMatchingClient nlpMatchingClient,
			InterviewInviteRepository interviewInviteRepository
	) {
		this.jobVacancyRepository = jobVacancyRepository;
		this.anonymousProfileRepository = anonymousProfileRepository;
		this.matchingRequestBuilder = matchingRequestBuilder;
		this.nlpMatchingClient = nlpMatchingClient;
		this.interviewInviteRepository = interviewInviteRepository;
	}

	@Transactional(readOnly = true)
	public List<RecommendationResponse> getRecommendations(UUID jobId) {
		JobVacancy jobVacancy = jobVacancyRepository.findByIdWithRequirements(jobId)
				.orElseThrow(() -> new JobNotFoundException(jobId));
		jobVacancy.getLanguageRequirements().size();
		jobVacancy.getSoftSkills().size();

		Map<String, InviteStatus> inviteStatusByCandidate = interviewInviteRepository.findByJobId(jobId).stream()
				.collect(Collectors.toMap(
						invite -> PublicIdFormatter.toPublicCandidateId(invite.getProfile().getId()),
						InterviewInvite::getStatus,
						(existing, replacement) -> existing
				));

		Set<String> activeInviteCandidateIds = inviteStatusByCandidate.entrySet().stream()
				.filter(entry -> entry.getValue() == InviteStatus.ENVIADO
						|| entry.getValue() == InviteStatus.ACEITO)
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());

		List<AnonymousProfile> profiles = anonymousProfileRepository.findAll().stream()
				.filter(profile -> isCompatible(profile, jobVacancy))
				.toList();
		profiles.forEach(this::initializeProfileDetails);

		if (profiles.isEmpty()) {
			return List.of();
		}

		MatchingRankRequest request = matchingRequestBuilder.buildBatchRequest(profiles, jobVacancy);
		MatchingRankResponse nlpResponse = nlpMatchingClient.rankCandidates(request);

		Map<String, AnonymousProfile> profilesByPublicId = profiles.stream()
				.collect(Collectors.toMap(
						profile -> PublicIdFormatter.toPublicCandidateId(profile.getId()),
						Function.identity()
				));

		List<RecommendationResponse> ranked = nlpResponse.ranking().stream()
				.filter(ranking -> ranking.aprovadoFiltragem() && ranking.compatibilidadeScore() > 0.0)
				.map(ranking -> toRecommendation(
						ranking,
						profilesByPublicId.get(ranking.candidatoId()),
						inviteStatusByCandidate.get(ranking.candidatoId())
				))
				.filter(recommendation -> recommendation != null)
				.filter(recommendation -> !activeInviteCandidateIds.contains(recommendation.candidatoId()))
				.sorted(Comparator.comparingDouble(RecommendationResponse::compatibilidadeScore).reversed())
				.toList();

		AtomicInteger position = new AtomicInteger(1);
		return ranked.stream()
				.map(recommendation -> withPosition(recommendation, position.getAndIncrement()))
				.toList();
	}

	private RecommendationResponse withPosition(RecommendationResponse recommendation, int position) {
		return new RecommendationResponse(
				position,
				recommendation.candidatoId(),
				recommendation.compatibilidadeScore(),
				recommendation.compatibilidade(),
				recommendation.competenciasTecnicas(),
				recommendation.experiencias(),
				recommendation.projetosDestaque(),
				recommendation.conviteStatus(),
				recommendation.faixaSalarial(),
				recommendation.softSkills(),
				recommendation.beneficios(),
				recommendation.explicacao()
		);
	}

	private boolean isCompatible(AnonymousProfile profile, JobVacancy jobVacancy) {
		return isSalaryCompatible(profile, jobVacancy)
				&& isModalityCompatible(profile, jobVacancy)
				&& isEmploymentCompatible(profile, jobVacancy)
				&& isLanguageCompatible(profile, jobVacancy)
				&& isLocationCompatible(profile, jobVacancy);
	}

	private boolean isLocationCompatible(AnonymousProfile profile, JobVacancy jobVacancy) {
		if (jobVacancy.getWorkModality() != WorkModality.PRESENCIAL) {
			return true;
		}
		if (jobVacancy.getLocation() == null || profile.getRegionState() == null) {
			return false;
		}
		return jobVacancy.getLocation().equalsIgnoreCase(profile.getRegionState().getKey());
	}

	private boolean isSalaryCompatible(AnonymousProfile profile, JobVacancy jobVacancy) {
		if (jobVacancy.getMaxSalary() == null || profile.getSalaryExpectationMin() == null) {
			return true;
		}
		return profile.getSalaryExpectationMin() <= jobVacancy.getMaxSalary();
	}

	private boolean isModalityCompatible(AnonymousProfile profile, JobVacancy jobVacancy) {
		if (profile.getPreferredModalities().isEmpty() || jobVacancy.getWorkModality() == null) {
			return true;
		}
		return profile.getPreferredModalities().contains(jobVacancy.getWorkModality());
	}

	private boolean isEmploymentCompatible(AnonymousProfile profile, JobVacancy jobVacancy) {
		if (profile.getPreferredEmploymentTypes().isEmpty() || jobVacancy.getEmploymentType() == null) {
			return true;
		}
		return profile.getPreferredEmploymentTypes().contains(jobVacancy.getEmploymentType());
	}

	private boolean isLanguageCompatible(AnonymousProfile profile, JobVacancy jobVacancy) {
		List<JobLanguageRequirement> required = jobVacancy.getLanguageRequirements();
		if (required == null || required.isEmpty()) {
			return true;
		}

		Map<LanguageName, LanguageLevel> candidateLevels = profile.getLanguages().stream()
				.collect(Collectors.toMap(
						CandidateLanguage::getLanguageName,
						CandidateLanguage::getLanguageLevel,
						(existing, replacement) -> existing.getRank() >= replacement.getRank() ? existing : replacement
				));

		for (JobLanguageRequirement requirement : required) {
			LanguageLevel candidateLevel = candidateLevels.get(requirement.getLanguageName());
			if (candidateLevel == null || !candidateLevel.meetsOrExceeds(requirement.getMinLevel())) {
				return false;
			}
		}
		return true;
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

	private RecommendationResponse toRecommendation(
			MatchingRankResponse.RankedCandidatePayload ranking,
			AnonymousProfile profile,
			InviteStatus inviteStatus
	) {
		if (profile == null) {
			return null;
		}

		List<String> competencias = profile.getSkills().stream()
				.filter(skill -> skill.getSkillLevel() > 0)
				.map(skill -> skill.getSkillName().getDisplayName())
				.toList();

		List<ExperienceResponse> experiencias = profile.getExperiences().stream()
				.map(experience -> new ExperienceResponse(
						experience.getRoleTitle(),
						experience.getSeniorityLevel().getKey(),
						experience.getStartMonth(),
						experience.getStartYear(),
						experience.getEndMonth(),
						experience.getEndYear(),
						experience.isCurrent(),
						experience.getDurationMonths()
				))
				.toList();

		List<String> projetos = profile.getProjects().stream()
				.map(project -> project.getDescription())
				.toList();

		String conviteStatus = inviteStatus == InviteStatus.RECUSADO ? InviteStatus.RECUSADO.name() : null;

		return new RecommendationResponse(
				0,
				ranking.candidatoId(),
				Math.round(ranking.compatibilidadeScore() * 100.0) / 100.0,
				ranking.compatibilidade(),
				competencias,
				experiencias,
				projetos,
				conviteStatus,
				profile.getSalaryRange() != null ? profile.getSalaryRange().getKey() : null,
				profile.getSoftSkills().stream().map(skill -> skill.getKey()).sorted().toList(),
				profile.getDesiredBenefits().stream().map(benefit -> benefit.getKey()).sorted().toList(),
				ranking.explicacao() == null ? List.of() : ranking.explicacao()
		);
	}
}
