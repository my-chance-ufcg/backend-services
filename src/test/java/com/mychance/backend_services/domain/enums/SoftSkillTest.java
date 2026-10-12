package com.mychance.backend_services.domain.enums;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoftSkillTest {

	@Test
	void acceptsTheLabelsShownOnTheForms() {
		assertThat(SoftSkill.fromKey("Escuta ativa").getKey()).isEqualTo("Escuta ativa");
		assertThat(SoftSkill.fromKey("Aprendizado contínuo").getKey()).isEqualTo("Aprendizado contínuo");
		assertThat(SoftSkill.fromKey("comunicacao").getKey()).isEqualTo("Comunicação");
	}

	@Test
	void rejectsMoreThanThreeSkills() {
		assertThatThrownBy(() -> SoftSkill.parseAll(List.of(
				"Comunicação",
				"Empatia",
				"Autonomia",
				"Liderança"
		)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("3");
	}
}
