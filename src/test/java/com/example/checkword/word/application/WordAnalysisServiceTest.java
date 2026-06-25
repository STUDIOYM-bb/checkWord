package com.example.checkword.word.application;

import com.example.checkword.word.domain.RepeatedWord;
import com.example.checkword.word.domain.WordAnalysisResult;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WordAnalysisServiceTest {

	private final WordAnalysisService wordAnalysisService = new WordAnalysisService();

	@Test
	void analyzeFindsRepeatedWordsAcrossKoreanEnglishAndNumbers() {
		String text = "사과 바나나 사과, Apple apple APPLE 123 123 !@# 가 나";

		WordAnalysisResult result = wordAnalysisService.analyze(text);

		Map<String, RepeatedWord> repeatedWords = result.repeatedWords()
			.stream()
			.collect(Collectors.toMap(RepeatedWord::normalizedWord, repeatedWord -> repeatedWord));

		assertEquals(3, result.repeatedWordTypeCount());
		assertEquals(8, result.totalWordCount());
		assertEquals(2, repeatedWords.get("사과").count());
		assertEquals(3, repeatedWords.get("apple").count());
		assertEquals(2, repeatedWords.get("123").count());
		assertFalse(repeatedWords.containsKey("가"));
	}

	@Test
	void analyzeKeepsOriginalIndexesForHighlighting() {
		String text = "테스트-테스트 테스트";

		WordAnalysisResult result = wordAnalysisService.analyze(text);
		RepeatedWord repeatedWord = result.repeatedWords().get(0);

		assertEquals("테스트", repeatedWord.displayWord());
		assertEquals(3, repeatedWord.count());
		assertEquals(0, repeatedWord.occurrences().get(0).startIndex());
		assertEquals(3, repeatedWord.occurrences().get(0).endIndex());
		assertEquals(4, repeatedWord.occurrences().get(1).startIndex());
		assertEquals(7, repeatedWord.occurrences().get(1).endIndex());
		assertTrue(repeatedWord.occurrences().get(2).startIndex() > repeatedWord.occurrences().get(1).endIndex());
	}

	@Test
	void analyzeTreatsKoreanWordsWithParticlesAsTheSameWord() {
		String text = """
			사과와 바나나를 샀다. 사과는 달고, banana는 노랗다. Banana 123 123 테스트 테스트!
			사과 사과. 사과
			""";

		WordAnalysisResult result = wordAnalysisService.analyze(text);

		Map<String, RepeatedWord> repeatedWords = result.repeatedWords()
			.stream()
			.collect(Collectors.toMap(RepeatedWord::normalizedWord, repeatedWord -> repeatedWord));

		assertEquals(5, repeatedWords.get("사과").count());
		assertEquals("사과", repeatedWords.get("사과").displayWord());
		assertEquals(2, repeatedWords.get("banana").count());
		assertEquals(2, repeatedWords.get("123").count());
		assertEquals(2, repeatedWords.get("테스트").count());
	}

	@Test
	void analyzeDoesNotStripParticleTextFromInsideProtectedNouns() {
		String text = "을지로 을지로를 을지";

		WordAnalysisResult result = wordAnalysisService.analyze(text);

		Map<String, RepeatedWord> repeatedWords = result.repeatedWords()
			.stream()
			.collect(Collectors.toMap(RepeatedWord::normalizedWord, repeatedWord -> repeatedWord));

		assertEquals(1, result.repeatedWordTypeCount());
		assertEquals(2, repeatedWords.get("을지로").count());
		assertFalse(repeatedWords.containsKey("을지"));
	}

	@Test
	void analyzeStripsOnlyOneParticleFromTheEndOfEachWord() {
		String text = "골뱅이 골뱅이는 고양이";

		WordAnalysisResult result = wordAnalysisService.analyze(text);

		Map<String, RepeatedWord> repeatedWords = result.repeatedWords()
			.stream()
			.collect(Collectors.toMap(RepeatedWord::normalizedWord, repeatedWord -> repeatedWord));

		assertEquals(1, result.repeatedWordTypeCount());
		assertEquals(2, repeatedWords.get("골뱅이").count());
		assertEquals("골뱅이", repeatedWords.get("골뱅이").displayWord());
		assertFalse(repeatedWords.containsKey("골뱅"));
	}

	@Test
	void analyzeSupportsMultiSyllableParticlesAsOneEndingParticle() {
		String text = "학교 학교부터 학교까지";

		WordAnalysisResult result = wordAnalysisService.analyze(text);

		Map<String, RepeatedWord> repeatedWords = result.repeatedWords()
			.stream()
			.collect(Collectors.toMap(RepeatedWord::normalizedWord, repeatedWord -> repeatedWord));

		assertEquals(1, result.repeatedWordTypeCount());
		assertEquals(3, repeatedWords.get("학교").count());
	}
}
