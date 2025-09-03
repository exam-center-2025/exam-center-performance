/**
 * 공통 컴포넌트 인덱스
 * AIDEV-NOTE: 모든 공통 컴포넌트를 중앙에서 관리하고 자동 등록
 */

// UI 컴포넌트
import Button from './ui/Button.vue';
import BaseButton from './ui/BaseButton.vue';
import Card from './ui/Card.vue';
import BaseCard from './ui/BaseCard.vue';
import BaseModal from './ui/BaseModal.vue';
import BaseTable from './ui/BaseTable.vue';
import BaseBadge from './ui/BaseBadge.vue';
import BaseTooltip from './ui/BaseTooltip.vue';
import BaseSpinner from './ui/BaseSpinner.vue';
import BaseSkeleton from './ui/BaseSkeleton.vue';

// 폼 컴포넌트
import BaseInput from './form/BaseInput.vue';
import BaseSelect from './form/BaseSelect.vue';
import BaseCheckbox from './form/BaseCheckbox.vue';
import BaseRadio from './form/BaseRadio.vue';
import BaseTextarea from './form/BaseTextarea.vue';
import BaseSwitch from './form/BaseSwitch.vue';
import FormGroup from './form/FormGroup.vue';

// 차트 컴포넌트
import LineChart from './charts/LineChart.vue';

// 컴포넌트 맵
const components = {
  // UI 컴포넌트
  Button,
  BaseButton,
  Card,
  BaseCard,
  BaseModal,
  BaseTable,
  BaseBadge,
  BaseTooltip,
  BaseSpinner,
  BaseSkeleton,
  
  // 폼 컴포넌트
  BaseInput,
  BaseSelect,
  BaseCheckbox,
  BaseRadio,
  BaseTextarea,
  BaseSwitch,
  FormGroup,
  
  // 차트 컴포넌트
  LineChart
};

// Vue 앱에 컴포넌트 등록하는 함수
const install = (app) => {
  Object.keys(components).forEach(name => {
    app.component(name, components[name]);
  });
};

// 개별 컴포넌트들도 export
export {
  // UI 컴포넌트
  Button,
  BaseButton,
  Card,
  BaseCard,
  BaseModal,
  BaseTable,
  BaseBadge,
  BaseTooltip,
  BaseSpinner,
  BaseSkeleton,
  
  // 폼 컴포넌트
  BaseInput,
  BaseSelect,
  BaseCheckbox,
  BaseRadio,
  BaseTextarea,
  BaseSwitch,
  FormGroup,
  
  // 차트 컴포넌트
  LineChart
};

// 플러그인으로 사용할 수 있도록 default export
export default {
  install
};

/**
 * 사용 예시:
 * 
 * 1. 전체 등록:
 * import SharedComponents from '@/shared/components';
 * app.use(SharedComponents);
 * 
 * 2. 개별 사용:
 * import { BaseButton, BaseModal } from '@/shared/components';
 * 
 * 3. 특정 그룹만 등록:
 * import { BaseButton, BaseInput, BaseModal } from '@/shared/components';
 * app.component('BaseButton', BaseButton);
 * app.component('BaseInput', BaseInput);
 * app.component('BaseModal', BaseModal);
 */