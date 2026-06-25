package com.example.checkword.word.domain;

import java.util.List;

/**
 * 입력 글 분석이 끝난 뒤 화면과 API 응답에 필요한 전체 결과를 담는 도메인 객체입니다.
 */
public record WordAnalysisResult(
	String text,
	int totalWordCount,
	int repeatedWordTypeCount,
	List<RepeatedWord> repeatedWords
) {
	/**
	 * 분석 결과 목록을 불변 컬렉션으로 고정해 응답 생성 중 변경되지 않도록 합니다.
	 */
	public WordAnalysisResult {
		if (text == null) {
			throw new IllegalArgumentException("text must not be null.");
		}
		if (totalWordCount < 0) {
			throw new IllegalArgumentException("totalWordCount must not be negative.");
		}
		if (repeatedWordTypeCount < 0) {
			throw new IllegalArgumentException("repeatedWordTypeCount must not be negative.");
		}
		repeatedWords = List.copyOf(repeatedWords);
	}
}
