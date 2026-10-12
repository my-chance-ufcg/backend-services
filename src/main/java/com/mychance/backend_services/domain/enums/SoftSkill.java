package com.mychance.backend_services.domain.enums;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public enum SoftSkill {
	COMUNICACAO("Comunicação", "comunicacao"),
	ESCUTA_ATIVA("Escuta ativa", "escuta_ativa"),
	TRABALHO_EQUIPE("Trabalho em equipe", "trabalho_equipe"),
	LIDERANCA("Liderança", "lideranca"),
	PROATIVIDADE("Proatividade", "proatividade"),
	AUTONOMIA("Autonomia", "autonomia"),
	RESOLUCAO_PROBLEMAS("Resolução de problemas", "resolucao_problemas"),
	PENSAMENTO_CRITICO("Pensamento crítico", "pensamento_critico"),
	TOMADA_DECISAO("Tomada de decisão", "tomada_decisao"),
	ADAPTABILIDADE("Adaptabilidade", "adaptabilidade"),
	RESILIENCIA("Resiliência", "resiliencia"),
	GESTAO_TEMPO("Gestão de tempo", "gestao_tempo"),
	ORGANIZACAO("Organização", "organizacao"),
	INTELIGENCIA_EMOCIONAL("Inteligência emocional", "inteligencia_emocional"),
	EMPATIA("Empatia", "empatia"),
	NEGOCIACAO("Negociação", "negociacao"),
	CRIATIVIDADE("Criatividade", "criatividade"),
	FLEXIBILIDADE("Flexibilidade", "flexibilidade"),
	ORIENTACAO_RESULTADOS("Orientação a resultados", "orientacao_resultados"),
	APRENDIZADO_CONTINUO("Aprendizado contínuo", "aprendizado_continuo");

	public static final int MAX_SELECTION = 3;

	private static final Map<String, SoftSkill> BY_KEY = new HashMap<>();

	static {
		for (SoftSkill skill : values()) {
			register(skill.key, skill);
			for (String alias : skill.aliases) {
				register(alias, skill);
			}
		}
	}

	private final String key;
	private final String[] aliases;

	SoftSkill(String key, String... aliases) {
		this.key = key;
		this.aliases = aliases;
	}

	public String getKey() {
		return key;
	}

	public static SoftSkill fromKey(String value) {
		if (!isValidKey(value)) {
			throw new IllegalArgumentException("Soft skill inválida: " + value);
		}
		return BY_KEY.get(normalize(value));
	}

	public static boolean isValidKey(String value) {
		return value != null && BY_KEY.containsKey(normalize(value));
	}

	public static Set<SoftSkill> parseAll(List<String> keys) {
		Set<SoftSkill> parsed = new LinkedHashSet<>();
		if (keys == null) {
			return parsed;
		}
		for (String key : keys) {
			parsed.add(fromKey(key));
		}
		if (parsed.size() > MAX_SELECTION) {
			throw new IllegalArgumentException("Selecione no máximo 3 competências comportamentais");
		}
		return parsed;
	}

	private static void register(String value, SoftSkill skill) {
		BY_KEY.put(normalize(value), skill);
	}

	private static String normalize(String value) {
		String decomposed = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "");
		return decomposed.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
	}
}
