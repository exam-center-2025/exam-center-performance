# Vue 공통 컴포넌트 라이브러리

> **AIDEV-NOTE**: exam-center-performance 프로젝트의 모든 Vue 앱(Dashboard, Monitor, Configure, Results)에서 사용할 수 있는 공통 컴포넌트 라이브러리입니다.

## 📁 디렉토리 구조

```
src/shared/
├── components/          # 재사용 가능한 UI 컴포넌트
│   ├── ui/             # 기본 UI 컴포넌트
│   ├── form/           # 폼 관련 컴포넌트  
│   ├── charts/         # 차트 컴포넌트
│   ├── layout/         # 레이아웃 컴포넌트
│   ├── feedback/       # 피드백 컴포넌트
│   ├── data/           # 데이터 표시 컴포넌트
│   ├── navigation/     # 네비게이션 컴포넌트
│   ├── utils/          # 유틸리티 컴포넌트
│   └── index.js        # 컴포넌트 인덱스
├── composables/        # Vue Composition API 함수들
├── services/           # API 서비스 레이어
├── stores/             # Pinia 스토어
└── styles/             # 공통 스타일
    ├── variables.css   # CSS 변수 및 디자인 토큰
    ├── utilities.css   # 유틸리티 클래스
    └── common.css      # 공통 스타일
```

## 🎨 완성된 컴포넌트들

### UI 컴포넌트 (ui/)
- ✅ **BaseButton** - 재사용 가능한 버튼 (primary, secondary, danger, success, warning, info, outline, ghost, link)
- ✅ **BaseCard** - 카드 컨테이너 (헤더, 본문, 푸터 슬롯, 로딩/에러 상태)
- ✅ **BaseModal** - 모달 다이얼로그 (접근성, 포커스 트랩, ESC 키 지원)
- ✅ **BaseTable** - 테이블 컴포넌트 (정렬, 페이지네이션, 선택 기능)
- ✅ **BaseBadge** - 상태 배지 (다양한 변형과 크기)
- ✅ **BaseTooltip** - 툴팁 컴포넌트 (12가지 배치 옵션)
- ✅ **BaseSpinner** - 로딩 스피너 (5가지 타입: spin, dots, pulse, bars, ring)
- ✅ **BaseSkeleton** - 스켈레톤 로더 (텍스트, 아바타, 이미지, 버튼, 카드, 리스트, 테이블)

### 폼 컴포넌트 (form/)
- ✅ **BaseInput** - 입력 필드 (에러 상태, 도움말, 접두사/접미사, 클리어 버튼, 비밀번호 토글)
- ✅ **BaseSelect** - 선택 박스 (검색, 다중 선택, 커스텀 옵션 렌더링)
- ✅ **BaseCheckbox** - 체크박스 (그룹/개별 선택, 커스텀 스타일, indeterminate 상태)
- ✅ **BaseRadio** - 라디오 버튼 (그룹 선택, 커스텀 스타일, 설명 텍스트)
- ✅ **BaseTextarea** - 텍스트 영역 (자동 크기 조정, 글자 수/단어 수 카운터, 리사이즈)
- ✅ **BaseSwitch** - 토글 스위치 (애니메이션, 아이콘, 라벨 위치 조정)
- ✅ **FormGroup** - 폼 그룹 (라벨, 검증, 에러 처리 통합)

### 차트 컴포넌트 (charts/)
- ✅ **LineChart** - 선 차트 (Chart.js 기반, 반응형, 테마 지원)
- 🚧 **BarChart** - 막대 차트 (개발 예정)
- 🚧 **PieChart** - 파이 차트 (개발 예정)
- 🚧 **GaugeChart** - 게이지 차트 (개발 예정)
- 🚧 **SparklineChart** - 스파크라인 (개발 예정)

### Composables
- ✅ **useToast** - 토스트 알림 관리 (success, error, warning, info, promise 기반)
- ✅ **useModal** - 모달 상태 관리 (프로그래밍 방식 제어, 스택 관리)
- ✅ **useForm** - 폼 상태 관리 (검증, 제출, 에러 처리)

### 스타일 시스템
- ✅ **variables.css** - CSS 변수 및 디자인 토큰 (색상, 간격, 타이포그래피, 그림자)
- ✅ **utilities.css** - 유틸리티 클래스 (레이아웃, 텍스트, 색상, 애니메이션)

## 🚀 사용법

### 1. 전체 컴포넌트 등록

```javascript
// main.js
import { createApp } from 'vue';
import App from './App.vue';
import SharedComponents from '@/shared/components';

const app = createApp(App);
app.use(SharedComponents);
app.mount('#app');
```

### 2. 개별 컴포넌트 사용

