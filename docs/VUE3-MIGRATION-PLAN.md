# Vue 3 마이그레이션 계획서
*작성일: 2025-01-03*
*최종 업데이트: 2025-01-31*
*프로젝트: exam-center-performance*
*상태: **Phase 1 완료** - 하이브리드 구조 구현 완료*

## 📋 개요

exam-center-performance 서비스의 Frontend를 Thymeleaf + jQuery 기반에서 Vue 3 기반으로 전환하는 마이그레이션 계획서입니다.

## 🎯 마이그레이션 목표

### 주요 목표
- 현대적인 Frontend 아키텍처 도입
- 개발 생산성 및 유지보수성 향상
- 컴포넌트 기반 재사용 가능한 UI 구축
- 실시간 데이터 처리 성능 개선

### 성공 지표
- 기존 기능 100% 호환성 유지
- 페이지 로딩 속도 30% 개선
- 코드 재사용률 40% 이상 달성
- 테스트 커버리지 80% 이상

## 🏗️ 현재 상태 분석

### 현재 기술 스택
- **Backend**: Spring Boot 3.4.2 + WebFlux
- **Frontend**: Thymeleaf + jQuery + Chart.js
- **실시간 통신**: STOMP over WebSocket
- **스타일링**: Tailwind CSS

### 현재 페이지 구성
1. **Dashboard** (`/dashboard`) - 메인 대시보드
2. **Monitor** (`/monitor`) - 실시간 모니터링
3. **Config** (`/config`) - 테스트 설정
4. **Results** (`/results`) - 결과 조회

### 현재 파일 구조
```
src/main/resources/
├── templates/           # Thymeleaf 템플릿 (4개)
│   ├── dashboard.html   (425줄)
│   ├── config.html      (312줄)
│   ├── monitor.html     (378줄)
│   └── results.html     (289줄)
└── static/js/          # jQuery 기반 (2,208줄)
    ├── dashboard.js     (678줄)
    ├── websocket.js     (456줄)
    ├── monitor.js       (589줄)
    └── charts.js        (485줄)
```

## 🚀 마이그레이션 전략: 하이브리드 접근법

### 핵심 원칙
1. **점진적 전환**: 페이지 단위로 순차적 마이그레이션
2. **무중단 전환**: 기존 시스템 운영 중 전환
3. **리스크 최소화**: 각 단계별 롤백 가능
4. **호환성 유지**: Spring Boot 인프라 그대로 활용

### 아키텍처 개요
```
[ Spring Boot Application (Port: 8097) ]
    │
    ├── Phase 1: 하이브리드 모드
    │   ├── Thymeleaf Pages (유지)
    │   │   ├── /config → config.html
    │   │   └── /results → results.html
    │   └── Vue 3 Pages (신규)
    │       ├── /dashboard → Vue App
    │       └── /monitor → Vue App
    │
    └── Phase 2: 완전 전환 (선택적)
        └── 모든 페이지 Vue 3 SPA
```

## 📐 목표 아키텍처

