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

	private static final List<String> PARTICLES = List.of(
		"으로부터는", "으로부터도", "으로까지는", "으로까지도",
		"에게서는", "에게서도", "한테서는", "한테서도", "께서는",
		"에서부터", "에서까지", "에서보다", "으로부터", "으로까지",
		"에게서", "한테서", "에서는", "에서도", "에게는", "에게도", "한테는", "한테도",
		"까지는", "까지도", "부터는", "부터도", "밖에는", "밖에도", "보다는", "보다도",
		"처럼은", "처럼도", "같이는", "같이도", "만큼은", "만큼도", "대로는", "대로도",
		"이라도", "이라든지", "이라든가", "이라면", "이라서", "이랑은", "이랑도",
		"라든지", "라든가", "라도", "라면", "라서",
		"에서는", "에서", "에게", "한테", "께", "부터", "까지", "밖에", "보다", "처럼",
		"같이", "만큼", "대로", "뿐만", "뿐은", "뿐도", "뿐",
		"으로", "이나", "이나마", "이든", "이든지", "이든가", "이야", "이랑", "이며", "이고",
		"든지", "든가", "든", "야", "랑", "은", "는", "이", "가", "을", "를", "와", "과",
		"도", "만", "의", "에", "로"
	);

	private KoreanParticleNormalizer() {
	}

	/**
	 * 단어 끝에서 조사 후보를 반복적으로 제거하고, 비교용 단어와 화면 표시용 단어를 함께 반환합니다.
	 */
	static NormalizedWord normalize(String word) {
		String normalized = word;

		if (PROTECTED_WORDS.contains(normalized)) {
			return new NormalizedWord(normalized, normalized);
		}

		for (int index = 0; index < 3; index++) {
			String stripped = stripOneParticle(normalized);

			if (stripped.equals(normalized)) {
				break;
			}

			normalized = stripped;
		}

		return new NormalizedWord(normalized, normalized);
	}

	/**
	 * 가장 긴 조사 후보부터 검사해 단어 끝에 붙은 조사 하나를 제거합니다.
	 */
	private static String stripOneParticle(String word) {
		if (PROTECTED_WORDS.contains(word)) {
			return word;
		}

		for (String particle : PARTICLES) {
			if (!word.endsWith(particle)) {
				continue;
			}

			String stem = word.substring(0, word.length() - particle.length());

			if (canUseStem(stem)) {
				return stem;
			}
		}

		return word;
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
}
