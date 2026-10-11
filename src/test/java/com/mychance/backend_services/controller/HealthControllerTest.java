package com.mychance.backend_services.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthControllerTest {

	@Mock
	private JdbcTemplate jdbcTemplate;

	@InjectMocks
	private HealthController healthController;

	@Test
	void reportsUpWhenDatabaseAnswers() {
		when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);

		ResponseEntity<Map<String, String>> response = healthController.health();

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody())
				.containsEntry("status", "ok")
				.containsEntry("service", "backend-services")
				.containsEntry("database", "up");
	}

	@Test
	void reportsDownWithoutLeakingTheDatabaseError() {
		when(jdbcTemplate.queryForObject("SELECT 1", Integer.class))
				.thenThrow(new DataRetrievalFailureException("jdbc:postgresql://db/mychance user=secret"));

		ResponseEntity<Map<String, String>> response = healthController.health();

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
		assertThat(response.getBody())
				.containsEntry("status", "down")
				.containsEntry("database", "down");
		assertThat(response.getBody().toString()).doesNotContain("secret").doesNotContain("postgresql");
	}
}
