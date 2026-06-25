package com.example.checkword.word.dto;

/**
 * 프론트엔드가 원문에서 단어를 색칠할 때 사용하는 단어 등장 위치 응답입니다.
 */
public record WordOccurrenceResponse(
	int startIndex,
	int endIndex,
	String originalText
) {
}
