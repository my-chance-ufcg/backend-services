package com.mychance.backend_services.telemetry;

public enum ProductEvent {
	CADASTRO_CANDIDATO("cadastro_candidato"),
	CADASTRO_VAGA("cadastro_vaga"),
	MATCHING_GERADO("matching_gerado"),
	CONVITE_ENVIADO("convite_enviado"),
	ACEITE_CONVITE("aceite_convite"),
	REJEICAO_CONVITE("rejeicao_convite");

	private final String key;

	ProductEvent(String key) {
		this.key = key;
	}

	public String key() {
		return key;
	}
}
