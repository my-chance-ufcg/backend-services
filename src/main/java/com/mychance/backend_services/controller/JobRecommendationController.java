package com.mychance.backend_services.controller;

import com.mychance.backend_services.dto.response.RecommendationResponse;
import com.mychance.backend_services.security.AuthenticatedUser;
import com.mychance.backend_services.service.JobVacancyService;
import com.mychance.backend_services.service.RecommendationService;
import com.mychance.backend_services.telemetry.ProductEvent;
import com.mychance.backend_services.telemetry.ProductTelemetry;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobRecommendationController {

	private final RecommendationService recommendationService;
	private final JobVacancyService jobVacancyService;
	private final ProductTelemetry productTelemetry;

	public JobRecommendationController(
			RecommendationService recommendationService,
			JobVacancyService jobVacancyService,
			ProductTelemetry productTelemetry
	) {
		this.recommendationService = recommendationService;
		this.jobVacancyService = jobVacancyService;
		this.productTelemetry = productTelemetry;
	}

	@GetMapping("/{id}/recommendations")
	public List<RecommendationResponse> getRecommendations(
			@AuthenticationPrincipal AuthenticatedUser user,
			@PathVariable UUID id
	) {
		jobVacancyService.getOwnedJob(user.id(), id);
		List<RecommendationResponse> recommendations = recommendationService.getRecommendations(id);
		productTelemetry.record(
				ProductEvent.MATCHING_GERADO,
				"vaga_id", id.toString(),
				"resultados", Integer.toString(recommendations.size())
		);
		return recommendations;
	}
}
