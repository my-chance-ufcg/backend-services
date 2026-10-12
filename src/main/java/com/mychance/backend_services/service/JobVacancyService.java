package com.mychance.backend_services.service;

import com.mychance.backend_services.domain.entity.JobLanguageRequirement;
import com.mychance.backend_services.domain.entity.JobRequirement;
import com.mychance.backend_services.domain.entity.JobVacancy;
import com.mychance.backend_services.domain.enums.Benefit;
import com.mychance.backend_services.domain.enums.BrazilianState;
import com.mychance.backend_services.domain.enums.EmploymentType;
import com.mychance.backend_services.domain.enums.LanguageLevel;
import com.mychance.backend_services.domain.enums.LanguageName;
import com.mychance.backend_services.domain.enums.SalaryRange;
import com.mychance.backend_services.domain.enums.SeniorityLevel;
import com.mychance.backend_services.domain.enums.SkillName;
import com.mychance.backend_services.domain.enums.SoftSkill;
import com.mychance.backend_services.domain.enums.WorkModality;
import com.mychance.backend_services.dto.request.JobCreateRequest;
import com.mychance.backend_services.dto.request.JobLanguageRequirementRequest;
import com.mychance.backend_services.dto.request.JobRequirementRequest;
import com.mychance.backend_services.dto.response.JobCreateResponse;
import com.mychance.backend_services.dto.response.JobDetailResponse;
import com.mychance.backend_services.dto.response.JobLanguageRequirementDetailResponse;
import com.mychance.backend_services.dto.response.JobRequirementDetailResponse;
import com.mychance.backend_services.dto.response.JobSummaryResponse;
import com.mychance.backend_services.exception.InvalidSkillException;
import com.mychance.backend_services.exception.JobNotFoundException;
import com.mychance.backend_services.exception.ResourceAccessDeniedException;
import com.mychance.backend_services.repository.JobVacancyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class JobVacancyService {

	private final JobVacancyRepository jobVacancyRepository;
	private final TextSanitizerService textSanitizerService;

	public JobVacancyService(
			JobVacancyRepository jobVacancyRepository,
			TextSanitizerService textSanitizerService
	) {
		this.jobVacancyRepository = jobVacancyRepository;
		this.textSanitizerService = textSanitizerService;
	}

	@Transactional
	public JobCreateResponse createJob(UUID recruiterId, JobCreateRequest request) {
		SalaryResolution salary = resolveSalary(request);
		WorkModality modality = parseModality(request.modalidade());
		String location = normalizeLocation(request.local(), modality);
		JobVacancy jobVacancy = new JobVacancy(
				request.titulo(),
				recruiterId,
				sanitizeDescription(request.descricao()),
				request.empresaInstituicao().trim(),
				modality,
				parseEmploymentType(request.tipoVinculo()),
				location,
				parseSeniority(request.senioridade()),
				salary.minimum(),
				salary.maximum()
		);
		applyAdvancedFilters(request, salary.range(), jobVacancy);
		mapRequirements(request.requisitos(), jobVacancy);
		mapLanguages(request.idiomas(), jobVacancy);

		JobVacancy saved = jobVacancyRepository.save(jobVacancy);
		return new JobCreateResponse(saved.getId(), "Vaga criada com sucesso");
	}

	@Transactional(readOnly = true)
	public JobVacancy getJob(UUID jobId) {
		JobVacancy jobVacancy = jobVacancyRepository.findByIdWithRequirements(jobId)
				.orElseThrow(() -> new JobNotFoundException(jobId));
		jobVacancy.getLanguageRequirements().size();
		return jobVacancy;
	}

	@Transactional(readOnly = true)
	public JobVacancy getOwnedJob(UUID recruiterId, UUID jobId) {
		JobVacancy jobVacancy = getJob(jobId);
		if (!jobVacancy.getRecruiterId().equals(recruiterId)) {
			throw new ResourceAccessDeniedException("Recruiter does not own this job");
		}
		return jobVacancy;
	}

	@Transactional(readOnly = true)
	public List<JobSummaryResponse> listRecruiterJobs(UUID recruiterId) {
		return jobVacancyRepository.findByRecruiterIdOrderByTitleAsc(recruiterId).stream()
				.map(job -> new JobSummaryResponse(job.getId(), job.getTitle()))
				.toList();
	}

	@Transactional(readOnly = true)
	public JobDetailResponse getOwnedJobDetail(UUID recruiterId, UUID jobId) {
		JobVacancy jobVacancy = getOwnedJob(recruiterId, jobId);
		return toDetailResponse(jobVacancy);
	}

	@Transactional
	public JobCreateResponse updateJob(UUID recruiterId, UUID jobId, JobCreateRequest request) {
		SalaryResolution salary = resolveSalary(request);
		JobVacancy jobVacancy = getOwnedJob(recruiterId, jobId);
		WorkModality modality = parseModality(request.modalidade());
		String location = normalizeLocation(request.local(), modality);
		jobVacancy.setTitle(request.titulo());
		jobVacancy.setDescription(sanitizeDescription(request.descricao()));
		jobVacancy.setCompanyName(request.empresaInstituicao().trim());
		jobVacancy.setWorkModality(modality);
		jobVacancy.setEmploymentType(parseEmploymentType(request.tipoVinculo()));
		jobVacancy.setLocation(location);
		jobVacancy.setSeniorityLevel(parseSeniority(request.senioridade()));
		jobVacancy.setMinSalary(salary.minimum());
		jobVacancy.setMaxSalary(salary.maximum());
		applyAdvancedFilters(request, salary.range(), jobVacancy);
		jobVacancy.clearRequirements();
		jobVacancy.clearLanguageRequirements();
		mapRequirements(request.requisitos(), jobVacancy);
		mapLanguages(request.idiomas(), jobVacancy);

		JobVacancy saved = jobVacancyRepository.save(jobVacancy);
		return new JobCreateResponse(saved.getId(), "Vaga atualizada com sucesso");
	}

	private void mapRequirements(List<JobRequirementRequest> requisitos, JobVacancy jobVacancy) {
		for (JobRequirementRequest requirement : requisitos) {
			if (!SkillName.isValidKey(requirement.competencia())) {
				throw new InvalidSkillException(requirement.competencia());
			}
			jobVacancy.addRequirement(new JobRequirement(
					SkillName.fromKey(requirement.competencia()),
					requirement.peso(),
					requirement.obrigatoria(),
					requirement.nivelMin()
			));
		}
	}

	private void mapLanguages(List<JobLanguageRequirementRequest> idiomas, JobVacancy jobVacancy) {
		for (JobLanguageRequirementRequest idioma : idiomas) {
			if (!LanguageName.isValidKey(idioma.idioma())) {
				throw new IllegalArgumentException("Idioma inválido: " + idioma.idioma());
			}
			if (!LanguageLevel.isValidKey(idioma.nivelMin())) {
				throw new IllegalArgumentException("Nível de idioma inválido: " + idioma.nivelMin());
			}
			jobVacancy.addLanguageRequirement(new JobLanguageRequirement(
					LanguageName.fromKey(idioma.idioma()),
					LanguageLevel.fromKey(idioma.nivelMin())
			));
		}
	}

	private WorkModality parseModality(String value) {
		if (!WorkModality.isValidKey(value)) {
			throw new IllegalArgumentException("Modalidade inválida: " + value);
		}
		return WorkModality.fromKey(value);
	}

	private EmploymentType parseEmploymentType(String value) {
		if (!EmploymentType.isValidKey(value)) {
			throw new IllegalArgumentException("Tipo de vínculo inválido: " + value);
		}
		return EmploymentType.fromKey(value);
	}

	private SeniorityLevel parseSeniority(String value) {
		if (!SeniorityLevel.isValidKey(value)) {
			throw new IllegalArgumentException("Senioridade inválida: " + value);
		}
		return SeniorityLevel.fromKey(value);
	}

	private void applyAdvancedFilters(JobCreateRequest request, SalaryRange range, JobVacancy jobVacancy) {
		jobVacancy.setSalaryRange(range);
		jobVacancy.setSoftSkills(SoftSkill.parseAll(request.softSkills()));
		jobVacancy.setBenefits(Benefit.parseAll(request.beneficios()));
	}

	private SalaryResolution resolveSalary(JobCreateRequest request) {
		if (request.salarioMaximo() != null) {
			validateSalaryRange(request.salarioMinimo(), request.salarioMaximo());
			SalaryRange range = blankToNull(request.faixaSalarial()) == null
					? null
					: SalaryRange.fromKey(request.faixaSalarial());
			return new SalaryResolution(request.salarioMinimo(), request.salarioMaximo(), range);
		}
		if (blankToNull(request.faixaSalarial()) != null) {
			SalaryRange range = SalaryRange.fromKey(request.faixaSalarial());
			validateSalaryRange(range.getMinimum(), range.getMaximum());
			return new SalaryResolution(range.getMinimum(), range.getMaximum(), range);
		}
		throw new IllegalArgumentException("Informe o salário máximo ou a faixa salarial.");
	}

	private void validateSalaryRange(Integer minSalary, Integer maxSalary) {
		if (minSalary != null && maxSalary != null && minSalary > maxSalary) {
			throw new IllegalArgumentException("O salário mínimo não pode ser maior que o máximo.");
		}
	}

	private String normalizeLocation(String value, WorkModality modality) {
		String location = blankToNull(value);
		if (modality == WorkModality.PRESENCIAL) {
			if (location == null || !BrazilianState.isValidKey(location)) {
				throw new IllegalArgumentException("Informe o estado (UF) para vagas presenciais.");
			}
			return location.trim().toLowerCase();
		}
		if (location != null && !BrazilianState.isValidKey(location)) {
			throw new IllegalArgumentException("Estado inválido: " + location);
		}
		return location == null ? null : location.trim().toLowerCase();
	}

	private String blankToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}

	private JobDetailResponse toDetailResponse(JobVacancy jobVacancy) {
		List<JobRequirementDetailResponse> requisitos = jobVacancy.getRequirements().stream()
				.map(req -> new JobRequirementDetailResponse(
						req.getSkillName().getKey(),
						req.getWeight(),
						req.isMandatory(),
						req.getMinLevel()
				))
				.toList();

		List<JobLanguageRequirementDetailResponse> idiomas = jobVacancy.getLanguageRequirements().stream()
				.map(req -> new JobLanguageRequirementDetailResponse(
						req.getLanguageName().getKey(),
						req.getMinLevel().getKey()
				))
				.toList();

		return new JobDetailResponse(
				jobVacancy.getId(),
				jobVacancy.getTitle(),
				jobVacancy.getDescription(),
				jobVacancy.getCompanyName(),
				jobVacancy.getWorkModality().getKey(),
				jobVacancy.getEmploymentType().getKey(),
				jobVacancy.getLocation(),
				jobVacancy.getSeniorityLevel().getKey(),
				jobVacancy.getMinSalary(),
				jobVacancy.getMaxSalary(),
				requisitos,
				idiomas,
				jobVacancy.getSalaryRange() != null ? jobVacancy.getSalaryRange().getKey() : null,
				jobVacancy.getSoftSkills().stream().map(SoftSkill::getKey).sorted().toList(),
				jobVacancy.getBenefits().stream().map(Benefit::getKey).sorted().toList()
		);
	}

	private String sanitizeDescription(String value) {
		if (value == null || value.isBlank()) {
			return value;
		}
		String sanitized = textSanitizerService.sanitize(value.trim());
		if (sanitized == null || sanitized.isBlank()) {
			return "[conteúdo removido]";
		}
		return sanitized;
	}

	private record SalaryResolution(Integer minimum, Integer maximum, SalaryRange range) {
	}
}
