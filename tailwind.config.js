/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './src/main/resources/templates/**/*.html',
    './src/main/resources/static/**/*.js',
    './src/main/resources/static/**/*.html'
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#eef2ff',
          100: '#e0e7ff',
          500: '#6366f1',
          600: '#4f46e5',
          700: '#4338ca',
        },
        gray: {
          50: '#f9fafb',
          100: '#f3f4f6',
          200: '#e5e7eb',
          300: '#d1d5db',
          400: '#9ca3af',
          500: '#6b7280',
          600: '#4b5563',
          700: '#374151',
          800: '#1f2937',
          900: '#111827',
        },
        success: {
          50: '#f0fdf4',
          500: '#10b981',
          600: '#059669',
        },
        warning: {
          50: '#fffbeb',
          500: '#f59e0b',
          600: '#d97706',
        },
        danger: {
          50: '#fef2f2',
          500: '#ef4444',
          600: '#dc2626',
        },
        info: {
          50: '#eff6ff',
          500: '#3b82f6',
          600: '#2563eb',
        }
      },
      fontFamily: {
        sans: ['Noto Sans KR', 'Inter', 'ui-sans-serif', 'system-ui'],
        mono: ['Monaco', 'Menlo', 'Ubuntu Mono', 'monospace']
      },
      fontSize: {
        'xs': '0.75rem',    // 12px
        'sm': '0.875rem',   // 14px
        'base': '1rem',     // 16px
        'lg': '1.125rem',   // 18px
        'xl': '1.25rem',    // 20px
        '2xl': '1.5rem',    // 24px
        '3xl': '1.875rem',  // 30px
      },
      spacing: {
        '18': '4.5rem',     // 72px
        '88': '22rem',      // 352px
      },
      borderRadius: {
        'none': '0',
        'sm': '0.125rem',   // 2px
        'md': '0.25rem',    // 4px - 기본값
      },
      boxShadow: {
        'none': 'none',
      },
      animation: {
        'pulse': 'pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'spin': 'spin 0.6s linear infinite',
      }
    },
  },
  plugins: [],
  corePlugins: {
    // 불필요한 기능들 비활성화
    gradientColorStops: false,
    backdropOpacity: false,
    backdropSaturate: false,
    backdropSepia: false,
  },
  // 플랫 디자인을 위한 기본값 재정의
  safelist: [
    // 상태 관련 클래스들
    'bg-success-50', 'text-success-600', 'border-success-200',
    'bg-warning-50', 'text-warning-600', 'border-warning-200',
    'bg-danger-50', 'text-danger-600', 'border-danger-200',
    'bg-info-50', 'text-info-600', 'border-info-200',
    'bg-primary-50', 'text-primary-600', 'border-primary-200',
  ]
}