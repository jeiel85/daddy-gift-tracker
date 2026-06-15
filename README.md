# 경조사 인맥 관리

![경조사 인맥 관리 hero](assets/readme-hero.png)

40~50대 아빠가 결혼식, 장례식, 돌잔치, 명절 인사처럼 놓치기 쉬운 관계의 기록을 차분하게 관리하는 Android 장부 앱입니다. 지인별 경조사비, 받은 금액, 다음에 챙길 일정, 관계별 통계를 한곳에 모아 둡니다.

## 주요 기능

- 인맥부: 가족, 친척, 회사, 동창, 친구, 교회 등 관계별 지인 기록
- 경조사 장부: 보낸 돈과 받은 돈, 장소, 메모, 날짜를 지인별로 저장
- 일정 알림: 생일, 명절 인사, 다음 경조사 챙김을 로컬 알림으로 예약
- 통계: 누적 지출/수입과 관계별 흐름을 한눈에 확인
- 백업: CSV 형태로 장부를 내보내고 클립보드로 복사
- 로컬 우선: 민감한 인맥/금액 데이터를 기기 안에 보관

## 릴리즈 정보

- Application ID: `com.jeiel.daddygifttracker`
- Current version: `1.1` (`versionCode` 2)
- Minimum SDK: 24
- Target SDK: 36

## 빌드

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

릴리즈 번들은 로컬 키스토어가 준비된 뒤 생성합니다.

```powershell
.\gradlew.bat :app:bundleRelease
.\gradlew.bat :app:exportReleaseToDesktop
```

릴리즈 서명 파일은 `.keystore/`에만 보관하며 Git에는 포함하지 않습니다.

## 공개 페이지와 스토어 자산

- GitHub Pages landing: `docs/index.html`
- Favicon/app icon: `docs/assets/favicon.png`, `docs/assets/app-icon.png`
- Play Console graphics: `store-graphics/play-console-current/`
- Play release notes: `play_store/release_notes/v1.1.txt`

## 개발 원칙

이 앱은 외부 AI 서비스나 서버 연동 없이 실제 경조사 기록 관리에 필요한 코드만 포함합니다. 릴리즈용 패키지명, 실제 앱 검증 테스트, 서명/내보내기 흐름을 사용합니다.
