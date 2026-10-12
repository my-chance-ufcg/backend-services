package com.mychance.backend_services.config;

import com.mychance.backend_services.domain.entity.Account;
import com.mychance.backend_services.domain.entity.JobLanguageRequirement;
import com.mychance.backend_services.domain.entity.JobRequirement;
import com.mychance.backend_services.domain.entity.JobVacancy;
import com.mychance.backend_services.domain.enums.EmploymentType;
import com.mychance.backend_services.domain.enums.LanguageLevel;
import com.mychance.backend_services.domain.enums.LanguageName;
import com.mychance.backend_services.domain.enums.SeniorityLevel;
import com.mychance.backend_services.domain.enums.SkillName;
import com.mychance.backend_services.domain.enums.UserRole;
import com.mychance.backend_services.domain.enums.WorkModality;
import com.mychance.backend_services.dto.request.ExperienceRequest;
import com.mychance.backend_services.dto.request.LanguageProficiencyRequest;
import com.mychance.backend_services.dto.request.ProfileCreateRequest;
import com.mychance.backend_services.repository.AccountRepository;
import com.mychance.backend_services.repository.JobVacancyRepository;
import com.mychance.backend_services.service.CandidateProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class DemoDataSeeder {

	private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

	public static final UUID RECRUITER_ALPHA_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	public static final UUID RECRUITER_BETA_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	public static final UUID RECRUITER_TEST_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
	public static final UUID ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000099");

	public static final UUID ANA_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1");
	public static final UUID DIEGO_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4");
	public static final UUID ELISA_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa5");

	public static final String DEMO_RECRUITER_PASSWORD = "recruiter123";
	public static final String DEMO_CANDIDATE_PASSWORD = "candidato123";
	public static final String DEMO_ADMIN_PASSWORD = "admin123";

	private final AccountRepository accountRepository;
	private final JobVacancyRepository jobVacancyRepository;
	private final CandidateProfileService candidateProfileService;
	private final PasswordEncoder passwordEncoder;

	public DemoDataSeeder(
			AccountRepository accountRepository,
			JobVacancyRepository jobVacancyRepository,
			CandidateProfileService candidateProfileService,
			PasswordEncoder passwordEncoder
	) {
		this.accountRepository = accountRepository;
		this.jobVacancyRepository = jobVacancyRepository;
		this.candidateProfileService = candidateProfileService;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public void seed() {
		seedAdmin();
		seedRecruiter(RECRUITER_ALPHA_ID, "Recrutador Demo", "recruiter@mychance.local");
		seedRecruiter(RECRUITER_BETA_ID, "Recrutadora Beta", "recruiter2@mychance.local");
		seedRecruiter(RECRUITER_TEST_ID, "Recrutador Teste R2", "recruiter.teste@demo.local");

		seedBackendJobIfMissing();
		seedFrontendJobIfMissing();

		seedCandidateProfile(
				ANA_ID,
				"Ana Silva",
				"ana.silva@demo.local",
				"(83) 99999-0001",
				profile(
						Map.of("python", 5, "sql", 5, "docker", 4, "postgresql", 4),
						List.of(exp("Desenvolvedor Backend", "pleno", 3, 2023, null, null, true)),
						"API REST em Python com PostgreSQL, testes automatizados e deploy em containers.",
						10000,
						"pleno"
				)
		);
		seedCandidateProfile(
				UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2"),
				"Bruno Costa",
				"bruno.costa@demo.local",
				"(83) 99999-0002",
				profile(
						Map.of("python", 1, "sql", 5, "docker", 2),
						List.of(exp("Desenvolvedor Backend", "junior", 6, 2024, null, null, true)),
						"Scripts de integração com foco em SQL e automações internas.",
						7000,
						"junior"
				)
		);
		seedCandidateProfile(
				UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3"),
				"Carla Mendes",
				"carla.mendes@demo.local",
				"(83) 99999-0003",
				profile(
						Map.of("python", 5, "sql", 5, "docker", 3),
						List.of(exp("Desenvolvedor Backend", "pleno", 1, 2022, null, null, true)),
						"Microsserviços Python com mensageria e observabilidade.",
						15000,
						"senior"
				)
		);
		seedCandidateProfile(
				DIEGO_ID,
				"Diego Santos",
				"diego.santos@demo.local",
				"(83) 99999-0004",
				profile(
						Map.of("python", 4, "sql", 4, "docker", 3, "fastapi", 4),
						List.of(exp("Desenvolvedor Backend", "junior", 1, 2024, null, null, true)),
						"Serviços FastAPI com autenticação JWT e integração com filas.",
						8500,
						"junior"
				)
		);
		seedCandidateProfile(
				ELISA_ID,
				"Elisa Ferreira",
				"elisa.ferreira@demo.local",
				"(83) 99999-0005",
				profile(
						Map.of("python", 4, "sql", 4, "postgresql", 3),
						List.of(exp("Desenvolvedor Backend", "junior", 8, 2023, null, null, true)),
						"ETL com Python e SQL para pipelines de dados operacionais.",
						9000,
						"pleno"
				)
		);
		seedCandidateProfile(
				UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa6"),
				"Felipe Rocha",
				"felipe.rocha@demo.local",
				"(83) 99999-0006",
				profile(
						Map.of("react", 5, "typescript", 4, "javascript", 5, "nodejs", 3),
						List.of(exp("Desenvolvedor Frontend", "pleno", 2, 2023, null, null, true)),
						"SPA em React com TypeScript, testes de componente e CI/CD.",
						11000,
						"pleno"
				)
		);

		log.info("Demo dataset ready. Recruiters: recruiter@mychance.local, recruiter2@mychance.local (senha: {})",
				DEMO_RECRUITER_PASSWORD);
		log.info("Demo candidates: *@demo.local (senha: {})", DEMO_CANDIDATE_PASSWORD);
		log.info("Demo admin: admin@mychance.local (senha: {})", DEMO_ADMIN_PASSWORD);
	}

	private ProfileCreateRequest profile(
			Map<String, Integer> skills,
			List<ExperienceRequest> experiences,
			String project,
			int salary,
			String ignoredSeniorityHint
	) {
		return new ProfileCreateRequest(
				skills,
				experiences,
				List.of(project),
				"graduacao_concluida",
				"pb",
				"ciencia_computacao",
				salary,
				List.of("remoto", "hibrido", "presencial"),
				List.of("clt", "bolsa_projeto", "pj"),
				List.of(
						new LanguageProficiencyRequest("portugues", "fluente"),
						new LanguageProficiencyRequest("ingles", "intermediario")
				),
				null,
				null,
				null
		);
	}

	private ExperienceRequest exp(
			String cargo,
			String senioridade,
			int inicioMes,
			int inicioAno,
			Integer fimMes,
			Integer fimAno,
			boolean atual
	) {
		return new ExperienceRequest(cargo, senioridade, inicioMes, inicioAno, fimMes, fimAno, atual);
	}

	private void seedAdmin() {
		if (accountRepository.existsById(ADMIN_ID) || accountRepository.existsByEmailIgnoreCase("admin@mychance.local")) {
			return;
		}
		accountRepository.save(Account.withId(
				ADMIN_ID,
				"Administrador Demo",
				"admin@mychance.local",
				passwordEncoder.encode(DEMO_ADMIN_PASSWORD),
				UserRole.ADMIN
		));
		log.info("Demo admin seeded: admin@mychance.local / {}", DEMO_ADMIN_PASSWORD);
	}

	private void seedRecruiter(UUID id, String name, String email) {
		if (accountRepository.existsById(id) || accountRepository.existsByEmailIgnoreCase(email)) {
			return;
		}
		accountRepository.save(Account.withId(
				id,
				name,
				email,
				passwordEncoder.encode(DEMO_RECRUITER_PASSWORD),
				UserRole.RECRUITER
		));
		log.info("Demo recruiter seeded: {} / {}", email, DEMO_RECRUITER_PASSWORD);
	}

	private void seedBackendJobIfMissing() {
		if (jobVacancyRepository.findByRecruiterIdOrderByTitleAsc(RECRUITER_ALPHA_ID).stream()
				.anyMatch(job -> "Desenvolvedor Backend Python".equals(job.getTitle()))) {
			return;
		}

		JobVacancy vacancy = new JobVacancy(
				"Desenvolvedor Backend Python",
				RECRUITER_ALPHA_ID,
				"Buscamos profissional para APIs Python, SQL e boas práticas de deploy.",
				"UFCG / Laboratório MyChance",
				WorkModality.HIBRIDO,
				EmploymentType.BOLSA_PROJETO,
				"pb",
				SeniorityLevel.PLENO,
				8000,
				12000
		);
		vacancy.addRequirement(new JobRequirement(SkillName.PYTHON, 5, true, 3));
		vacancy.addRequirement(new JobRequirement(SkillName.SQL, 4, true, 3));
		vacancy.addRequirement(new JobRequirement(SkillName.DOCKER, 3, false, 2));
		vacancy.addRequirement(new JobRequirement(SkillName.POSTGRESQL, 3, false, 2));
		vacancy.addLanguageRequirement(new JobLanguageRequirement(LanguageName.PORTUGUES, LanguageLevel.FLUENTE));
		vacancy.addLanguageRequirement(new JobLanguageRequirement(LanguageName.INGLES, LanguageLevel.INTERMEDIARIO));

		JobVacancy saved = jobVacancyRepository.save(vacancy);
		log.info("Demo backend job seeded: {} ({})", saved.getTitle(), saved.getId());
	}

	private void seedFrontendJobIfMissing() {
		if (jobVacancyRepository.findByRecruiterIdOrderByTitleAsc(RECRUITER_BETA_ID).stream()
				.anyMatch(job -> "Desenvolvedor Frontend React".equals(job.getTitle()))) {
			return;
		}

		JobVacancy vacancy = new JobVacancy(
				"Desenvolvedor Frontend React",
				RECRUITER_BETA_ID,
				"Vaga para construção de interfaces React/TypeScript com foco em acessibilidade.",
				"Empresa Parceira Demo",
				WorkModality.REMOTO,
				EmploymentType.CLT,
				null,
				SeniorityLevel.PLENO,
				9000,
				13000
		);
		vacancy.addRequirement(new JobRequirement(SkillName.REACT, 5, true, 3));
		vacancy.addRequirement(new JobRequirement(SkillName.TYPESCRIPT, 4, true, 2));
		vacancy.addRequirement(new JobRequirement(SkillName.JAVASCRIPT, 4, false, 3));
		vacancy.addRequirement(new JobRequirement(SkillName.GIT, 3, false, 2));
		vacancy.addLanguageRequirement(new JobLanguageRequirement(LanguageName.PORTUGUES, LanguageLevel.FLUENTE));

		JobVacancy saved = jobVacancyRepository.save(vacancy);
		log.info("Demo frontend job seeded: {} ({})", saved.getTitle(), saved.getId());
	}

	private void seedCandidateProfile(
			UUID accountId,
			String name,
			String email,
			String phone,
			ProfileCreateRequest profile
	) {
		if (accountRepository.existsByEmailIgnoreCase(email)) {
			return;
		}

		Account account = Account.withId(
				accountId,
				name,
				email,
				passwordEncoder.encode(DEMO_CANDIDATE_PASSWORD),
				UserRole.CANDIDATE,
				phone
		);
		accountRepository.save(account);
		candidateProfileService.createProfile(accountId, profile);
		log.info("Demo candidate seeded: {} / {}", email, DEMO_CANDIDATE_PASSWORD);
	}
}
