package com.example.checkword.word.application;

import java.util.List;
import java.util.Set;

/**
 * 한국어 명사 뒤에 붙는 조사를 제거해 반복 단어 비교 기준을 맞추는 정규화 도구입니다.
 */
final class KoreanParticleNormalizer {

	private static final int MIN_STEM_CODE_POINT_LENGTH = 2;

	private static final Set<String> PROTECTED_WORDS = Set.of(
		"을지로"
	);

	private static final Set<String> CONTEXT_REQUIRED_PARTICLES = Set.of(
		"이", "가", "의", "에", "로", "도", "만"
	);

	private static final List<String> PARTICLES = List.of(
		"으로부터", "에게서", "한테서",
		"이라든지", "라든지", "이라든가", "라든가", "이든지", "이든가",
		"으로", "에서", "에게", "한테", "부터", "까지", "밖에", "보다", "처럼",
		"같이", "만큼", "대로", "이나마", "이라도", "이라면", "이라서", "이든",
		"이랑", "이며", "이고", "라도", "라면", "라서", "든지", "든가",
		"께서", "이나", "든", "야", "랑", "께", "은", "는", "이", "가", "을", "를",
		"와", "과", "도", "만", "의", "에", "로"
	);

	private KoreanParticleNormalizer() {
	}

	/**
	 * 단어 끝에 붙은 가장 마지막 조사 후보 하나만 제거하고, 비교용 단어와 화면 표시용 단어를 함께 반환합니다.
	 */
	static NormalizedWord normalize(String word, boolean stripParticle) {
		String normalized = stripParticle ? stripOneParticle(word) : word;

		return new NormalizedWord(normalized, normalized);
	}

	/**
	 * 단어 끝에 붙은 조사 후보 하나를 찾습니다. 긴 조사를 먼저 검사해 `부터`, `까지` 같은 조사를 우선합니다.
	 */
	static ParticleMatch findEndingParticle(String word) {
		if (PROTECTED_WORDS.contains(word)) {
			return null;
		}

		for (String particle : PARTICLES) {
			if (!word.endsWith(particle)) {
				continue;
			}

			String stem = word.substring(0, word.length() - particle.length());

			if (canUseStem(stem)) {
				return new ParticleMatch(stem, particle, CONTEXT_REQUIRED_PARTICLES.contains(particle));
			}
		}

		return null;
	}

	/**
	 * 가장 긴 조사 후보부터 검사해 단어 끝에 붙은 조사 하나를 제거합니다.
	 */
	private static String stripOneParticle(String word) {
		ParticleMatch particleMatch = findEndingParticle(word);

		if (particleMatch == null) {
			return word;
		}

		return particleMatch.stem();
	}

	/**
	 * 조사 제거 뒤 남은 어간이 너무 짧거나 비어 있으면 잘못된 제거로 보고 원문을 유지합니다.
	 */
	private static boolean canUseStem(String stem) {
		return stem.codePointCount(0, stem.length()) >= MIN_STEM_CODE_POINT_LENGTH
			&& stem.codePoints().anyMatch(Character::isLetterOrDigit);
	}

	/**
	 * 중복 비교 기준 단어와 사용자에게 보여줄 단어를 함께 담습니다.
	 */
	record NormalizedWord(
		String normalizedWord,
		String displayWord
	) {
	}

	/**
	 * 단어 끝에서 발견한 조사 후보와 제거 후 남는 어간입니다.
	 */
	record ParticleMatch(
		String stem,
		String particle,
		boolean requiresContext
	) {
	}
}
