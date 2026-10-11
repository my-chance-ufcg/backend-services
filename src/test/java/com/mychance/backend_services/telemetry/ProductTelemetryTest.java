package com.mychance.backend_services.telemetry;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

class ProductTelemetryTest {

	private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
	private final ProductTelemetry telemetry = new ProductTelemetry(meterRegistry);
	private final Logger logger = (Logger) LoggerFactory.getLogger(ProductTelemetry.LOGGER_NAME);
	private ListAppender<ILoggingEvent> appender;

	@BeforeEach
	void attachLogAppender() {
		appender = new ListAppender<>();
		appender.start();
		logger.setLevel(Level.INFO);
		logger.addAppender(appender);
	}

	@AfterEach
	void detachLogAppender() {
		logger.detachAppender(appender);
	}

	@Test
	void recordsCandidateSignupWithoutPersonalData() {
		telemetry.record(ProductEvent.CADASTRO_CANDIDATO, "account_id", "6f1c0c2e-1b2a-4d3e-9a8b-0c1d2e3f4a5b");

		assertThat(meterRegistry.get("mychance.product.events")
				.tag("event", "cadastro_candidato")
				.counter()
				.count()).isEqualTo(1);
		assertThat(appender.list).singleElement().satisfies(event -> {
			assertThat(event.getFormattedMessage())
					.isEqualTo("event=cadastro_candidato account_id=6f1c0c2e-1b2a-4d3e-9a8b-0c1d2e3f4a5b");
			assertThat(event.getFormattedMessage()).doesNotContain("@");
		});
	}

	@Test
	void keepsEachUsageEventOnItsOwnCounter() {
		telemetry.record(ProductEvent.CADASTRO_VAGA, "vaga_id", "vaga-1", "recruiter_id", "rec-1");
		telemetry.record(ProductEvent.MATCHING_GERADO, "vaga_id", "vaga-1", "resultados", "3");
		telemetry.record(ProductEvent.CONVITE_ENVIADO, "convite_id", "conv-1", "vaga_id", "vaga-1", "candidato_id", "usr_abc12345");
		telemetry.record(ProductEvent.ACEITE_CONVITE, "convite_id", "conv-1", "vaga_id", "vaga-1");
		telemetry.record(ProductEvent.REJEICAO_CONVITE, "convite_id", "conv-2", "vaga_id", "vaga-1");

		assertThat(count("cadastro_vaga")).isEqualTo(1);
		assertThat(count("matching_gerado")).isEqualTo(1);
		assertThat(count("convite_enviado")).isEqualTo(1);
		assertThat(count("aceite_convite")).isEqualTo(1);
		assertThat(count("rejeicao_convite")).isEqualTo(1);
		assertThat(appender.list).extracting(ILoggingEvent::getFormattedMessage).contains(
				"event=convite_enviado convite_id=conv-1 vaga_id=vaga-1 candidato_id=usr_abc12345"
		);
	}

	private double count(String event) {
		return meterRegistry.get("mychance.product.events").tag("event", event).counter().count();
	}
}
