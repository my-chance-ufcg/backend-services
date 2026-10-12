package com.mychance.backend_services.telemetry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AuditTrail {

	static final String LOGGER_NAME = "com.mychance.audit";

	private static final Logger log = LoggerFactory.getLogger(LOGGER_NAME);

	public void record(String event, String... keyValues) {
		if (keyValues.length % 2 != 0) {
			throw new IllegalArgumentException("Audit attributes must be key/value pairs");
		}
		log.info("event={} {}", event, format(keyValues));
	}

	private String format(String... keyValues) {
		List<String> pairs = new ArrayList<>();
		for (int index = 0; index < keyValues.length; index += 2) {
			pairs.add(keyValues[index] + "=" + sanitize(keyValues[index + 1]));
		}
		return String.join(" ", pairs);
	}

	private String sanitize(String value) {
		if (value == null || value.isBlank()) {
			return "-";
		}
		return value.replaceAll("[\\s=]", "_");
	}
}
