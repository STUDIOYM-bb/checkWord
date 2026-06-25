# CheckWord

입력된 글에서 2회 이상 반복되는 단어를 찾아 단어별 색상으로 표시하는 Spring Boot + React 프로그램입니다.

## 주요 기능

- 한글, 영어, 숫자가 섞인 긴 글 입력 지원
- 공백과 특수문자는 단어 구분자로 처리
- 1글자 단어 제외
- 영문 대소문자 차이를 무시하고 반복 단어 계산
- 한국어 명사 뒤에 붙은 조사를 제외하고 반복 단어 계산
- 반복 단어별 색상 하이라이트
- 단어별 반복 횟수와 등장 위치 기반 원문 표시

## 프로젝트 구조

```text
.
├── src/main/java/com/example/checkword
│   ├── CheckWordApplication.java
│   └── word
│       ├── api
│       ├── application
│       ├── domain
│       └── dto
├── src/test/java/com/example/checkword
└── frontend
    ├── src
    ├── index.html
    └── package.json
```

## 실행 방법

백엔드:

```bash
./gradlew bootRun
```

프론트엔드:

```bash
cd frontend
npm install
npm run dev
```

기본 접속 주소는 `http://localhost:5173`입니다. 프론트엔드는 기본적으로 `http://localhost:8080`의 백엔드 API를 호출합니다.

## API

### 반복 단어 분석

`POST /api/words/analyze`

요청:

```json
{
  "text": "사과 사과 apple Apple 123 123"
}
```

응답:

```json
{
  "text": "사과 사과 apple Apple 123 123",
  "totalWordCount": 6,
  "repeatedWordTypeCount": 3,
  "repeatedWords": [
    {
      "normalizedWord": "사과",
      "displayWord": "사과",
      "count": 2,
      "color": "#F97316",
      "occurrences": [
        { "startIndex": 0, "endIndex": 2, "originalText": "사과" },
        { "startIndex": 3, "endIndex": 5, "originalText": "사과" }
      ]
    }
  ]
}
```

## 단어 판정 기준

- `Character.isLetterOrDigit` 기준의 유니코드 문자와 숫자를 단어 문자로 봅니다.
- 공백, 쉼표, 마침표, 괄호, 기호 등 특수문자는 단어를 나누는 구분자로 처리합니다.
- 코드 포인트 기준 2글자 이상인 단어만 분석합니다.
- 영어는 `Locale.ROOT` 기준 소문자로 정규화해 `Apple`과 `apple`을 같은 단어로 계산합니다.
- 한국어 조사는 단어 끝에 붙은 suffix일 때만 제거합니다. 예를 들어 `사과와`, `사과는`, `사과를`은 모두 `사과`로 계산합니다.
- 조사 제거 뒤 남는 단어가 1글자 이하이면 오탐을 줄이기 위해 원문 단어를 유지합니다.
- `을지로`처럼 조사 글자가 단어 내부나 고유명사 일부로 쓰이는 대표 케이스는 보호 단어로 처리합니다.

## 테스트

```bash
./gradlew test
cd frontend
npm run build
```
