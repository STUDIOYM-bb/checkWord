package com.example.checkword.word.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 반복 단어 분석 API가 클라이언트에서 전달받는 요청 데이터입니다.
 */
public record WordAnalysisRequest(
	@NotNull(message = "분석할 글은 null일 수 없습니다.")
	@Size(max = 100_000, message = "분석할 글은 최대 100,000자까지 입력할 수 있습니다.")
	String text
) {
}
