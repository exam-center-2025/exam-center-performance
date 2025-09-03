/**
 * useForm Composable
 * AIDEV-NOTE: 폼 상태 관리, 검증, 제출 처리
 */
import { ref, reactive, computed, watch, nextTick } from 'vue';

/**
 * 기본 검증 규칙
 */
const validators = {
  required: (value, message = '필수 입력 항목입니다') => {
    if (Array.isArray(value)) {
      return value.length > 0 || message;
    }
    return (value !== null && value !== undefined && value !== '') || message;
  },
  
  email: (value, message = '올바른 이메일 주소를 입력하세요') => {
    if (!value) return true;
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(value) || message;
  },
  
  min: (minValue, message) => (value, customMessage) => {
    if (!value) return true;
    const msg = customMessage || message || `최소 ${minValue}자 이상 입력하세요`;
    return value.length >= minValue || msg;
  },
  
  max: (maxValue, message) => (value, customMessage) => {
    if (!value) return true;
    const msg = customMessage || message || `최대 ${maxValue}자까지 입력 가능합니다`;
    return value.length <= maxValue || msg;
  },
  
  pattern: (regex, message = '올바른 형식이 아닙니다') => (value, customMessage) => {
    if (!value) return true;
    return regex.test(value) || customMessage || message;
  },
  
  number: (value, message = '숫자만 입력 가능합니다') => {
    if (!value) return true;
    return !isNaN(Number(value)) || message;
  },
  
  phone: (value, message = '올바른 전화번호를 입력하세요') => {
    if (!value) return true;
    const phoneRegex = /^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$/;
    return phoneRegex.test(value.replace(/[^0-9]/g, '')) || message;
  }
};

