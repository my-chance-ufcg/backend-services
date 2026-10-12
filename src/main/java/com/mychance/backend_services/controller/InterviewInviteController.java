package com.mychance.backend_services.controller;

import com.mychance.backend_services.domain.enums.UserRole;
import com.mychance.backend_services.dto.request.InviteCreateRequest;
import com.mychance.backend_services.dto.request.ScheduleInterviewRequest;
import com.mychance.backend_services.dto.response.InviteResponse;
import com.mychance.backend_services.security.AuthenticatedUser;
import com.mychance.backend_services.service.InterviewInviteService;
import com.mychance.backend_services.service.JobVacancyService;
import com.mychance.backend_services.telemetry.AuditTrail;
import com.mychance.backend_services.telemetry.ProductEvent;
import com.mychance.backend_services.telemetry.ProductTelemetry;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class InterviewInviteController {

	private final InterviewInviteService interviewInviteService;
	private final JobVacancyService jobVacancyService;
	private final ProductTelemetry productTelemetry;
	private final AuditTrail auditTrail;

	public InterviewInviteController(
			InterviewInviteService interviewInviteService,
			JobVacancyService jobVacancyService,
			ProductTelemetry productTelemetry,
			AuditTrail auditTrail
	) {
		this.interviewInviteService = interviewInviteService;
		this.jobVacancyService = jobVacancyService;
		this.productTelemetry = productTelemetry;
		this.auditTrail = auditTrail;
	}

	@PostMapping("/jobs/{jobId}/invites")
	@ResponseStatus(HttpStatus.CREATED)
	public InviteResponse sendInvite(
			@AuthenticationPrincipal AuthenticatedUser user,
			@PathVariable UUID jobId,
			@Valid @RequestBody InviteCreateRequest request
	) {
		jobVacancyService.getOwnedJob(user.id(), jobId);
		InviteResponse response = interviewInviteService.sendInvite(jobId, request);
		productTelemetry.record(
				ProductEvent.CONVITE_ENVIADO,
				"convite_id", response.conviteId().toString(),
				"vaga_id", response.vagaId().toString(),
				"candidato_id", response.candidatoId()
		);
		return response;
	}

	@PostMapping("/invites/{inviteId}/accept")
	public InviteResponse acceptInvite(
			@AuthenticationPrincipal AuthenticatedUser user,
			@PathVariable UUID inviteId
	) {
		InviteResponse response = interviewInviteService.acceptInvite(inviteId, user.id());
		productTelemetry.record(
				ProductEvent.ACEITE_CONVITE,
				"convite_id", response.conviteId().toString(),
				"vaga_id", response.vagaId().toString()
		);
		auditTrail.record(
				"revelacao_identidade",
				"actor_id", user.id().toString(),
				"convite_id", response.conviteId().toString(),
				"vaga_id", response.vagaId().toString(),
				"candidato_id", response.candidatoId()
		);
		return response;
	}

	@PostMapping("/invites/{inviteId}/reject")
	public InviteResponse rejectInvite(
			@AuthenticationPrincipal AuthenticatedUser user,
			@PathVariable UUID inviteId
	) {
		InviteResponse response = interviewInviteService.rejectInvite(inviteId, user.id());
		productTelemetry.record(
				ProductEvent.REJEICAO_CONVITE,
				"convite_id", response.conviteId().toString(),
				"vaga_id", response.vagaId().toString()
		);
		return response;
	}

	@PostMapping("/invites/{inviteId}/schedule")
	public InviteResponse scheduleInterview(
			@AuthenticationPrincipal AuthenticatedUser user,
			@PathVariable UUID inviteId,
			@Valid @RequestBody ScheduleInterviewRequest request
	) {
		return interviewInviteService.scheduleInterview(inviteId, user.id(), request);
	}

	@PostMapping("/invites/{inviteId}/schedule/confirm")
	public InviteResponse confirmSchedule(
			@AuthenticationPrincipal AuthenticatedUser user,
			@PathVariable UUID inviteId
	) {
		if (user.role() == UserRole.RECRUITER) {
			return interviewInviteService.confirmScheduleByRecruiter(inviteId, user.id());
		}
		return interviewInviteService.confirmScheduleByCandidate(inviteId, user.id());
	}

	@PostMapping("/invites/{inviteId}/schedule/counter-propose")
	public InviteResponse counterProposeSchedule(
			@AuthenticationPrincipal AuthenticatedUser user,
			@PathVariable UUID inviteId,
			@Valid @RequestBody ScheduleInterviewRequest request
	) {
		return interviewInviteService.counterProposeSchedule(inviteId, user.id(), request);
	}

	@GetMapping("/candidates/me/invites")
	public List<InviteResponse> listMyInvites(@AuthenticationPrincipal AuthenticatedUser user) {
		List<InviteResponse> invites = interviewInviteService.listInvitesForAccount(user.id());
		auditRevealedContacts(user, invites);
		return invites;
	}

	@GetMapping("/jobs/{jobId}/invites")
	public List<InviteResponse> listJobInvites(
			@AuthenticationPrincipal AuthenticatedUser user,
			@PathVariable UUID jobId
	) {
		jobVacancyService.getOwnedJob(user.id(), jobId);
		List<InviteResponse> invites = interviewInviteService.listInvitesForJob(jobId);
		auditRevealedContacts(user, invites);
		return invites;
	}

	private void auditRevealedContacts(AuthenticatedUser user, List<InviteResponse> invites) {
		for (InviteResponse invite : invites) {
			String dado = invite.candidatoEmail() != null
					? "contato_candidato"
					: invite.recruiterEmail() != null ? "contato_recrutador" : null;
			if (dado == null) {
				continue;
			}
			auditTrail.record(
					"acesso_dado_pessoal",
					"actor_id", user.id().toString(),
					"actor_role", user.role().name(),
					"convite_id", invite.conviteId().toString(),
					"vaga_id", invite.vagaId().toString(),
					"candidato_id", invite.candidatoId(),
					"dado", dado
			);
		}
	}
}
