<template>
  <div class="bg-white border-b border-gray-200">
    <div class="max-w-4xl mx-auto px-4 py-4">
      <div class="flex justify-between items-center">
        <h1 class="text-2xl font-bold text-gray-900">성능 테스트 설정</h1>
        <div class="flex gap-2">
          <a 
            href="/dashboard" 
            class="inline-flex items-center px-3 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
          >
            <i class="fas fa-arrow-left mr-2"></i>
            대시보드로
          </a>
        </div>
      </div>
      
      <!-- 진행 상태 표시 (선택적) -->
      <div class="mt-4 flex items-center text-sm text-gray-500">
        <i class="fas fa-info-circle mr-2"></i>
        테스트 설정을 완료하고 성능 테스트를 시작할 수 있습니다.
      </div>
    </div>
  </div>
</template>

<script setup>
// AIDEV-NOTE: Configure 페이지 헤더 - 페이지 제목 및 네비게이션
</script>

<style scoped>
/* 필요한 경우 추가 스타일 */
</style>