export function useForm(initialValues = {}, rules = {}) {
  // 폼 데이터
  const form = reactive({ ...initialValues });
  
  // 에러 상태
  const errors = reactive({});
  
  // 터치 상태 (사용자가 필드를 건드렸는지)
  const touched = reactive({});
  
  // 폼 상태
  const isSubmitting = ref(false);
  const isValidating = ref(false);
  const submitCount = ref(0);

  /**
   * 계산된 속성들
   */
  const isValid = computed(() => {
    return Object.keys(errors).every(key => !errors[key]);
  });

  const isDirty = computed(() => {
    return Object.keys(form).some(key => 
      JSON.stringify(form[key]) !== JSON.stringify(initialValues[key])
    );
  });

  const hasErrors = computed(() => {
    return Object.keys(errors).some(key => errors[key]);
  });

  const touchedFields = computed(() => {
    return Object.keys(touched).filter(key => touched[key]);
  });

  /**
   * 필드 검증
   */
  const validateField = async (fieldName) => {
    const fieldRules = rules[fieldName];
    if (!fieldRules) return true;

    const value = form[fieldName];
    const fieldErrors = [];

    // 배열 형태의 규칙 처리
    const rulesArray = Array.isArray(fieldRules) ? fieldRules : [fieldRules];

    for (const rule of rulesArray) {
      let result = true;
      
      if (typeof rule === 'function') {
        result = rule(value);
      } else if (typeof rule === 'string' && validators[rule]) {
        result = validators[rule](value);
      } else if (typeof rule === 'object' && rule.validator) {
        if (typeof rule.validator === 'string' && validators[rule.validator]) {
          result = validators[rule.validator](value, rule.message);
        } else if (typeof rule.validator === 'function') {
          result = rule.validator(value, rule.message);
        }
      }

      // 프로미스 결과 처리
      if (result instanceof Promise) {
        result = await result;
      }

      if (result !== true) {
        fieldErrors.push(result);
        break; // 첫 번째 에러에서 중단
      }
    }

    // 에러 상태 업데이트
    if (fieldErrors.length > 0) {
      errors[fieldName] = fieldErrors[0];
    } else {
      delete errors[fieldName];
    }

    return fieldErrors.length === 0;
  };

  /**
   * 모든 필드 검증
   */
  const validate = async () => {
    isValidating.value = true;
    const validationPromises = Object.keys(rules).map(fieldName => 
      validateField(fieldName)
    );

    const results = await Promise.all(validationPromises);
    isValidating.value = false;
    
    return results.every(result => result);
  };

  /**
   * 필드 값 설정
   */
  const setFieldValue = (fieldName, value) => {
    form[fieldName] = value;
    touched[fieldName] = true;
    
    // 실시간 검증 (이미 터치된 필드에 대해)
    if (touched[fieldName]) {
      nextTick(() => {
        validateField(fieldName);
      });
    }
  };

  /**
   * 필드 에러 설정
   */
  const setFieldError = (fieldName, error) => {
    if (error) {
      errors[fieldName] = error;
    } else {
      delete errors[fieldName];
    }
  };

  /**
   * 필드 터치 상태 설정
   */
  const setFieldTouched = (fieldName, isTouched = true) => {
    touched[fieldName] = isTouched;
  };

  /**
   * 폼 리셋
   */
  const resetForm = (newValues = initialValues) => {
    // 값 리셋
    Object.keys(form).forEach(key => {
      delete form[key];
    });
    Object.assign(form, newValues);

    // 에러 리셋
    Object.keys(errors).forEach(key => {
      delete errors[key];
    });

    // 터치 상태 리셋
    Object.keys(touched).forEach(key => {
      delete touched[key];
    });

    // 상태 리셋
    isSubmitting.value = false;
    submitCount.value = 0;
  };

  /**
   * 폼 제출
   */
  const handleSubmit = async (submitFn) => {
    submitCount.value++;
    
    // 모든 필드를 터치 상태로 설정
    Object.keys(rules).forEach(fieldName => {
      touched[fieldName] = true;
    });

    // 검증 실행
    const isFormValid = await validate();
    
    if (!isFormValid) {
      return;
    }

    try {
      isSubmitting.value = true;
      await submitFn(form);
    } catch (error) {
      // 서버 에러 처리
      if (error.response && error.response.data && error.response.data.errors) {
        Object.keys(error.response.data.errors).forEach(fieldName => {
          setFieldError(fieldName, error.response.data.errors[fieldName]);
        });
      }
      throw error;
    } finally {
      isSubmitting.value = false;
    }
  };

  /**
   * 필드 포커스 아웃 핸들러
   */
  const handleFieldBlur = (fieldName) => {
    setFieldTouched(fieldName, true);
    validateField(fieldName);
  };

  /**
   * 필드별 헬퍼 생성
   */
  const getFieldProps = (fieldName) => {
    return {
      modelValue: form[fieldName],
      error: errors[fieldName],
      'onUpdate:modelValue': (value) => setFieldValue(fieldName, value),
      onBlur: () => handleFieldBlur(fieldName)
    };
  };

  /**
   * 감시자 설정 - 폼 값 변경 시 검증
   */
  Object.keys(rules).forEach(fieldName => {
    watch(
      () => form[fieldName],
      () => {
        if (touched[fieldName]) {
          nextTick(() => {
            validateField(fieldName);
          });
        }
      }
    );
  });

  return {
    // 상태
    form,
    errors,
    touched,
    isSubmitting,
    isValidating,
    submitCount,

    // 계산된 속성
    isValid,
    isDirty,
    hasErrors,
    touchedFields,

    // 메서드
    setFieldValue,
    setFieldError,
    setFieldTouched,
    validateField,
    validate,
    resetForm,
    handleSubmit,
    handleFieldBlur,
    getFieldProps,

    // 유틸리티
    validators
  };
}

/**
 * 배열 필드 관리를 위한 확장
 */
export function useFieldArray(form, fieldName) {
  const fields = computed(() => form[fieldName] || []);

  const append = (value) => {
    if (!form[fieldName]) {
      form[fieldName] = [];
    }
    form[fieldName].push(value);
  };

  const prepend = (value) => {
    if (!form[fieldName]) {
      form[fieldName] = [];
    }
    form[fieldName].unshift(value);
  };

  const insert = (index, value) => {
    if (!form[fieldName]) {
      form[fieldName] = [];
    }
    form[fieldName].splice(index, 0, value);
  };

  const remove = (index) => {
    if (form[fieldName] && form[fieldName].length > index) {
      form[fieldName].splice(index, 1);
    }
  };

  const move = (fromIndex, toIndex) => {
    if (form[fieldName] && form[fieldName].length > Math.max(fromIndex, toIndex)) {
      const item = form[fieldName].splice(fromIndex, 1)[0];
      form[fieldName].splice(toIndex, 0, item);
    }
  };

  const clear = () => {
    form[fieldName] = [];
  };

  return {
    fields,
    append,
    prepend,
    insert,
    remove,
    move,
    clear
  };
}