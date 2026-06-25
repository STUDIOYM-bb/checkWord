package com.example.checkword.word.application;

import com.example.checkword.word.domain.RepeatedWord;
import com.example.checkword.word.domain.WordAnalysisResult;
import com.example.checkword.word.domain.WordOccurrence;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 입력된 글에서 반복 단어를 찾고, 하이라이트에 필요한 색상과 위치 정보를 계산하는 서비스입니다.
 */
@Service
public class WordAnalysisService {

	private static final int MIN_WORD_CODE_POINT_LENGTH = 2;

	private static final String[] HIGHLIGHT_COLORS = {
		"#F97316",
		"#2563EB",
		"#16A34A",
		"#DC2626",
		"#9333EA",
		"#0D9488",
		"#DB2777",
		"#CA8A04",
		"#4F46E5",
		"#059669"
	};

	/**
	 * 공백과 특수문자를 구분자로 삼아 단어를 추출하고, 2회 이상 반복된 단어만 반환합니다.
	 */
	public WordAnalysisResult analyze(String text) {
		String safeText = text == null ? "" : text;
		List<RawParsedWord> rawWords = parseWords(safeText);
		List<ParsedWord> parsedWords = normalizeWords(rawWords);
		Map<String, List<ParsedWord>> groupedWords = groupByNormalizedWord(parsedWords);
		List<RepeatedWord> repeatedWords = createRepeatedWords(groupedWords);

		return new WordAnalysisResult(
			safeText,
			rawWords.size(),
			repeatedWords.size(),
			repeatedWords
		);
	}

	/**
	 * 유니코드 문자와 숫자로 이어진 구간만 단어로 보고, 한 글자 단어는 제외합니다.
	 */
	private List<RawParsedWord> parseWords(String text) {
		List<RawParsedWord> words = new ArrayList<>();
		int wordStart = -1;

		for (int index = 0; index < text.length(); ) {
			int codePoint = text.codePointAt(index);
			boolean isWordCharacter = Character.isLetterOrDigit(codePoint);

			if (isWordCharacter && wordStart == -1) {
				wordStart = index;
			}

			if (!isWordCharacter && wordStart != -1) {
				addWordIfValid(text, wordStart, index, words);
				wordStart = -1;
			}

			index += Character.charCount(codePoint);
		}

		if (wordStart != -1) {
			addWordIfValid(text, wordStart, text.length(), words);
		}

		return words;
	}

	/**
	 * 원문 구간의 글자 수가 2글자 이상이면 분석 대상 단어로 추가합니다.
	 */
	private void addWordIfValid(String text, int startIndex, int endIndex, List<RawParsedWord> words) {
		String originalWord = text.substring(startIndex, endIndex);
		int codePointLength = originalWord.codePointCount(0, originalWord.length());

		if (codePointLength >= MIN_WORD_CODE_POINT_LENGTH) {
			words.add(new RawParsedWord(
				originalWord,
				new WordOccurrence(startIndex, endIndex, originalWord)
			));
		}
	}

	/**
	 * 전체 단어 목록의 관계를 보고 한국어 조사 제거 여부를 결정한 뒤 비교용 단어를 만듭니다.
	 */
	private List<ParsedWord> normalizeWords(List<RawParsedWord> rawWords) {
		Set<String> rawNormalizedWords = new HashSet<>();
		Map<String, Set<String>> stemToSurfaceWords = new HashMap<>();

		for (RawParsedWord rawWord : rawWords) {
			String normalizedWord = normalizeForComparison(rawWord.originalWord());
			KoreanParticleNormalizer.ParticleMatch particleMatch = KoreanParticleNormalizer.findEndingParticle(normalizedWord);

			rawNormalizedWords.add(normalizedWord);

			if (particleMatch != null) {
				stemToSurfaceWords.computeIfAbsent(particleMatch.stem(), ignored -> new HashSet<>())
					.add(normalizedWord);
			}
		}

		List<ParsedWord> parsedWords = new ArrayList<>();

		for (RawParsedWord rawWord : rawWords) {
			String normalizedWord = normalizeForComparison(rawWord.originalWord());
			String displayWord = Normalizer.normalize(rawWord.originalWord(), Normalizer.Form.NFC);
			boolean stripParticle = shouldStripParticle(normalizedWord, rawNormalizedWords, stemToSurfaceWords);
			KoreanParticleNormalizer.NormalizedWord normalizedResult = KoreanParticleNormalizer.normalize(normalizedWord, stripParticle);
			KoreanParticleNormalizer.NormalizedWord displayResult = KoreanParticleNormalizer.normalize(displayWord, stripParticle);

			parsedWords.add(new ParsedWord(
				normalizedResult.normalizedWord(),
				displayResult.displayWord(),
				rawWord.originalWord(),
				rawWord.occurrence()
			));
		}

		return parsedWords;
	}

