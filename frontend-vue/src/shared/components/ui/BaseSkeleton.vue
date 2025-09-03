<!--
  BaseSkeleton 컴포넌트
  AIDEV-NOTE: 콘텐츠 로딩 중 스켈레톤 UI, 다양한 형태와 크기 지원
  @example
  <BaseSkeleton type="text" :lines="3" />
  <BaseSkeleton type="card" :animated="true" />
  <BaseSkeleton type="avatar" size="lg" />
-->
<template>
  <div :class="containerClasses" :aria-label="ariaLabel" role="status">
    <!-- 텍스트 스켈레톤 -->
    <div v-if="type === 'text'" class="space-y-2">
      <div
        v-for="line in lines"
        :key="line"
        :class="textLineClasses(line)"
        :style="textLineStyle(line)"
      ></div>
    </div>
    
    <!-- 아바타 스켈레톤 -->
    <div
      v-else-if="type === 'avatar'"
      :class="avatarClasses"
    ></div>
    
    <!-- 이미지 스켈레톤 -->
    <div
      v-else-if="type === 'image'"
      :class="imageClasses"
      :style="imageStyle"
    ></div>
    
    <!-- 버튼 스켈레톤 -->
    <div
      v-else-if="type === 'button'"
      :class="buttonClasses"
    ></div>
    
    <!-- 카드 스켈레톤 -->
    <div v-else-if="type === 'card'" :class="cardClasses">
      <!-- 카드 이미지 -->
      <div v-if="cardImage" class="skeleton-shimmer rounded-t-lg h-48 mb-4"></div>
      
      <!-- 카드 헤더 -->
      <div class="space-y-2 mb-4">
        <div class="skeleton-shimmer h-6 rounded" :style="{ width: '75%' }"></div>
        <div class="skeleton-shimmer h-4 rounded" :style="{ width: '50%' }"></div>
      </div>
      
      <!-- 카드 내용 -->
      <div class="space-y-2">
        <div class="skeleton-shimmer h-4 rounded"></div>
        <div class="skeleton-shimmer h-4 rounded" :style="{ width: '80%' }"></div>
        <div class="skeleton-shimmer h-4 rounded" :style="{ width: '60%' }"></div>
      </div>
    </div>
    
    <!-- 리스트 스켈레톤 -->
    <div v-else-if="type === 'list'" class="space-y-3">
      <div
        v-for="item in listItems"
        :key="item"
        class="flex items-center space-x-3"
      >
        <!-- 아바타 -->
        <div v-if="listAvatar" class="skeleton-shimmer w-10 h-10 rounded-full flex-shrink-0"></div>
        
        <!-- 내용 -->
        <div class="flex-1 space-y-2">
          <div class="skeleton-shimmer h-4 rounded" :style="{ width: '70%' }"></div>
          <div class="skeleton-shimmer h-3 rounded" :style="{ width: '40%' }"></div>
        </div>
      </div>
    </div>
    
    <!-- 테이블 스켈레톤 -->
    <div v-else-if="type === 'table'" class="space-y-3">
      <!-- 헤더 -->
      <div class="flex space-x-4">
        <div
          v-for="col in tableColumns"
          :key="col"
          class="skeleton-shimmer h-5 rounded flex-1"
        ></div>
      </div>
      
      <!-- 행들 -->
      <div
        v-for="row in tableRows"
        :key="row"
        class="flex space-x-4"
      >
        <div
          v-for="col in tableColumns"
          :key="`${row}-${col}`"
          class="skeleton-shimmer h-4 rounded flex-1"
        ></div>
      </div>
    </div>
    
    <!-- 커스텀 스켈레톤 -->
    <div v-else-if="type === 'custom'" :class="customClasses">
      <slot></slot>
    </div>
    
    <!-- 기본 직사각형 -->
    <div v-else :class="defaultClasses" :style="defaultStyle"></div>
  </div>
</template>

