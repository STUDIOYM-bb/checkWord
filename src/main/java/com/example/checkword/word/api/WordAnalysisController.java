package com.example.checkword.word.api;

import com.example.checkword.word.application.WordAnalysisService;
import com.example.checkword.word.domain.WordAnalysisResult;
import com.example.checkword.word.dto.WordAnalysisRequest;
import com.example.checkword.word.dto.WordAnalysisResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 반복 단어 분석 기능을 HTTP API로 제공하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/words")
@CrossOrigin(origins = {
	"http://localhost:5173",
	"http://127.0.0.1:5173"
})
public class WordAnalysisController {

	private final WordAnalysisService wordAnalysisService;

	/**
	 * 분석 서비스 의존성을 생성자 주입으로 전달받습니다.
	 */
	public WordAnalysisController(WordAnalysisService wordAnalysisService) {
		this.wordAnalysisService = wordAnalysisService;
	}

	/**
	 * 사용자가 입력한 글을 분석해 2회 이상 반복된 단어 목록과 원문 위치를 반환합니다.
	 */
	@PostMapping("/analyze")
	public ResponseEntity<WordAnalysisResponse> analyze(@Valid @RequestBody WordAnalysisRequest request) {
		WordAnalysisResult result = wordAnalysisService.analyze(request.text());
		return ResponseEntity.ok(WordAnalysisResponse.from(result));
	}
}
