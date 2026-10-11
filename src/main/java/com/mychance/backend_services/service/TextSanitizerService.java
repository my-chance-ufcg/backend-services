package com.mychance.backend_services.service;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class TextSanitizerService {

	private static final Pattern EMAIL_PATTERN = Pattern.compile(
			"[a-z0-9._%+-]+\\s*(?:@|\\(at\\)|\\[at\\]|\\bat\\b)\\s*[a-z0-9.-]+\\s*(?:\\.\\s*|\\(dot\\)|\\[dot\\]|\\bdot\\b)\\s*[a-z]{2,}",
			Pattern.CASE_INSENSITIVE
	);

	private static final Pattern URL_PATTERN = Pattern.compile(
			"(?:https?://|mailto:)\\S+"
					+ "|(?:www\\.)\\S+"
					+ "|\\b(?:linkedin|github|gitlab|bitbucket|instagram|medium|behance|notion)\\.com/\\S+"
					+ "|\\b(?:t\\.me|wa\\.me|bit\\.ly)/\\S+"
					+ "|\\b(?:[a-z0-9-]+\\.)+(?:com\\.br|com|io|dev|app|org|net|me|br)\\b(?:/\\S+)?",
			Pattern.CASE_INSENSITIVE
	);

	private static final Pattern ORGANIZATION_MARKER_PATTERN = Pattern.compile(
			"(?i:empresa|companhia|startup|organização|organizacao|instituição|instituicao)\\s+[\\p{Lu}][\\p{L}0-9&.'’-]{1,40}"
	);

	private static final Pattern ORGANIZATION_SUFFIX_PATTERN = Pattern.compile(
			"[\\p{Lu}][\\p{L}0-9&.'’-]{1,40}(?:\\s+[\\p{Lu}][\\p{L}0-9&.'’-]{1,40}){0,3}\\s+"
					+ "(?i:ltda\\.?|s\\.?\\s?a\\.?|s/a|inc\\.?|llc|gmbh|corp\\.?|eireli|epp)\\b"
	);

	public String sanitize(String text) {
		if (text == null || text.isBlank()) {
			return text;
		}
		String sanitized = EMAIL_PATTERN.matcher(text).replaceAll("[email removido]");
		sanitized = URL_PATTERN.matcher(sanitized).replaceAll("[link removido]");
		sanitized = ORGANIZATION_SUFFIX_PATTERN.matcher(sanitized).replaceAll("[empresa removida]");
		sanitized = ORGANIZATION_MARKER_PATTERN.matcher(sanitized).replaceAll("[empresa removida]");
		return sanitized.trim();
	}
}
