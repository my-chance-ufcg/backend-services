package com.mychance.backend_services.domain.entity;

import com.mychance.backend_services.domain.enums.Benefit;
import com.mychance.backend_services.domain.enums.BrazilianState;
import com.mychance.backend_services.domain.enums.EducationLevel;
import com.mychance.backend_services.domain.enums.EmploymentType;
import com.mychance.backend_services.domain.enums.SalaryRange;
import com.mychance.backend_services.domain.enums.SoftSkill;
import com.mychance.backend_services.domain.enums.WorkModality;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "anonymous_profiles")
public class AnonymousProfile {

	@Id
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "candidate_id", nullable = false, unique = true)
	private Candidate candidate;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "education_level", length = 50)
	private EducationLevel educationLevel;

	@Enumerated(EnumType.STRING)
	@Column(name = "region_state", length = 50)
	private BrazilianState regionState;

	@Column(name = "study_area", length = 150)
	private String studyArea;

	@Column(name = "salary_expectation_min")
	private Integer salaryExpectationMin;

	@Enumerated(EnumType.STRING)
	@Column(name = "salary_range", length = 30)
	private SalaryRange salaryRange;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "profile_soft_skills", joinColumns = @JoinColumn(name = "profile_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "soft_skill", nullable = false, length = 40)
	private Set<SoftSkill> softSkills = new HashSet<>();

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "profile_desired_benefits", joinColumns = @JoinColumn(name = "profile_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "benefit", nullable = false, length = 40)
	private Set<Benefit> desiredBenefits = new HashSet<>();

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "profile_preferred_modalities", joinColumns = @JoinColumn(name = "profile_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "modality", nullable = false, length = 30)
	private Set<WorkModality> preferredModalities = new HashSet<>();

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "profile_preferred_employment_types", joinColumns = @JoinColumn(name = "profile_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "employment_type", nullable = false, length = 30)
	private Set<EmploymentType> preferredEmploymentTypes = new HashSet<>();

	@OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CandidateSkill> skills = new ArrayList<>();

	@OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CandidateProject> projects = new ArrayList<>();

	@OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CandidateExperience> experiences = new ArrayList<>();

	@OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CandidateLanguage> languages = new ArrayList<>();

	protected AnonymousProfile() {
	}

	public AnonymousProfile(Candidate candidate) {
		this.candidate = candidate;
	}

	@PrePersist
	void onCreate() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public UUID getId() {
		return id;
	}

	public Candidate getCandidate() {
		return candidate;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public EducationLevel getEducationLevel() {
		return educationLevel;
	}

	public void setEducationLevel(EducationLevel educationLevel) {
		this.educationLevel = educationLevel;
	}

	public BrazilianState getRegionState() {
		return regionState;
	}

	public void setRegionState(BrazilianState regionState) {
		this.regionState = regionState;
	}

	public String getStudyArea() {
		return studyArea;
	}

	public void setStudyArea(String studyArea) {
		this.studyArea = studyArea;
	}

	public Integer getSalaryExpectationMin() {
		return salaryExpectationMin;
	}

	public void setSalaryExpectationMin(Integer salaryExpectationMin) {
		this.salaryExpectationMin = salaryExpectationMin;
	}

	public SalaryRange getSalaryRange() {
		return salaryRange;
	}

	public void setSalaryRange(SalaryRange salaryRange) {
		this.salaryRange = salaryRange;
	}

	public Set<SoftSkill> getSoftSkills() {
		return softSkills;
	}

	public void setSoftSkills(Set<SoftSkill> softSkills) {
		this.softSkills.clear();
		if (softSkills != null) {
			this.softSkills.addAll(softSkills);
		}
	}

	public Set<Benefit> getDesiredBenefits() {
		return desiredBenefits;
	}

	public void setDesiredBenefits(Set<Benefit> desiredBenefits) {
		this.desiredBenefits.clear();
		if (desiredBenefits != null) {
			this.desiredBenefits.addAll(desiredBenefits);
		}
	}

	public Set<WorkModality> getPreferredModalities() {
		return preferredModalities;
	}

	public void setPreferredModalities(Set<WorkModality> preferredModalities) {
		this.preferredModalities.clear();
		if (preferredModalities != null) {
			this.preferredModalities.addAll(preferredModalities);
		}
	}

	public Set<EmploymentType> getPreferredEmploymentTypes() {
		return preferredEmploymentTypes;
	}

	public void setPreferredEmploymentTypes(Set<EmploymentType> preferredEmploymentTypes) {
		this.preferredEmploymentTypes.clear();
		if (preferredEmploymentTypes != null) {
			this.preferredEmploymentTypes.addAll(preferredEmploymentTypes);
		}
	}

	public List<CandidateSkill> getSkills() {
		return skills;
	}

	public List<CandidateProject> getProjects() {
		return projects;
	}

	public List<CandidateExperience> getExperiences() {
		return experiences;
	}

	public List<CandidateLanguage> getLanguages() {
		return languages;
	}

	public void addSkill(CandidateSkill skill) {
		skills.add(skill);
		skill.setProfile(this);
	}

	public void addProject(CandidateProject project) {
		projects.add(project);
		project.setProfile(this);
	}

	public void addExperience(CandidateExperience experience) {
		experiences.add(experience);
		experience.setProfile(this);
	}

	public void addLanguage(CandidateLanguage language) {
		languages.add(language);
		language.setProfile(this);
	}
}
