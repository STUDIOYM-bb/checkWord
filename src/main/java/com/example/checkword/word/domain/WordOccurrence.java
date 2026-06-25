package com.example.checkword.word.domain;

/**
 * 원문 안에서 발견된 단어의 한 번의 등장 위치를 표현하는 도메인 객체입니다.
 */
public record WordOccurrence(
	int startIndex,
	int endIndex,
	String originalText
) {
	/**
	 * 단어 위치와 원문 조각을 검증한 뒤 불변 객체로 저장합니다.
	 */
	public WordOccurrence {
		if (startIndex < 0) {
			throw new IllegalArgumentException("startIndex must not be negative.");
		}
		if (endIndex < startIndex) {
			throw new IllegalArgumentException("endIndex must be greater than or equal to startIndex.");
		}
		if (originalText == null || originalText.isBlank()) {
			throw new IllegalArgumentException("originalText must not be blank.");
		}
	}
}
