/**
 * useModal Composable
 * AIDEV-NOTE: 모달 상태 관리, 프로그래밍 방식 모달 제어
 */
import { ref, reactive, computed, nextTick } from 'vue';

// 전역 모달 상태
const modals = reactive(new Map());
let modalId = 0;

export function useModal(initialId = null) {
  const id = initialId || `modal-${++modalId}`;
  
  // 개별 모달 상태
  const isOpen = computed(() => modals.get(id)?.isOpen || false);
  const props = computed(() => modals.get(id)?.props || {});
  const component = computed(() => modals.get(id)?.component || null);

  /**
   * 모달 열기
   */
  const open = async (modalComponent = null, modalProps = {}) => {
    modals.set(id, {
      isOpen: true,
      component: modalComponent,
      props: modalProps,
      id
    });

    await nextTick();
    
    return new Promise((resolve, reject) => {
      modals.get(id).resolve = resolve;
      modals.get(id).reject = reject;
    });
  };

  /**
   * 모달 닫기
   */
  const close = (result = null) => {
    const modal = modals.get(id);
    if (modal) {
      modal.isOpen = false;
      if (modal.resolve) {
        modal.resolve(result);
      }
      
      // 애니메이션 시간 후 완전 제거
      setTimeout(() => {
        modals.delete(id);
      }, 300);
    }
  };

  /**
   * 모달 거부 (취소)
   */
  const cancel = (reason = 'cancelled') => {
    const modal = modals.get(id);
    if (modal) {
      modal.isOpen = false;
      if (modal.reject) {
        modal.reject(new Error(reason));
      }
      
      setTimeout(() => {
        modals.delete(id);
      }, 300);
    }
  };

  /**
   * 모달 토글
   */
  const toggle = () => {
    if (isOpen.value) {
      close();
    } else {
      open();
    }
  };

  /**
   * 모달 프로퍼티 업데이트
   */
  const updateProps = (newProps) => {
    const modal = modals.get(id);
    if (modal) {
      modal.props = { ...modal.props, ...newProps };
    }
  };

  return {
    // 상태
    id,
    isOpen,
    props,
    component,
    
    // 메서드
    open,
    close,
    cancel,
    toggle,
    updateProps
  };
}

/**
 * 전역 모달 관리자
 */
export function useModalManager() {
  const activeModals = computed(() => Array.from(modals.values()));
  const hasActiveModals = computed(() => modals.size > 0);

  /**
   * 모든 모달 닫기
   */
  const closeAll = () => {
    modals.forEach((modal) => {
      modal.isOpen = false;
      if (modal.resolve) {
        modal.resolve(null);
      }
    });
    
    setTimeout(() => {
      modals.clear();
    }, 300);
  };

  /**
   * 특정 모달 찾기
   */
  const findModal = (modalId) => {
    return modals.get(modalId);
  };

  /**
   * 모달 스택 관리
   */
  const getTopModal = () => {
    const modalArray = Array.from(modals.values());
    return modalArray[modalArray.length - 1];
  };

  return {
    // 상태
    activeModals,
    hasActiveModals,
    modals,
    
    // 메서드
    closeAll,
    findModal,
    getTopModal
  };
}

/**
 * 확인 모달 유틸리티
 */
export function useConfirm() {
  const { open } = useModal();

  const confirm = (options = {}) => {
    const defaultOptions = {
      title: '확인',
      message: '정말로 진행하시겠습니까?',
      confirmText: '확인',
      cancelText: '취소',
      variant: 'default'
    };

    return open('ConfirmDialog', {
      ...defaultOptions,
      ...options
    });
  };

  const confirmDelete = (options = {}) => {
    return confirm({
      title: '삭제 확인',
      message: '삭제된 항목은 복구할 수 없습니다. 정말로 삭제하시겠습니까?',
      confirmText: '삭제',
      variant: 'danger',
      ...options
    });
  };

  return {
    confirm,
    confirmDelete
  };
}

/**
 * 알림 모달 유틸리티
 */
export function useAlert() {
  const { open } = useModal();

  const alert = (message, options = {}) => {
    const defaultOptions = {
      title: '알림',
      confirmText: '확인',
      showCancel: false
    };

    return open('AlertDialog', {
      message,
      ...defaultOptions,
      ...options
    });
  };

  const alertError = (message, options = {}) => {
    return alert(message, {
      title: '오류',
      variant: 'danger',
      ...options
    });
  };

  const alertSuccess = (message, options = {}) => {
    return alert(message, {
      title: '성공',
      variant: 'success',
      ...options
    });
  };

  return {
    alert,
    alertError,
    alertSuccess
  };
}