	/**
	 * 영어 대소문자와 유니코드 표현 차이를 줄여 조사 판정과 반복 비교에 사용할 기본 단어를 만듭니다.
	 */
	private String normalizeForComparison(String word) {
		return Normalizer.normalize(word, Normalizer.Form.NFC).toLowerCase(Locale.ROOT);
	}

	/**
	 * 짧은 조사가 단어 일부일 수 있으면 다른 단어 형태와 연결될 때만 제거합니다.
	 */
	private boolean shouldStripParticle(
		String normalizedWord,
		Set<String> rawNormalizedWords,
		Map<String, Set<String>> stemToSurfaceWords
	) {
		KoreanParticleNormalizer.ParticleMatch particleMatch = KoreanParticleNormalizer.findEndingParticle(normalizedWord);

		if (particleMatch == null) {
			return false;
		}

		if (!particleMatch.requiresContext()) {
			return true;
		}

		if (rawNormalizedWords.contains(particleMatch.stem())) {
			return true;
		}

		return stemToSurfaceWords.getOrDefault(particleMatch.stem(), Set.of()).size() > 1;
	}

	/**
	 * 단어가 처음 등장한 순서를 보존하면서 같은 정규화 단어끼리 묶습니다.
	 */
	private Map<String, List<ParsedWord>> groupByNormalizedWord(List<ParsedWord> parsedWords) {
		Map<String, List<ParsedWord>> groupedWords = new LinkedHashMap<>();

		for (ParsedWord parsedWord : parsedWords) {
			groupedWords.computeIfAbsent(parsedWord.normalizedWord(), ignored -> new ArrayList<>())
				.add(parsedWord);
		}

		return groupedWords;
	}

	/**
	 * 2회 이상 등장한 단어를 빈도순으로 정렬하고 각 단어에 하이라이트 색상을 배정합니다.
	 */
	private List<RepeatedWord> createRepeatedWords(Map<String, List<ParsedWord>> groupedWords) {
		List<Map.Entry<String, List<ParsedWord>>> repeatedEntries = groupedWords.entrySet()
			.stream()
			.filter(entry -> entry.getValue().size() >= 2)
			.sorted(Comparator
				.<Map.Entry<String, List<ParsedWord>>>comparingInt(entry -> entry.getValue().size())
				.reversed())
			.toList();

		List<RepeatedWord> repeatedWords = new ArrayList<>();

		for (int index = 0; index < repeatedEntries.size(); index++) {
			Map.Entry<String, List<ParsedWord>> entry = repeatedEntries.get(index);
			List<ParsedWord> words = entry.getValue();
			String displayWord = words.get(0).displayWord();
			List<WordOccurrence> occurrences = words.stream()
				.map(ParsedWord::occurrence)
				.toList();

			repeatedWords.add(new RepeatedWord(
				entry.getKey(),
				displayWord,
				words.size(),
				HIGHLIGHT_COLORS[index % HIGHLIGHT_COLORS.length],
				occurrences
			));
		}

		return repeatedWords;
	}

	/**
	 * 내부 분석 단계에서만 사용하는 단어 정보입니다.
	 */
	private record ParsedWord(
		String normalizedWord,
		String displayWord,
		String originalWord,
		WordOccurrence occurrence
	) {
	}

	/**
	 * 원문에서 추출한 뒤 아직 조사 정규화를 적용하지 않은 단어 정보입니다.
	 */
	private record RawParsedWord(
		String originalWord,
		WordOccurrence occurrence
	) {
	}
}
