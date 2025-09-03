/**
 * useToast Composable
 * AIDEV-NOTE: 토스트 알림 관리, 전역 상태로 알림 메시지 제어
 */
import { ref, reactive, nextTick } from 'vue';

// 전역 토스트 상태
const toasts = ref([]);
let toastId = 0;

/**
 * 토스트 타입 정의
 */
const TOAST_TYPES = {
  SUCCESS: 'success',
  ERROR: 'error',
  WARNING: 'warning',
  INFO: 'info'
};

/**
 * 기본 토스트 설정
 */
const DEFAULT_OPTIONS = {
  duration: 4000,
  position: 'top-right',
  closable: true,
  pauseOnHover: true
};

export function useToast() {
  /**
   * 토스트 추가
   */
  const addToast = (message, type = TOAST_TYPES.INFO, options = {}) => {
    const id = ++toastId;
    const toast = {
      id,
      message,
      type,
      createdAt: Date.now(),
      ...DEFAULT_OPTIONS,
      ...options
    };

    toasts.value.push(toast);

    // 자동 제거
    if (toast.duration > 0) {
      setTimeout(() => {
        removeToast(id);
      }, toast.duration);
    }

    return id;
  };

  /**
   * 토스트 제거
   */
  const removeToast = (id) => {
    const index = toasts.value.findIndex(toast => toast.id === id);
    if (index > -1) {
      toasts.value.splice(index, 1);
    }
  };

  /**
   * 모든 토스트 제거
   */
  const clearToasts = () => {
    toasts.value = [];
  };

  /**
   * 성공 토스트
   */
  const success = (message, options = {}) => {
    return addToast(message, TOAST_TYPES.SUCCESS, {
      icon: '✓',
      ...options
    });
  };

  /**
   * 에러 토스트
   */
  const error = (message, options = {}) => {
    return addToast(message, TOAST_TYPES.ERROR, {
      icon: '✕',
      duration: 6000, // 에러는 조금 더 오래 표시
      ...options
    });
  };

  /**
   * 경고 토스트
   */
  const warning = (message, options = {}) => {
    return addToast(message, TOAST_TYPES.WARNING, {
      icon: '⚠',
      ...options
    });
  };

  /**
   * 정보 토스트
   */
  const info = (message, options = {}) => {
    return addToast(message, TOAST_TYPES.INFO, {
      icon: 'ⓘ',
      ...options
    });
  };

  /**
   * 프로미스 기반 토스트
   */
  const promise = async (promise, messages, options = {}) => {
    const loadingId = addToast(messages.loading || '처리 중...', TOAST_TYPES.INFO, {
      duration: 0,
      closable: false,
      ...options
    });

    try {
      const result = await promise;
      removeToast(loadingId);
      success(messages.success || '완료되었습니다.');
      return result;
    } catch (err) {
      removeToast(loadingId);
      error(messages.error || '오류가 발생했습니다.');
      throw err;
    }
  };

  return {
    // 상태
    toasts: readonly(toasts),
    
    // 메서드
    addToast,
    removeToast,
    clearToasts,
    
    // 타입별 메서드
    success,
    error,
    warning,
    info,
    promise,
    
    // 상수
    TOAST_TYPES
  };
}