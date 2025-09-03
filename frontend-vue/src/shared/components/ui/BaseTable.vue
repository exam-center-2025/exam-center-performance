<!--
  BaseTable 컴포넌트
  AIDEV-NOTE: 정렬, 페이지네이션, 선택 기능을 지원하는 테이블 컴포넌트
  @example
  <BaseTable 
    :columns="columns" 
    :data="data" 
    :sortable="true" 
    :selectable="true"
    @sort="handleSort"
    @select="handleSelect"
  >
    <template #actions="{ row }">
      <button @click="editRow(row)">편집</button>
    </template>
  </BaseTable>
-->
<template>
  <div class="base-table-container">
    <!-- 테이블 헤더 (제목, 검색, 액션) -->
    <div v-if="$slots.header || title" class="table-header mb-4">
      <div class="flex items-center justify-between">
        <div>
          <h3 v-if="title" class="text-lg font-semibold text-gray-900">{{ title }}</h3>
          <p v-if="description" class="text-sm text-gray-500 mt-1">{{ description }}</p>
        </div>
        <div class="flex items-center space-x-3">
          <slot name="header"></slot>
        </div>
      </div>
    </div>

    <!-- 테이블 컨테이너 -->
    <div class="table-wrapper" :class="wrapperClasses">
      <div class="overflow-hidden shadow ring-1 ring-black ring-opacity-5 md:rounded-lg">
        <table class="min-w-full divide-y divide-gray-300" :class="tableClasses">
          <!-- 테이블 헤드 -->
          <thead class="bg-gray-50">
            <tr>
              <!-- 선택 체크박스 열 -->
              <th 
                v-if="selectable"
                scope="col"
                class="relative w-12 px-6 sm:w-16 sm:px-8"
              >
                <input
                  type="checkbox"
                  :checked="isAllSelected"
                  :indeterminate="isIndeterminate"
                  class="absolute left-4 top-1/2 -mt-2 h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-600"
                  @change="toggleSelectAll"
                />
              </th>

              <!-- 데이터 열들 -->
              <th
                v-for="column in columns"
                :key="column.key"
                scope="col"
                :class="getHeaderCellClasses(column)"
                @click="handleHeaderClick(column)"
              >
                <div class="flex items-center space-x-1">
                  <span>{{ column.title }}</span>
                  
                  <!-- 정렬 아이콘 -->
                  <span v-if="column.sortable && sortable" class="sort-icon">
                    <svg 
                      v-if="sortKey === column.key && sortDirection === 'asc'"
                      class="h-4 w-4 text-gray-400" 
                      fill="currentColor" 
                      viewBox="0 0 20 20"
                    >
                      <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd" />
                    </svg>
                    <svg 
                      v-else-if="sortKey === column.key && sortDirection === 'desc'"
                      class="h-4 w-4 text-gray-400" 
                      fill="currentColor" 
                      viewBox="0 0 20 20"
                    >
                      <path fill-rule="evenodd" d="M14.707 12.707a1 1 0 01-1.414 0L10 9.414l-3.293 3.293a1 1 0 01-1.414-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 010 1.414z" clip-rule="evenodd" />
                    </svg>
                    <svg 
                      v-else
                      class="h-4 w-4 text-gray-300" 
                      fill="currentColor" 
                      viewBox="0 0 20 20"
                    >
                      <path d="M5 12a1 1 0 102 0V6.414l1.293 1.293a1 1 0 001.414-1.414l-3-3a1 1 0 00-1.414 0l-3 3a1 1 0 001.414 1.414L5 6.414V12zM15 8a1 1 0 10-2 0v5.586l-1.293-1.293a1 1 0 00-1.414 1.414l3 3a1 1 0 001.414 0l3-3a1 1 0 00-1.414-1.414L15 13.586V8z" />
                    </svg>
                  </span>
                </div>
              </th>

              <!-- 액션 열 -->
              <th v-if="$slots.actions" scope="col" class="relative py-3.5 pl-3 pr-4 sm:pr-6">
                <span class="sr-only">액션</span>
              </th>
            </tr>
          </thead>

          <!-- 테이블 바디 -->
          <tbody class="divide-y divide-gray-200 bg-white">
            <!-- 로딩 상태 -->
            <tr v-if="loading">
              <td :colspan="totalColumns" class="py-12 text-center">
                <div class="flex items-center justify-center">
                  <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                  <span class="ml-3 text-sm text-gray-500">{{ loadingText || '데이터를 불러오는 중...' }}</span>
                </div>
              </td>
            </tr>

            <!-- 빈 상태 -->
            <tr v-else-if="!data || data.length === 0">
              <td :colspan="totalColumns" class="py-12 text-center">
                <div class="text-gray-500">
                  <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                  <p class="mt-2 text-sm">{{ emptyText || '데이터가 없습니다.' }}</p>
                </div>
              </td>
            </tr>

            <!-- 데이터 행들 -->
            <tr 
              v-else
              v-for="(row, rowIndex) in data" 
              :key="getRowKey(row, rowIndex)"
              :class="getRowClasses(row, rowIndex)"
              @click="handleRowClick(row, rowIndex)"
            >
              <!-- 선택 체크박스 -->
              <td v-if="selectable" class="relative w-12 px-6 sm:w-16 sm:px-8">
                <input
                  type="checkbox"
                  :checked="isRowSelected(row)"
                  class="absolute left-4 top-1/2 -mt-2 h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-600"
                  @change="toggleRowSelection(row)"
                  @click.stop
                />
              </td>

              <!-- 데이터 셀들 -->
              <td
                v-for="column in columns"
                :key="`${rowIndex}-${column.key}`"
                :class="getCellClasses(column, row)"
              >
                <slot 
                  :name="column.key" 
                  :row="row" 
                  :value="getCellValue(row, column)" 
                  :column="column"
                  :index="rowIndex"
                >
                  <span :class="getCellContentClasses(column, row)">
                    {{ formatCellValue(getCellValue(row, column), column) }}
                  </span>
                </slot>
              </td>

              <!-- 액션 셀 -->
              <td v-if="$slots.actions" class="relative py-4 pl-3 pr-4 text-right text-sm font-medium sm:pr-6">
                <slot name="actions" :row="row" :index="rowIndex"></slot>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 테이블 푸터 (페이지네이션, 정보) -->
    <div v-if="$slots.footer || showPagination" class="table-footer mt-4">
      <div class="flex items-center justify-between">
        <!-- 정보 -->
        <div class="text-sm text-gray-700">
          <slot name="info">
            <span v-if="data && data.length > 0">
              총 {{ total || data.length }}개 중 {{ data.length }}개 표시
            </span>
          </slot>
        </div>

        <!-- 푸터 슬롯 또는 페이지네이션 -->
        <div>
          <slot name="footer"></slot>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BaseTable',
  props: {
    // 데이터
    columns: {
      type: Array,
      required: true,
      validator: (columns) => columns.every(col => col.key && col.title)
    },
    data: {
      type: Array,
      default: () => []
    },
    
    // 기본 속성
    title: {
      type: String,
      default: ''
    },
    description: {
      type: String,
      default: ''
    },
    
    // 기능
    sortable: {
      type: Boolean,
      default: false
    },
    selectable: {
      type: Boolean,
      default: false
    },
    clickableRows: {
      type: Boolean,
      default: false
    },
    
    // 정렬 상태
    sortKey: {
      type: String,
      default: ''
    },
    sortDirection: {
      type: String,
      default: 'asc',
      validator: (value) => ['asc', 'desc'].includes(value)
    },
    
    // 선택된 항목
    selectedItems: {
      type: Array,
      default: () => []
    },
    
    // 상태
    loading: {
      type: Boolean,
      default: false
    },
    loadingText: {
      type: String,
      default: ''
    },
    
    // 텍스트
    emptyText: {
      type: String,
      default: '데이터가 없습니다.'
    },
    
    // 스타일
    size: {
      type: String,
      default: 'default',
      validator: (value) => ['compact', 'default', 'comfortable'].includes(value)
    },
    striped: {
      type: Boolean,
      default: false
    },
    bordered: {
      type: Boolean,
      default: false
    },
    
    // 페이지네이션
    showPagination: {
      type: Boolean,
      default: false
    },
    total: {
      type: Number,
      default: null
    },
    
    // 행 키
    rowKey: {
      type: [String, Function],
      default: 'id'
    }
  },
  
  emits: ['sort', 'select', 'row-click', 'select-all'],
  
  computed: {
    totalColumns() {
      let count = this.columns.length;
      if (this.selectable) count++;
      if (this.$slots.actions) count++;
      return count;
    },
    
    isAllSelected() {
      return this.data && this.data.length > 0 && 
             this.selectedItems.length === this.data.length;
    },
    
    isIndeterminate() {
      return this.selectedItems.length > 0 && 
             this.selectedItems.length < (this.data ? this.data.length : 0);
    },
    
    wrapperClasses() {
      const classes = [];
      
      if (this.bordered) {
        classes.push('border', 'border-gray-200', 'rounded-lg');
      }
      
      return classes.join(' ');
    },
    
    tableClasses() {
      const classes = [];
      
      if (this.striped) {
        classes.push('table-striped');
      }
      
      return classes.join(' ');
    }
  },
  
  methods: {
    getRowKey(row, index) {
      if (typeof this.rowKey === 'function') {
        return this.rowKey(row, index);
      }
      return row[this.rowKey] || index;
    },
    
    getHeaderCellClasses(column) {
      const classes = [
        'py-3.5', 'text-left', 'text-sm', 'font-semibold', 
        'text-gray-900', 'uppercase', 'tracking-wide'
      ];
      
      if (column.sortable && this.sortable) {
        classes.push('cursor-pointer', 'hover:bg-gray-100', 'select-none');
      }
      
      if (column.align === 'center') {
        classes.push('text-center');
      } else if (column.align === 'right') {
        classes.push('text-right');
      }
      
      // 첫 번째 열
      if (this.columns.indexOf(column) === 0) {
        classes.push('pl-6', 'pr-3');
      } else {
        classes.push('px-3');
      }
      
      return classes.join(' ');
    },
    
    getRowClasses(row, index) {
      const classes = [];
      
      if (this.clickableRows) {
        classes.push('cursor-pointer', 'hover:bg-gray-50');
      }
      
      if (this.isRowSelected(row)) {
        classes.push('bg-blue-50');
      }
      
      if (this.striped && index % 2 === 1) {
        classes.push('bg-gray-50');
      }
      
      return classes.join(' ');
    },
    
    getCellClasses(column, row) {
      const classes = ['whitespace-nowrap', 'text-sm'];
      
      // 크기에 따른 패딩
      switch (this.size) {
        case 'compact':
          classes.push('py-2');
          break;
        case 'comfortable':
          classes.push('py-6');
          break;
        default:
          classes.push('py-4');
      }
      
      // 정렬
      if (column.align === 'center') {
        classes.push('text-center');
      } else if (column.align === 'right') {
        classes.push('text-right');
      }
      
      // 첫 번째 열
      if (this.columns.indexOf(column) === 0) {
        classes.push('pl-6', 'pr-3', 'font-medium', 'text-gray-900');
      } else {
        classes.push('px-3', 'text-gray-500');
      }
      
      return classes.join(' ');
    },
    
    getCellContentClasses(column, row) {
      const classes = [];
      
      if (column.truncate) {
        classes.push('truncate', 'max-w-xs');
      }
      
      return classes.join(' ');
    },
    
    getCellValue(row, column) {
      if (column.key.includes('.')) {
        return column.key.split('.').reduce((obj, key) => obj?.[key], row);
      }
      return row[column.key];
    },
    
    formatCellValue(value, column) {
      if (column.formatter && typeof column.formatter === 'function') {
        return column.formatter(value);
      }
      
      if (value == null) {
        return '-';
      }
      
      if (column.type === 'number') {
        return new Intl.NumberFormat('ko-KR').format(value);
      }
      
      if (column.type === 'date') {
        return new Date(value).toLocaleDateString('ko-KR');
      }
      
      if (column.type === 'datetime') {
        return new Date(value).toLocaleString('ko-KR');
      }
      
      if (column.type === 'boolean') {
        return value ? '예' : '아니오';
      }
      
      return String(value);
    },
    
    handleHeaderClick(column) {
      if (column.sortable && this.sortable) {
        let direction = 'asc';
        
        if (this.sortKey === column.key && this.sortDirection === 'asc') {
          direction = 'desc';
        }
        
        this.$emit('sort', {
          key: column.key,
          direction
        });
      }
    },
    
    handleRowClick(row, index) {
      if (this.clickableRows) {
        this.$emit('row-click', { row, index });
      }
    },
    
    isRowSelected(row) {
      const rowKey = this.getRowKey(row);
      return this.selectedItems.some(item => this.getRowKey(item) === rowKey);
    },
    
    toggleRowSelection(row) {
      const isSelected = this.isRowSelected(row);
      let newSelection;
      
      if (isSelected) {
        const rowKey = this.getRowKey(row);
        newSelection = this.selectedItems.filter(item => this.getRowKey(item) !== rowKey);
      } else {
        newSelection = [...this.selectedItems, row];
      }
      
      this.$emit('select', newSelection);
    },
    
    toggleSelectAll() {
      if (this.isAllSelected) {
        this.$emit('select', []);
      } else {
        this.$emit('select', [...this.data]);
      }
      
      this.$emit('select-all', !this.isAllSelected);
    }
  }
};
</script>

<style scoped>
.base-table-container {
  @apply w-full;
}

.table-striped tbody tr:nth-child(even) {
  @apply bg-gray-50;
}

.sort-icon {
  @apply inline-flex;
}

/* 체크박스 indeterminate 상태 */
input[type="checkbox"]:indeterminate {
  @apply bg-blue-600 border-blue-600;
  background-image: url("data:image/svg+xml,%3csvg viewBox='0 0 16 16' fill='white' xmlns='http://www.w3.org/2000/svg'%3e%3cpath d='M5 8h6'/%3e%3c/svg%3e");
}

/* 반응형 스크롤 */
.table-wrapper {
  @apply overflow-x-auto;
}

@media (max-width: 640px) {
  .table-wrapper {
    @apply -mx-4;
  }
}
</style>