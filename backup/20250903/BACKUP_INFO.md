# 백업 정보

**백업 일시**: 2025-09-03  
**백업 이유**: Vue 3 마이그레이션 완료에 따른 기존 파일 백업

## 📦 백업된 파일 목록

### JavaScript 파일 (static/js/)
- `charts.js` (20,393 bytes) - 차트 관련 JavaScript
- `dashboard.js` (17,906 bytes) - 대시보드 페이지 JavaScript
- `monitor.js` (16,458 bytes) - 모니터링 페이지 JavaScript
- `websocket.js` (11,647 bytes) - WebSocket 통신 JavaScript

### Thymeleaf 템플릿 (templates/dashboard/)
- `configure.html` (17,091 bytes) - 테스트 설정 페이지
- `index.html` (11,477 bytes) - 메인 대시보드 페이지
- `monitor.html` (6,800 bytes) - 실시간 모니터링 페이지

## 🔄 대체된 Vue 페이지

| 기존 파일 | Vue 대체 경로 |
|----------|--------------|
| `index.html` + `dashboard.js` | `/dashboard/vue` |
| `monitor.html` + `monitor.js` | `/dashboard/monitor-vue/{testId}` |
| `configure.html` | `/dashboard/configure-vue` |
| `charts.js` | Vue Chart.js 컴포넌트로 대체 |
| `websocket.js` | Vue WebSocket composable로 대체 |

## ⚠️ 주의사항

### 아직 사용 중인 파일들 (백업하지 않음)
- `results.html` - Gatling 리포트 연동으로 계속 사용 중
- `layout.html` - 레이아웃 템플릿
- `fragments/header.html` - 헤더 프래그먼트

### 완전 전환 시 제거 계획
1. **Phase 1** (현재): 하이브리드 운영 - 두 시스템 공존
2. **Phase 2**: A/B 테스트 및 사용자 피드백
3. **Phase 3**: 기본 경로를 Vue로 전환
4. **Phase 4**: 백업된 파일들 제거

## 🔧 복원 방법

백업된 파일을 복원해야 하는 경우:

```bash
# JavaScript 파일 복원
cp backup/20250903/static/js/* src/main/resources/static/js/

# Thymeleaf 템플릿 복원
cp backup/20250903/templates/dashboard/* src/main/resources/templates/dashboard/
```

## 📝 백업 요약

- **총 백업 파일 수**: 7개
- **총 백업 크기**: 약 100KB
- **백업 경로**: `/home/ldm/exam-center-dev/exam-center-performance/backup/20250903/`

이 백업은 Vue 3 마이그레이션이 완료되어 더 이상 필요하지 않은 파일들을 안전하게 보관하기 위한 것입니다.
완전한 전환이 확정되고 충분한 테스트 후에 삭제할 수 있습니다.