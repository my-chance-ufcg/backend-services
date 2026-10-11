package com.mychance.backend_services.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

	private static final Logger log = LoggerFactory.getLogger(HealthController.class);

	private final JdbcTemplate jdbcTemplate;

	public HealthController(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@GetMapping("/health")
	public ResponseEntity<Map<String, String>> health() {
		try {
			jdbcTemplate.queryForObject("SELECT 1", Integer.class);
			return ResponseEntity.ok(Map.of(
					"status", "ok",
					"service", "backend-services",
					"database", "up"
			));
		} catch (DataAccessException exception) {
			log.warn("health check failed: database unavailable ({})", exception.getClass().getSimpleName());
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
					"status", "down",
					"service", "backend-services",
					"database", "down"
			));
		}
	}
}