### Vue 3 프로젝트 구조
```
exam-center-performance/
├── frontend-vue/                    # Vue 3 프로젝트
│   ├── src/
│   │   ├── apps/                   # 페이지별 독립 앱
│   │   │   ├── dashboard/
│   │   │   │   ├── main.js        # 진입점
│   │   │   │   ├── App.vue        # 루트 컴포넌트
│   │   │   │   └── components/    # Dashboard 전용
│   │   │   └── monitor/
│   │   │       ├── main.js
│   │   │       ├── App.vue
│   │   │       └── components/
│   │   │
│   │   ├── shared/                 # 공통 모듈
│   │   │   ├── components/        # 공통 컴포넌트
│   │   │   │   ├── charts/
│   │   │   │   │   ├── LineChart.vue
│   │   │   │   │   ├── BarChart.vue
│   │   │   │   │   └── GaugeChart.vue
│   │   │   │   └── ui/
│   │   │   │       ├── Card.vue
│   │   │   │       ├── Button.vue
│   │   │   │       └── Modal.vue
│   │   │   │
│   │   │   ├── composables/       # Composition API
│   │   │   │   ├── useWebSocket.js
│   │   │   │   ├── useMetrics.js
│   │   │   │   └── useApi.js
│   │   │   │
│   │   │   ├── services/          # API 서비스
│   │   │   │   ├── api.js
│   │   │   │   ├── performanceApi.js
│   │   │   │   └── metricsApi.js
│   │   │   │
│   │   │   └── stores/            # Pinia 스토어
│   │   │       ├── performance.js
│   │   │       ├── metrics.js
│   │   │       └── websocket.js
│   │   │
│   │   └── assets/
│   │       └── styles/
│   │           └── main.css       # Tailwind CSS
│   │
│   ├── package.json
│   ├── vite.config.js
│   └── tailwind.config.js
│
└── src/main/resources/
    ├── templates/                  # 수정된 Thymeleaf
    │   ├── dashboard.html          # Vue 마운트 포인트
    │   └── monitor.html            # Vue 마운트 포인트
    └── static/
        └── vue-dist/               # Vue 빌드 결과물
```

### 기술 스택
```json
{
  "dependencies": {
    "vue": "^3.4.21",
    "vue-router": "^4.3.0",
    "pinia": "^2.1.7",
    "axios": "^1.6.7",
    "@stomp/stompjs": "^7.0.0",
    "chart.js": "^4.4.1",
    "vue-chartjs": "^5.3.0",
    "@vueuse/core": "^10.9.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.4",
    "vite": "^5.1.4",
    "tailwindcss": "^3.4.1",
    "@tailwindcss/forms": "^0.5.7",
    "@vue/test-utils": "^2.4.4",
    "vitest": "^1.3.1",
    "eslint": "^8.57.0",
    "eslint-plugin-vue": "^9.22.0"
  }
}
```

## 📅 구현 단계별 계획

### ✅ Phase 1: 환경 구축 (완료 - 2025-01-31)

#### Day 1-2: 프로젝트 초기화
- [x] Vue 3 프로젝트 생성
- [x] 디렉토리 구조 설정
- [x] 패키지 설치 및 설정
- [x] Vite 설정 (Multi-page 앱)
- [x] Tailwind CSS 통합

#### Day 3-4: 빌드 통합
- [x] Gradle 태스크 추가
  ```gradle
  task buildVue(type: Exec) {
      workingDir 'frontend-vue'
      commandLine 'npm', 'run', 'build'
  }
  
  processResources.dependsOn buildVue
  ```
- [x] 개발 환경 프록시 설정
- [x] Spring Boot CORS 설정
- [x] 정적 리소스 서빙 설정

#### Day 5: 개발 환경 검증
- [x] Hot Reload 테스트
- [x] API 연동 테스트
- [x] WebSocket 연결 테스트
- [x] 빌드 파이프라인 검증

### Phase 2: Dashboard 페이지 전환 (Week 2-3)

#### Week 2: 컴포넌트 개발
- [ ] Dashboard 레이아웃 구성
- [ ] 컴포넌트 분해 및 설계
  - TestControlPanel.vue
  - MetricsDisplay.vue
  - RealtimeChart.vue
  - StatusIndicator.vue
  - TestHistory.vue
- [ ] Pinia 스토어 구현
- [ ] API 서비스 레이어 구현

#### Week 3: 통합 및 테스트
- [ ] Thymeleaf 템플릿 수정
- [ ] Vue 앱 마운트 구현
- [ ] 데이터 바인딩 검증
- [ ] WebSocket 실시간 업데이트 테스트
- [ ] 기능 동등성 검증

### Phase 3: Monitor 페이지 전환 (Week 4)

- [ ] Monitor 컴포넌트 개발
  - RealtimeMonitor.vue
  - LogViewer.vue
  - MetricsGrid.vue
  - AlertPanel.vue
