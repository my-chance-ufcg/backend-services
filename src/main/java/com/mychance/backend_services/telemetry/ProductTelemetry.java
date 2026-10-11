package com.mychance.backend_services.telemetry;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductTelemetry {

	static final String LOGGER_NAME = "com.mychance.product";

	private static final Logger log = LoggerFactory.getLogger(LOGGER_NAME);

	private final MeterRegistry meterRegistry;

	public ProductTelemetry(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	public void record(ProductEvent event, String... keyValues) {
		if (keyValues.length % 2 != 0) {
			throw new IllegalArgumentException("Product event attributes must be key/value pairs");
		}

		meterRegistry.counter("mychance.product.events", "event", event.key()).increment();
		log.info("event={} {}", event.key(), format(keyValues));
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
