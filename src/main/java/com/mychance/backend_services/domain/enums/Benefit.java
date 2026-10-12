package com.mychance.backend_services.domain.enums;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public enum Benefit {
	VALE_REFEICAO("Vale Refeição (VR)", "vale_refeicao"),
	VALE_ALIMENTACAO("Vale Alimentação (VA)", "vale_alimentacao"),
	VALE_TRANSPORTE("Vale Transporte (VT)", "vale_transporte"),
	PLANO_SAUDE("Plano de Saúde", "plano_saude"),
	PLANO_ODONTOLOGICO("Plano Odontológico", "plano_odontologico"),
	SEGURO_VIDA("Seguro de Vida", "seguro_vida"),
	AUXILIO_HOME_OFFICE("Auxílio Home Office", "auxilio_home_office"),
	GYMPASS("Gympass / TotalPass", "gympass"),
	BOLSA_ESTUDOS("Bolsa de Estudos / Cursos", "bolsa_estudos"),
	PLR("Participação nos Lucros (PLR / PPR)", "plr"),
	HORARIO_FLEXIVEL("Horário Flexível", "horario_flexivel"),
	DAY_OFF_ANIVERSARIO("Day Off no Aniversário", "day_off_aniversario"),
	PREVIDENCIA_PRIVADA("Previdência Privada", "previdencia_privada"),
	AUXILIO_CRECHE("Auxílio Creche", "auxilio_creche");

	private static final Map<String, Benefit> BY_KEY = new HashMap<>();

	static {
		for (Benefit benefit : values()) {
			register(benefit.key, benefit);
			for (String alias : benefit.aliases) {
				register(alias, benefit);
			}
		}
	}

	private final String key;
	private final String[] aliases;

	Benefit(String key, String... aliases) {
		this.key = key;
		this.aliases = aliases;
	}

	public String getKey() {
		return key;
	}

	public static Benefit fromKey(String value) {
		if (!isValidKey(value)) {
			throw new IllegalArgumentException("Benefício inválido: " + value);
		}
		return BY_KEY.get(normalize(value));
	}

	public static boolean isValidKey(String value) {
		return value != null && BY_KEY.containsKey(normalize(value));
	}

	public static Set<Benefit> parseAll(List<String> keys) {
		Set<Benefit> parsed = new LinkedHashSet<>();
		if (keys == null) {
			return parsed;
		}
		for (String key : keys) {
			parsed.add(fromKey(key));
		}
		return parsed;
	}

	private static void register(String value, Benefit benefit) {
		BY_KEY.put(normalize(value), benefit);
	}

	private static String normalize(String value) {
		String decomposed = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "");
		return decomposed.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
	}
}