- [ ] 공통 컴포넌트 추출
- [ ] 상태 관리 최적화
- [ ] 성능 테스트 및 최적화

### Phase 4: 안정화 및 최적화 (Week 5-6)

#### Week 5: 테스트 및 버그 수정
- [ ] 단위 테스트 작성 (Vitest)
- [ ] E2E 테스트 구성
- [ ] 크로스 브라우저 테스트
- [ ] 성능 프로파일링
- [ ] 버그 수정

#### Week 6: 최적화
- [ ] 번들 크기 최적화
- [ ] 코드 스플리팅
- [ ] 레이지 로딩
- [ ] 캐싱 전략
- [ ] 문서화

### Phase 5: 선택적 완전 전환 (Week 7-8)

- [ ] Config 페이지 Vue 전환
- [ ] Results 페이지 Vue 전환
- [ ] Vue Router 통합
- [ ] 완전 SPA 전환 평가
- [ ] Thymeleaf 의존성 제거 검토

## 🔧 주요 구현 사항

### 1. WebSocket 통합
```javascript
// composables/useWebSocket.js
import { ref, onMounted, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'

export function useWebSocket() {
  const client = ref(null)
  const connected = ref(false)
  const metrics = ref([])
  
  const connect = () => {
    client.value = new Client({
      brokerURL: 'ws://localhost:8097/performance/ws',
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      
      onConnect: () => {
        connected.value = true
        
        // 메트릭 구독
        client.value.subscribe('/topic/metrics', (message) => {
          metrics.value.push(JSON.parse(message.body))
        })
      },
      
      onDisconnect: () => {
        connected.value = false
      }
    })
    
    client.value.activate()
  }
  
  const disconnect = () => {
    if (client.value) {
      client.value.deactivate()
    }
  }
  
  onMounted(() => connect())
  onUnmounted(() => disconnect())
  
  return {
    connected,
    metrics,
    client
  }
}
```

### 2. Thymeleaf 통합
```html
<!-- dashboard.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Performance Dashboard</title>
    <link rel="stylesheet" th:href="@{/vue-dist/assets/style.css}">
    
    <!-- Thymeleaf 변수를 JavaScript로 전달 -->
    <script th:inline="javascript">
        window.__INITIAL_STATE__ = {
            user: /*[[${user}]]*/ null,
            config: /*[[${config}]]*/ {},
            apiBase: /*[[@{/api}]]*/ '/api'
        };
    </script>
</head>
<body>
    <!-- Vue 앱 마운트 포인트 -->
    <div id="dashboard-app"></div>
    
    <!-- Vue 번들 -->
    <script type="module" th:src="@{/vue-dist/dashboard.js}"></script>
</body>
</html>
```

### 3. Spring Boot 설정
```java
// CorsConfig.java
@Configuration
public class CorsConfig {
    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOriginPatterns("http://localhost:*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
                    
                registry.addMapping("/ws/**")
                    .allowedOriginPatterns("http://localhost:*")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/vue-dist/**")
                .addResourceLocations("classpath:/static/vue-dist/")
                .setCachePeriod(3600)
                .resourceChain(true);
    }
}
```

## 📊 리스크 관리

### 주요 리스크 및 대응 방안

| 리스크 | 영향도 | 확률 | 대응 방안 |
|--------|--------|------|-----------|
| WebSocket 호환성 문제 | 높음 | 중간 | 폴백 메커니즘 구현, 롱폴링 지원 |
| 빌드 복잡도 증가 | 중간 | 높음 | CI/CD 자동화, 문서화 |
| 성능 저하 | 높음 | 낮음 | 점진적 로딩, 번들 최적화 |
| 팀 학습 곡선 | 중간 | 높음 | 교육 세션, 페어 프로그래밍 |
| 일정 지연 | 중간 | 중간 | 버퍼 시간 확보, MVP 우선 |

### 롤백 계획
- 각 Phase별 독립적 롤백 가능
- Git 브랜치 전략으로 버전 관리
- Feature 플래그로 Vue/Thymeleaf 전환

