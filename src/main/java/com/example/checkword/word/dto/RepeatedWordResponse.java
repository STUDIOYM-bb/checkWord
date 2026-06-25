package com.example.checkword.word.dto;

import java.util.List;

/**
 * 반복 단어 하나의 통계와 하이라이트 정보를 클라이언트에 전달하는 응답입니다.
 */
public record RepeatedWordResponse(
	String normalizedWord,
	String displayWord,
	int count,
	String color,
	List<WordOccurrenceResponse> occurrences
) {
}