```vue
<template>
  <div>
    <!-- 버튼 예시 -->
    <BaseButton 
      variant="primary" 
      size="lg"
      :loading="isLoading"
      @click="handleSubmit"
    >
      제출하기
    </BaseButton>

    <!-- 모달 예시 -->
    <BaseModal 
      v-model="showModal" 
      title="확인" 
      variant="danger"
      :show-default-buttons="true"
      @confirm="handleConfirm"
      @cancel="showModal = false"
    >
      <p>정말로 삭제하시겠습니까?</p>
    </BaseModal>

    <!-- 폼 그룹 예시 -->
    <FormGroup 
      label="사용자명" 
      :required="true"
      :error="errors.username"
      help-text="영문, 숫자 조합 4-20자"
    >
      <BaseInput 
        v-model="form.username"
        placeholder="사용자명을 입력하세요"
        :clearable="true"
      />
    </FormGroup>

    <!-- 테이블 예시 -->
    <BaseTable
      :columns="columns"
      :data="users"
      :sortable="true"
      :selectable="true"
      @sort="handleSort"
      @select="handleSelect"
    >
      <template #actions="{ row }">
        <BaseButton size="sm" variant="outline" @click="editUser(row)">
          편집
        </BaseButton>
      </template>
    </BaseTable>
  </div>
</template>

<script>
import { ref } from 'vue';
import { BaseButton, BaseModal, FormGroup, BaseInput, BaseTable } from '@/shared/components';

export default {
  components: {
    BaseButton,
    BaseModal, 
    FormGroup,
    BaseInput,
    BaseTable
  },
  setup() {
    const showModal = ref(false);
    const isLoading = ref(false);
    // ... 기타 로직
  }
};
</script>
```

### 3. Composables 사용

```vue
<script>
import { useToast, useModal, useForm } from '@/shared/composables';

export default {
  setup() {
    // 토스트 사용
    const { success, error, warning } = useToast();
    
    const handleSuccess = () => {
      success('저장되었습니다!');
    };
    
    // 모달 사용
    const { open: openModal } = useModal();
    
    const showConfirmDialog = async () => {
      try {
        await openModal('ConfirmDialog', {
          title: '삭제 확인',
          message: '정말로 삭제하시겠습니까?'
        });
        // 사용자가 확인을 클릭한 경우
        handleDelete();
      } catch (err) {
        // 사용자가 취소를 클릭한 경우
        console.log('취소됨');
      }
    };
    
    // 폼 사용
    const { form, errors, handleSubmit, getFieldProps } = useForm(
      { username: '', email: '' }, // 초기값
      {
        username: [
          { validator: 'required', message: '사용자명은 필수입니다' },
          { validator: min(4), message: '4자 이상 입력하세요' }
        ],
        email: [
          { validator: 'required' },
          { validator: 'email' }
        ]
      }
    );
    
    const onSubmit = handleSubmit(async (formData) => {
      // 폼 제출 로직
      await api.createUser(formData);
      success('사용자가 생성되었습니다!');
    });
    
    return {
      form,
      errors,
      onSubmit,
      getFieldProps,
      handleSuccess,
      showConfirmDialog
    };
  }
};
</script>
```

## 🎯 컴포넌트 특징

### 접근성 (Accessibility)
- WCAG 2.1 AA 준수
- 키보드 네비게이션 지원
- 스크린 리더 호환
- ARIA 속성 적용
- 고대비 모드 지원

### 반응형 디자인
- 모바일 우선 접근법
- 터치 친화적 인터페이스
- 유연한 그리드 시스템
- 적응형 컴포넌트 크기

### 성능 최적화
- 지연 로딩 지원
- 메모이제이션 적용
- 번들 크기 최적화
- 트리 쉐이킹 지원

### 테마 지원
- CSS 변수 기반 테마
- 다크 모드 지원
- 커스터마이징 가능
- 디자인 토큰 시스템

## 📋 개발 가이드라인

### 컴포넌트 개발 원칙
1. **단일 책임**: 각 컴포넌트는 명확한 하나의 역할
2. **재사용성**: 다양한 컨텍스트에서 사용 가능
3. **조합성**: 다른 컴포넌트와 잘 조합됨
4. **확장성**: props와 슬롯을 통한 유연한 확장

### Props 검증
- 모든 props에 타입 검증 적용
- 필수 props와 기본값 명시
- validator 함수로 값 검증
- 명확한 prop 문서화

### 이벤트 명명
- kebab-case 사용 (`update:model-value`)
- 명확한 이벤트 이름
- payload 구조 일관성
- emit 선언 필수

### 스타일 가이드
- CSS 변수 사용 권장
- 유틸리티 클래스 활용
- 컴포넌트별 scoped 스타일
- 반응형 고려

## 🔧 확장 계획

### Phase 2 (개발 예정)
- 레이아웃 컴포넌트 (PageHeader, Sidebar, GridContainer)
- 피드백 컴포넌트 (ToastNotification, AlertMessage, ConfirmDialog)
- 데이터 표시 컴포넌트 (MetricCard, StatusIndicator, EmptyState)
- 네비게이션 컴포넌트 (Breadcrumb, Pagination, TabNav)
- 유틸리티 컴포넌트 (DatePicker, SearchInput, FilterDropdown)

### Phase 3 (고도화)
- 차트 컴포넌트 완성
- 복잡한 데이터 그리드
- 드래그 앤 드롭 컴포넌트
- 가상화 리스트
- 고급 폼 컴포넌트

## 🤝 기여하기

### 새 컴포넌트 추가
1. 적절한 카테고리 디렉토리에 컴포넌트 생성
2. Props 검증 및 이벤트 정의
3. 접근성 고려사항 구현
4. `index.js`에 export 추가
5. 사용 예시 문서 작성

### 버그 리포트 및 개선사항
- 명확한 재현 방법 기술
- 예상 동작과 실제 동작 비교
- 브라우저 및 환경 정보 포함
- 가능한 해결책 제시

---

**이 컴포넌트 라이브러리는 exam-center-performance 프로젝트의 모든 Vue 앱에서 일관된 UI/UX를 제공하고 개발 효율성을 높이기 위해 설계되었습니다.**