<script>
export default {
  name: 'BaseSkeleton',
  props: {
    // 타입
    type: {
      type: String,
      default: 'default',
      validator: (value) => [
        'default', 'text', 'avatar', 'image', 'button', 
        'card', 'list', 'table', 'custom'
      ].includes(value)
    },
    
    // 애니메이션
    animated: {
      type: Boolean,
      default: true
    },
    
    // 크기
    size: {
      type: String,
      default: 'default',
      validator: (value) => ['xs', 'sm', 'default', 'lg', 'xl'].includes(value)
    },
    
    // 텍스트 스켈레톤 옵션
    lines: {
      type: Number,
      default: 3
    },
    lastLineWidth: {
      type: String,
      default: '60%'
    },
    
    // 이미지 스켈레톤 옵션
    width: {
      type: String,
      default: '100%'
    },
    height: {
      type: String,
      default: '200px'
    },
    
    // 카드 스켈레톤 옵션
    cardImage: {
      type: Boolean,
      default: true
    },
    
    // 리스트 스켈레톤 옵션
    listItems: {
      type: Number,
      default: 3
    },
    listAvatar: {
      type: Boolean,
      default: true
    },
    
    // 테이블 스켈레톤 옵션
    tableRows: {
      type: Number,
      default: 5
    },
    tableColumns: {
      type: Number,
      default: 4
    },
    
    // 모양
    shape: {
      type: String,
      default: 'rounded',
      validator: (value) => ['rounded', 'square', 'circle'].includes(value)
    },
    
    // 커스텀 클래스
    customClass: {
      type: String,
      default: ''
    },
    
    // 접근성
    ariaLabel: {
      type: String,
      default: '콘텐츠를 불러오는 중...'
    }
  },
  
  computed: {
    containerClasses() {
      const classes = ['base-skeleton'];
      
      if (this.animated) {
        classes.push('animate-pulse');
      }
      
      return classes.join(' ');
    },
    
    baseSkeletonClasses() {
      const classes = ['skeleton-shimmer'];
      
      // 모양
      switch (this.shape) {
        case 'circle':
          classes.push('rounded-full');
          break;
        case 'square':
          classes.push('rounded-none');
          break;
        default:
          classes.push('rounded');
      }
      
      return classes;
    },
    
    avatarClasses() {
      const classes = [...this.baseSkeletonClasses, 'rounded-full'];
      
      // 크기
      switch (this.size) {
        case 'xs':
          classes.push('w-6', 'h-6');
          break;
        case 'sm':
          classes.push('w-8', 'h-8');
          break;
        case 'lg':
          classes.push('w-16', 'h-16');
          break;
        case 'xl':
          classes.push('w-20', 'h-20');
          break;
        default:
          classes.push('w-12', 'h-12');
      }
      
      return classes.join(' ');
    },
    
    imageClasses() {
      const classes = [...this.baseSkeletonClasses];
      
      return classes.join(' ');
    },
    
    imageStyle() {
      return {
        width: this.width,
        height: this.height
      };
    },
    
    buttonClasses() {
      const classes = [...this.baseSkeletonClasses];
      
      // 크기
      switch (this.size) {
        case 'xs':
          classes.push('h-6', 'px-2', 'w-16');
          break;
        case 'sm':
          classes.push('h-8', 'px-3', 'w-20');
          break;
        case 'lg':
          classes.push('h-12', 'px-6', 'w-32');
          break;
        case 'xl':
          classes.push('h-14', 'px-8', 'w-40');
          break;
        default:
          classes.push('h-10', 'px-4', 'w-24');
      }
      
      return classes.join(' ');
    },
    
    cardClasses() {
      const classes = ['p-6', 'border', 'rounded-lg', 'bg-white'];
      
      return classes.join(' ');
    },
    
    customClasses() {
      const classes = [];
      
      if (this.customClass) {
        classes.push(this.customClass);
      }
      
      return classes.join(' ');
    },
    
    defaultClasses() {
      const classes = [...this.baseSkeletonClasses, 'h-4'];
      
      return classes.join(' ');
    },
    
    defaultStyle() {
      return {
        width: this.width,
        height: this.type === 'default' ? this.height : undefined
      };
    }
  },
  
  methods: {
    textLineClasses(line) {
      const classes = [...this.baseSkeletonClasses];
      
      // 크기
      switch (this.size) {
        case 'xs':
          classes.push('h-3');
          break;
        case 'sm':
          classes.push('h-3.5');
          break;
        case 'lg':
          classes.push('h-5');
          break;
        case 'xl':
          classes.push('h-6');
          break;
        default:
          classes.push('h-4');
      }
      
      return classes.join(' ');
    },
    
    textLineStyle(line) {
      const style = {};
      
      // 마지막 줄은 다른 너비
      if (line === this.lines) {
        style.width = this.lastLineWidth;
      }
      
      // 랜덤한 너비 (더 자연스러운 느낌)
      if (line > 1 && line < this.lines) {
        const widths = ['95%', '85%', '90%', '75%', '80%'];
        style.width = widths[line % widths.length];
      }
      
      return style;
    }
  }
};
</script>

<style scoped>
.skeleton-shimmer {
  @apply bg-gray-200;
  background: linear-gradient(
    90deg,
    #f0f0f0 0%,
    #e0e0e0 50%,
    #f0f0f0 100%
  );
  background-size: 200% 100%;
}

/* 커스텀 애니메이션 (기본 animate-pulse 대신 사용 가능) */
.skeleton-wave .skeleton-shimmer {
  animation: wave 2s infinite ease-in-out;
}

@keyframes wave {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* 다크 모드 지원 */
@media (prefers-color-scheme: dark) {
  .skeleton-shimmer {
    @apply bg-gray-700;
    background: linear-gradient(
      90deg,
      #374151 0%,
      #4b5563 50%,
      #374151 100%
    );
  }
}

/* 접근성 - 모션 감소 선호 시 애니메이션 비활성화 */
@media (prefers-reduced-motion: reduce) {
  .animate-pulse {
    animation: none;
  }
  
  .skeleton-wave .skeleton-shimmer {
    animation: none;
    background: #f0f0f0;
  }
  
  @media (prefers-color-scheme: dark) {
    .skeleton-wave .skeleton-shimmer {
      background: #374151;
    }
  }
}

/* 높은 대비 모드 지원 */
@media (prefers-contrast: high) {
  .skeleton-shimmer {
    @apply border border-gray-400;
  }
}

/* 반응형 조정 */
@media (max-width: 640px) {
  .base-skeleton {
    font-size: 0.875rem;
  }
  
  /* 모바일에서 카드 패딩 조정 */
  .skeleton-card {
    @apply p-4;
  }
}

/* 포커스 표시 (접근성) */
.base-skeleton:focus-within {
  @apply outline-2 outline-blue-500;
}
</style>