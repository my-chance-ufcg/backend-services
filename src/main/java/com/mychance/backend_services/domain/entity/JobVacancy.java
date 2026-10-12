package com.mychance.backend_services.domain.entity;

import com.mychance.backend_services.domain.enums.Benefit;
import com.mychance.backend_services.domain.enums.EmploymentType;
import com.mychance.backend_services.domain.enums.SalaryRange;
import com.mychance.backend_services.domain.enums.SeniorityLevel;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "job_vacancies")
public class JobVacancy {

	@Id
	private UUID id;

	@Column(nullable = false, length = 255)
	private String title;

	@Column(name = "recruiter_id", nullable = false)
	private UUID recruiterId;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Column(name = "company_name", nullable = false, length = 255)
	private String companyName;

	@Enumerated(EnumType.STRING)
	@Column(name = "work_modality", nullable = false, length = 30)
	private WorkModality workModality;

	@Enumerated(EnumType.STRING)
	@Column(name = "employment_type", nullable = false, length = 30)
	private EmploymentType employmentType;

	@Column(name = "location", length = 150)
	private String location;

	@Enumerated(EnumType.STRING)
	@Column(name = "seniority_level", nullable = false, length = 50)
	private SeniorityLevel seniorityLevel;

	@Column(name = "min_salary")
	private Integer minSalary;

	@Column(name = "max_salary")
	private Integer maxSalary;

	@Enumerated(EnumType.STRING)
	@Column(name = "salary_range", length = 30)
	private SalaryRange salaryRange;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "job_soft_skills", joinColumns = @JoinColumn(name = "job_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "soft_skill", nullable = false, length = 40)
	private Set<SoftSkill> softSkills = new HashSet<>();

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "job_benefits", joinColumns = @JoinColumn(name = "job_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "benefit", nullable = false, length = 40)
	private Set<Benefit> benefits = new HashSet<>();

	@OneToMany(mappedBy = "jobVacancy", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<JobRequirement> requirements = new ArrayList<>();

	@OneToMany(mappedBy = "jobVacancy", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<JobLanguageRequirement> languageRequirements = new ArrayList<>();

	protected JobVacancy() {
	}

	public JobVacancy(String title, UUID recruiterId) {
		this.title = title;
		this.recruiterId = recruiterId;
	}

	public JobVacancy(
			String title,
			UUID recruiterId,
			String description,
			String companyName,
			WorkModality workModality,
			EmploymentType employmentType,
			String location,
			SeniorityLevel seniorityLevel,
			Integer minSalary,
			Integer maxSalary
	) {
		this.title = title;
		this.recruiterId = recruiterId;
		this.description = description;
		this.companyName = companyName;
		this.workModality = workModality;
		this.employmentType = employmentType;
		this.location = location;
		this.seniorityLevel = seniorityLevel;
		this.minSalary = minSalary;
		this.maxSalary = maxSalary;
	}

	@PrePersist
	void assignId() {
		if (id == null) {
			id = UUID.randomUUID();
		}
	}

	public UUID getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public UUID getRecruiterId() {
		return recruiterId;
	}

	public String getDescription() {
		return description;
	}

	public String getCompanyName() {
		return companyName;
	}

	public WorkModality getWorkModality() {
		return workModality;
	}

	public EmploymentType getEmploymentType() {
		return employmentType;
	}

	public String getLocation() {
		return location;
	}

	public SeniorityLevel getSeniorityLevel() {
		return seniorityLevel;
	}

	public Integer getMinSalary() {
		return minSalary;
	}

	public Integer getMaxSalary() {
		return maxSalary;
	}

	public List<JobRequirement> getRequirements() {
		return requirements;
	}

	public List<JobLanguageRequirement> getLanguageRequirements() {
		return languageRequirements;
	}

	public void addRequirement(JobRequirement requirement) {
		requirements.add(requirement);
		requirement.setJobVacancy(this);
	}

	public void addLanguageRequirement(JobLanguageRequirement requirement) {
		languageRequirements.add(requirement);
		requirement.setJobVacancy(this);
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public void setWorkModality(WorkModality workModality) {
		this.workModality = workModality;
	}

	public void setEmploymentType(EmploymentType employmentType) {
		this.employmentType = employmentType;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setSeniorityLevel(SeniorityLevel seniorityLevel) {
		this.seniorityLevel = seniorityLevel;
	}

	public void setMinSalary(Integer minSalary) {
		this.minSalary = minSalary;
	}

	public void setMaxSalary(Integer maxSalary) {
		this.maxSalary = maxSalary;
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

	public Set<Benefit> getBenefits() {
		return benefits;
	}

	public void setBenefits(Set<Benefit> benefits) {
		this.benefits.clear();
		if (benefits != null) {
			this.benefits.addAll(benefits);
		}
	}

	public void clearRequirements() {
		requirements.clear();
	}

	public void clearLanguageRequirements() {
		languageRequirements.clear();
	}
}
