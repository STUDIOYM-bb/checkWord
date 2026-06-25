package com.example.checkword.word.dto;

import com.example.checkword.word.domain.WordAnalysisResult;

import java.util.List;

/**
 * 반복 단어 분석 API의 최종 응답 데이터입니다.
 */
public record WordAnalysisResponse(
	String text,
	int totalWordCount,
	int repeatedWordTypeCount,
	List<RepeatedWordResponse> repeatedWords
) {
	/**
	 * 도메인 분석 결과를 API 응답 구조로 변환합니다.
	 */
	public static WordAnalysisResponse from(WordAnalysisResult result) {
		return new WordAnalysisResponse(
			result.text(),
			result.totalWordCount(),
			result.repeatedWordTypeCount(),
			result.repeatedWords()
				.stream()
				.map(repeatedWord -> new RepeatedWordResponse(
					repeatedWord.normalizedWord(),
					repeatedWord.displayWord(),
					repeatedWord.count(),
					repeatedWord.color(),
					repeatedWord.occurrences()
						.stream()
						.map(occurrence -> new WordOccurrenceResponse(
							occurrence.startIndex(),
							occurrence.endIndex(),
							occurrence.originalText()
						))
						.toList()
				))
				.toList()
		);
	}
}
