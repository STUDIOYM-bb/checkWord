package com.example.checkword.word.domain;

import java.util.List;

/**
 * 2회 이상 반복된 단어와 해당 단어의 모든 등장 위치를 표현하는 도메인 객체입니다.
 */
public record RepeatedWord(
	String normalizedWord,
	String displayWord,
	int count,
	String color,
	List<WordOccurrence> occurrences
) {
	/**
	 * 반복 단어 결과가 화면 표시와 통계 계산에 사용할 수 있는 상태인지 검증합니다.
	 */
	public RepeatedWord {
		if (normalizedWord == null || normalizedWord.isBlank()) {
			throw new IllegalArgumentException("normalizedWord must not be blank.");
		}
		if (displayWord == null || displayWord.isBlank()) {
			throw new IllegalArgumentException("displayWord must not be blank.");
		}
		if (count < 2) {
			throw new IllegalArgumentException("Repeated word count must be at least 2.");
		}
		if (color == null || color.isBlank()) {
			throw new IllegalArgumentException("color must not be blank.");
		}
		occurrences = List.copyOf(occurrences);
	}
}