## 🎯 성공 기준

### 정량적 지표
- [ ] 페이지 로딩 시간: 3초 → 2초 이하
- [ ] 번들 크기: 300KB 이하
- [ ] 테스트 커버리지: 80% 이상
- [ ] 코드 재사용률: 40% 이상

### 정성적 지표
- [ ] 개발자 만족도 향상
- [ ] 유지보수 시간 50% 감소
- [ ] 신규 기능 개발 속도 향상
- [ ] 코드 가독성 개선

## 📝 체크리스트

### Phase 1 완료 기준
- [ ] Vue 프로젝트 구조 완성
- [ ] 빌드 파이프라인 통합
- [ ] 개발 환경 정상 작동
- [ ] 기본 컴포넌트 라이브러리 구축

### Phase 2 완료 기준
- [ ] Dashboard 페이지 Vue 전환 완료
- [ ] 모든 기능 정상 작동
- [ ] 테스트 통과
- [ ] 성능 기준 충족

### Phase 3 완료 기준
- [ ] Monitor 페이지 Vue 전환 완료
- [ ] 공통 컴포넌트 추출 완료
- [ ] 코드 재사용률 30% 이상

### 최종 완료 기준
- [ ] 2개 이상 페이지 Vue 전환
- [ ] 문서화 완료
- [ ] 팀 교육 완료
- [ ] 프로덕션 배포 준비

## 📚 참고 자료

