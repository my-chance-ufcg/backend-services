package com.mychance.backend_services.domain.enums;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum SalaryRange {
	ATE_2K("ate_2k", 1000, 2000),
	DE_2K_A_4K("2k_a_4k", 2001, 4000),
	DE_4K_A_7K("4k_a_7k", 4001, 7000),
	DE_7K_A_10K("7k_a_10k", 7001, 10000),
	DE_10K_A_15K("10k_a_15k", 10001, 15000),
	ACIMA_15K("acima_15k", 15001, null),
	A_COMBINAR("a_combinar", null, null);

	private static final Map<String, SalaryRange> BY_KEY = Arrays.stream(values())
			.collect(Collectors.toMap(item -> item.key, Function.identity()));

	private final String key;
	private final Integer minimum;
	private final Integer maximum;

	SalaryRange(String key, Integer minimum, Integer maximum) {
		this.key = key;
		this.minimum = minimum;
		this.maximum = maximum;
	}

	public String getKey() {
		return key;
	}

	public Integer getMinimum() {
		return minimum;
	}

	public Integer getMaximum() {
		return maximum;
	}

	public static SalaryRange fromKey(String value) {
		if (!isValidKey(value)) {
			throw new IllegalArgumentException("Faixa salarial inválida: " + value);
		}
		return BY_KEY.get(value.trim().toLowerCase(Locale.ROOT));
	}

	public static boolean isValidKey(String value) {
		return value != null && BY_KEY.containsKey(value.trim().toLowerCase(Locale.ROOT));
	}
}
