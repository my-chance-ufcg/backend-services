package com.mychance.backend_services.telemetry;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

class AuditTrailTest {

	private final AuditTrail auditTrail = new AuditTrail();
	private final Logger logger = (Logger) LoggerFactory.getLogger(AuditTrail.LOGGER_NAME);
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
	void recordsIdentityRevealWithoutContactData() {
		auditTrail.record(
				"revelacao_identidade",
				"actor_id", "6f1c0c2e-1b2a-4d3e-9a8b-0c1d2e3f4a5b",
				"convite_id", "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
				"candidato_id", "CAND-001"
		);

		assertThat(appender.list).hasSize(1);
		String message = appender.list.get(0).getFormattedMessage();
		assertThat(message).contains("event=revelacao_identidade");
		assertThat(message).contains("candidato_id=CAND-001");
		assertThat(message).doesNotContain("@");
	}
}
