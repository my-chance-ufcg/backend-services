package com.mychance.backend_services.service;

import com.mychance.backend_services.domain.entity.AnonymousProfile;
import com.mychance.backend_services.domain.entity.CandidateExperience;
import com.mychance.backend_services.domain.entity.JobVacancy;
import com.mychance.backend_services.domain.enums.SeniorityLevel;
import com.mychance.backend_services.domain.enums.SkillName;
import com.mychance.backend_services.dto.nlp.MatchingRankRequest;
import com.mychance.backend_services.dto.nlp.MatchingRankRequest.CandidateMatchingPayload;
import com.mychance.backend_services.dto.nlp.MatchingRankRequest.JobMatchingPayload;
import com.mychance.backend_services.dto.nlp.MatchingRankRequest.JobRequirementPayload;
import com.mychance.backend_services.util.PublicIdFormatter;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class MatchingRequestBuilder {

	private static final double RECENCY_DECAY = 0.15;

	public List<String> globalVocabulary() {
		return Arrays.stream(SkillName.values())
				.map(SkillName::getKey)
				.toList();
	}

	public MatchingRankRequest buildBatchRequest(List<AnonymousProfile> profiles, JobVacancy jobVacancy) {
		Map<String, JobRequirementPayload> competencias = new LinkedHashMap<>();
		jobVacancy.getRequirements().forEach(requirement -> competencias.put(
				requirement.getSkillName().getKey(),
				new JobRequirementPayload(
						requirement.getWeight(),
						requirement.isMandatory(),
						requirement.getMinLevel()
				)
		));

		Map<String, Integer> jobLanguages = new LinkedHashMap<>();
		jobVacancy.getLanguageRequirements().forEach(requirement ->
				jobLanguages.put(requirement.getLanguageName().getKey(), requirement.getMinLevel().getRank())
		);

		List<String> jobSoftSkills = jobVacancy.getSoftSkills().stream()
				.map(skill -> skill.getKey())
				.sorted()
				.toList();

		List<CandidateMatchingPayload> candidatos = profiles.stream()
				.map(profile -> new CandidateMatchingPayload(
						PublicIdFormatter.toPublicCandidateId(profile.getId()),
						buildCandidateSkills(profile),
						profile.getProjects().size(),
						weightedExperienceYears(profile),
						currentSeniorityRank(profile),
						buildCandidateLanguages(profile),
						profile.getSoftSkills().stream().map(skill -> skill.getKey()).sorted().toList()
				))
				.toList();

		return new MatchingRankRequest(
				globalVocabulary(),
				new JobMatchingPayload(
						competencias,
						jobVacancy.getSeniorityLevel() != null ? jobVacancy.getSeniorityLevel().getRank() : 0,
						jobLanguages,
						jobSoftSkills
				),
				candidatos
		);
	}

	private Map<String, Integer> buildCandidateSkills(AnonymousProfile profile) {
		Map<String, Integer> skills = new LinkedHashMap<>();
		profile.getSkills().forEach(skill ->
				skills.put(skill.getSkillName().getKey(), skill.getSkillLevel())
		);
		return skills;
	}

	private Map<String, Integer> buildCandidateLanguages(AnonymousProfile profile) {
		Map<String, Integer> languages = new LinkedHashMap<>();
		profile.getLanguages().forEach(language ->
				languages.put(language.getLanguageName().getKey(), language.getLanguageLevel().getRank())
		);
		return languages;
	}

	/**
	 * Weighted years of experience: recent stints count more than older ones.
	 */
	double weightedExperienceYears(AnonymousProfile profile) {
		YearMonth now = YearMonth.now();
		double weightedMonths = 0.0;
		for (CandidateExperience experience : profile.getExperiences()) {
			YearMonth start = experience.getStartYearMonth();
			YearMonth end = experience.getEndYearMonth();
			long midpointOffset = ChronoUnit.MONTHS.between(start, end) / 2;
			YearMonth midpoint = start.plusMonths(Math.max(0, midpointOffset));
			double yearsAgo = Math.max(0.0, ChronoUnit.MONTHS.between(midpoint, now) / 12.0);
			double weight = Math.exp(-RECENCY_DECAY * yearsAgo);
			weightedMonths += experience.getDurationMonths() * weight;
		}
		return weightedMonths / 12.0;
	}

	int currentSeniorityRank(AnonymousProfile profile) {
		return profile.getExperiences().stream()
				.max(Comparator
						.comparing(CandidateExperience::isCurrent)
						.thenComparing(CandidateExperience::getEndYearMonth)
						.thenComparing(CandidateExperience::getStartYearMonth))
				.map(CandidateExperience::getSeniorityLevel)
				.map(SeniorityLevel::getRank)
				.orElse(0);
	}
}