- [Vue 3 공식 문서](https://vuejs.org/)
- [Vite 공식 문서](https://vitejs.dev/)
- [Pinia 공식 문서](https://pinia.vuejs.org/)
- [Spring Boot + Vue 통합 가이드](https://spring.io/guides)
- [STOMP.js 문서](https://stomp-js.github.io/)

## 🤝 담당자 및 일정

- **프로젝트 리드**: [담당자명]
- **Frontend 개발**: [담당자명]
- **Backend 통합**: [담당자명]
- **QA**: [담당자명]

### 주요 마일스톤
- **Week 1**: 환경 구축 완료
- **Week 3**: Dashboard 전환 완료
- **Week 4**: Monitor 전환 완료
- **Week 6**: 안정화 완료
- **Week 8**: 최종 검수 및 배포

---

## 🚀 현재 구현 상태 (2025-01-31)

### ✅ 완료된 작업

#### Phase 1: 하이브리드 환경 구축
1. **Vue 3 프로젝트 구조 구성 완료**
   - `frontend-vue/` 디렉토리 생성
   - Multi-page 앱 구조 (dashboard, monitor)
   - Vite 빌드 설정 완료

2. **핵심 컴포넌트 구현**
   - `TestControlPanel.vue` - 테스트 제어 패널
   - `MetricsDisplay.vue` - 메트릭 표시
   - `RealtimeChart.vue` - 실시간 차트
   - `StatusIndicator.vue` - 상태 표시기

3. **상태 관리 및 통신**
   - Pinia 스토어 구현 (`performance.js`)
   - WebSocket composable 구현
   - STOMP 프로토콜 연동

4. **Spring Boot 통합**
   - `/dashboard/vue` 엔드포인트 추가
   - `DashboardController.java` 수정
   - `DashboardService.java` 확장
   - 정적 리소스 서빙 설정

5. **UI 개선**
   - 테스트 결과 페이지 가독성 개선
   - Gatling 리포트 연동 확인
   - 색상 대비 향상

### 📊 현재 아키텍처

```
[ Spring Boot (Port: 8097) ]
    │
    ├── Thymeleaf Pages (기존)
    │   ├── /dashboard → index.html
    │   ├── /dashboard/monitor → monitor.html
    │   ├── /dashboard/results → results.html
    │   └── /dashboard/configure → configure.html
    │
    └── Vue 3 Pages (신규)
        └── /dashboard/vue → vue-dashboard.html
            ├── Dashboard App
            └── Monitor App (준비됨)
```

### 📁 파일 구조

```
exam-center-performance/
├── frontend-vue/                    ✅ 구현 완료
│   ├── src/
│   │   ├── apps/
│   │   │   ├── dashboard/         ✅ 기본 구현
│   │   │   └── monitor/           ✅ 스켈레톤 구현
│   │   ├── shared/
│   │   │   ├── components/        ✅ 공통 컴포넌트
│   │   │   ├── composables/       ✅ WebSocket 등
│   │   │   ├── services/          ✅ API 서비스
│   │   │   └── stores/            ✅ Pinia 스토어
│   │   └── assets/                 ✅ 스타일 설정
│   ├── package.json                ✅ 의존성 설정
│   └── vite.config.js              ✅ 빌드 설정
│
└── src/main/resources/
    ├── templates/                  
    │   └── dashboard/
    │       └── vue-dashboard.html  ✅ Vue 마운트 포인트
    └── static/
        └── vue-dist/               ✅ 빌드 결과물
```

## 📋 다음 단계 (Phase 2)

### 🎯 목표: Dashboard 페이지 완성 및 Monitor 페이지 전환

#### 1. Dashboard 페이지 고도화 (우선순위: 높음)
- [ ] 실시간 메트릭 차트 완성
- [ ] 테스트 히스토리 테이블 구현
- [ ] 필터 및 검색 기능 추가
- [ ] 성능 테스트 시작/중지 기능 완성
- [ ] WebSocket 실시간 업데이트 안정화

#### 2. Monitor 페이지 Vue 전환 (우선순위: 높음)
- [ ] 실시간 로그 뷰어 구현
- [ ] 메트릭 그리드 구현
- [ ] 알림 패널 구현
- [ ] WebSocket 스트리밍 최적화

#### 3. 공통 컴포넌트 라이브러리 확장
- [ ] 차트 컴포넌트 세트 완성
- [ ] 폼 컴포넌트 라이브러리
- [ ] 로딩 및 에러 상태 컴포넌트
- [ ] 토스트 알림 시스템

#### 4. 성능 최적화
- [ ] 코드 스플리팅 적용
- [ ] 레이지 로딩 구현
- [ ] 번들 크기 최적화
- [ ] 캐싱 전략 구현

### 🔄 마이그레이션 전략

1. **단계적 전환**
   - 현재: 하이브리드 모드 (`/dashboard`와 `/dashboard/vue` 공존)
   - 다음: 사용자 피드백 수집 및 기능 완성
   - 최종: 기본 경로를 Vue로 전환

2. **위험 관리**
   - 기존 Thymeleaf 페이지 유지
   - 각 단계별 롤백 가능
   - A/B 테스트 진행

3. **품질 보증**
   - 기능 동등성 테스트
   - 성능 벤치마크
   - 사용자 수용 테스트

### 📆 예상 일정

- **Week 1-2**: Dashboard 페이지 완성
- **Week 3**: Monitor 페이지 전환
- **Week 4**: 테스트 및 최적화
- **Week 5**: 사용자 피드백 반영
- **Week 6**: 프로덕션 준비

### 🛠️ 개발 명령어

```bash
# Vue 개발 서버 실행
cd frontend-vue
npm run dev

# Vue 빌드
npm run build

# Spring Boot 실행
./gradlew bootRun

# 전체 빌드 (Vue + Spring Boot)
./gradlew clean build
```

### 📌 주의사항

1. **하이브리드 운영 중**
   - 기존 파일 제거 금지
   - 두 시스템 호환성 유지
   - 점진적 전환 원칙 준수

2. **테스트 우선**
   - 모든 변경사항 테스트
   - 기능 동등성 검증
   - 성능 저하 모니터링

3. **문서화**
   - 변경사항 기록
   - API 문서 업데이트
   - 사용자 가이드 준비

---

*이 문서는 프로젝트 진행 상황에 따라 지속적으로 업데이트됩니다.*
*최종 수정일: 2025-01-31*