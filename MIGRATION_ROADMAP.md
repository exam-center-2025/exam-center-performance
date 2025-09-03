# Vue 3 마이그레이션 로드맵 및 파일 정리 계획

## 현재 상태 (2025-01-31)

### 하이브리드 구조
- **Thymeleaf 기존 페이지**: `/dashboard`, `/dashboard/monitor`, `/dashboard/results`, `/dashboard/configure`
- **Vue 3 새 페이지**: `/dashboard/vue` (하이브리드 대시보드)

## 파일 사용 현황

### 🟢 현재 사용 중 (제거 불가)

#### Thymeleaf 템플릿
- `templates/dashboard/index.html` - 메인 대시보드 (GET /dashboard)
- `templates/dashboard/monitor.html` - 실시간 모니터링 (GET /dashboard/monitor/{testId})
- `templates/dashboard/results.html` - 테스트 결과 (GET /dashboard/results/{testId})
- `templates/dashboard/configure.html` - 테스트 설정 (GET /dashboard/configure)
- `templates/dashboard/layout.html` - 공통 레이아웃
- `templates/fragments/header.html` - 헤더 프래그먼트
- `templates/dashboard/vue-dashboard.html` - Vue 하이브리드 페이지 (GET /dashboard/vue)

#### JavaScript 파일
- `static/js/dashboard.js` - index.html에서 사용
- `static/js/monitor.js` - monitor.html에서 사용
- `static/js/websocket.js` - header.html에서 전역 사용
- `static/js/charts.js` - header.html에서 전역 사용

### 🔵 Vue 3 신규 파일

#### Vue 빌드 결과물
- `static/vue-dist/dashboard.html` - Vue 대시보드 엔트리
- `static/vue-dist/dashboard.js` - Vue 대시보드 번들
- `static/vue-dist/monitor.html` - Vue 모니터 엔트리
- `static/vue-dist/monitor.js` - Vue 모니터 번들
- `static/vue-dist/chunks/*.js` - 공유 청크

#### Vue 소스 코드 (frontend-vue/)
- `src/apps/dashboard/` - 대시보드 Vue 앱
- `src/apps/monitor/` - 모니터 Vue 앱
- `src/shared/` - 공유 컴포넌트, 스토어, 유틸리티

## 단계별 마이그레이션 계획

### Phase 1: 하이브리드 운영 (현재) ✅
- Thymeleaf와 Vue 페이지 공존
- 사용자가 `/dashboard`와 `/dashboard/vue` 중 선택 가능
- 기능 동등성 검증

### Phase 2: 점진적 전환 (예정)
1. **index.html → Vue 전환**
   - `/dashboard` 경로를 Vue로 리다이렉트
   - 제거 가능: `dashboard.js`, `index.html`

2. **monitor.html → Vue 전환**
   - `/dashboard/monitor/*` 경로를 Vue로 전환
   - 제거 가능: `monitor.js`, `monitor.html`

3. **WebSocket/Charts 통합**
   - Vue 컴포넌트로 WebSocket, Chart 기능 통합
   - 제거 가능: `websocket.js`, `charts.js`

### Phase 3: 완전 전환
- 모든 Thymeleaf 템플릿을 Vue SPA로 전환
- 제거 가능한 파일:
  ```
  templates/dashboard/index.html
  templates/dashboard/monitor.html
  templates/dashboard/configure.html
  templates/dashboard/layout.html
  templates/fragments/header.html
  static/js/dashboard.js
  static/js/monitor.js
  static/js/websocket.js
  static/js/charts.js
  ```

## 전환 시 주의사항

### ⚠️ 유지해야 할 파일
- `templates/dashboard/results.html` - Gatling 리포트 연동으로 당분간 유지
- `templates/dashboard/vue-dashboard.html` - Vue 마운트 포인트로 계속 필요

### 🔄 Controller 수정 필요
```java
// Phase 2: 리다이렉트 추가
@GetMapping
public String dashboard() {
    return "redirect:/dashboard/vue";
}

// Phase 3: 모든 경로를 Vue로
@GetMapping("/**")
public String vueApp() {
    return "dashboard/vue-dashboard";
}
```

## 현재 권장사항

**❌ 아직 파일 제거하지 마세요**
- 하이브리드 접근법으로 두 시스템이 공존 중
- 사용자가 기존 인터페이스에 익숙할 수 있음
- 완전한 기능 검증 후 점진적 제거 필요

**✅ 다음 단계**
1. Vue 버전의 기능 완성도 높이기
2. 사용자 피드백 수집
3. A/B 테스트로 선호도 확인
4. 단계적 전환 시작

## 명령어 참고

```bash
# Vue 개발 서버 실행
cd frontend-vue
npm run dev

# Vue 빌드 (Spring 정적 리소스로)
npm run build

# 기존 JS 파일 백업 (전환 시)
mkdir -p backup/static/js
cp -r src/main/resources/static/js/* backup/static/js/

# 기존 템플릿 백업 (전환 시)
mkdir -p backup/templates
cp -r src/main/resources/templates/* backup/templates/
```

## 체크리스트

### 전환 전 확인사항
- [ ] Vue 버전이 모든 기능 구현 완료
- [ ] 성능 테스트 통과
- [ ] 사용자 교육 자료 준비
- [ ] 롤백 계획 수립
- [ ] 백업 완료

### 전환 후 확인사항
- [ ] 모든 엔드포인트 정상 동작
- [ ] WebSocket 연결 정상
- [ ] 차트 렌더링 정상
- [ ] Gatling 리포트 연동 정상
- [ ] 에러 페이지 정상 표시