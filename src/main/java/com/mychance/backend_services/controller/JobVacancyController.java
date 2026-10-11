package com.mychance.backend_services.controller;

import com.mychance.backend_services.dto.request.JobCreateRequest;
import com.mychance.backend_services.dto.response.JobCreateResponse;
import com.mychance.backend_services.dto.response.JobDetailResponse;
import com.mychance.backend_services.dto.response.JobSummaryResponse;
import com.mychance.backend_services.security.AuthenticatedUser;
import com.mychance.backend_services.service.JobVacancyService;
import com.mychance.backend_services.telemetry.ProductEvent;
import com.mychance.backend_services.telemetry.ProductTelemetry;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobVacancyController {

	private final JobVacancyService jobVacancyService;
	private final ProductTelemetry productTelemetry;

	public JobVacancyController(JobVacancyService jobVacancyService, ProductTelemetry productTelemetry) {
		this.jobVacancyService = jobVacancyService;
		this.productTelemetry = productTelemetry;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public JobCreateResponse createJob(
			@AuthenticationPrincipal AuthenticatedUser user,
			@Valid @RequestBody JobCreateRequest request
	) {
		JobCreateResponse response = jobVacancyService.createJob(user.id(), request);
		productTelemetry.record(
				ProductEvent.CADASTRO_VAGA,
				"vaga_id", response.vagaId().toString(),
				"recruiter_id", user.id().toString()
		);
		return response;
	}

	@GetMapping("/mine")
	public List<JobSummaryResponse> listMyJobs(@AuthenticationPrincipal AuthenticatedUser user) {
		return jobVacancyService.listRecruiterJobs(user.id());
	}

	@GetMapping("/{jobId}")
	public JobDetailResponse getJob(
			@AuthenticationPrincipal AuthenticatedUser user,
			@PathVariable UUID jobId
	) {
		return jobVacancyService.getOwnedJobDetail(user.id(), jobId);
	}

	@PutMapping("/{jobId}")
	public JobCreateResponse updateJob(
			@AuthenticationPrincipal AuthenticatedUser user,
			@PathVariable UUID jobId,
			@Valid @RequestBody JobCreateRequest request
	) {
		return jobVacancyService.updateJob(user.id(), jobId, request);
	}
}
