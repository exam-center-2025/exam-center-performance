/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./dashboard.html",
    "./monitor.html",
    "./src/**/*.{vue,js,jsx}"
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Pretendard Variable', 'IBM Plex Sans KR', 'Inter', '-apple-system', 'BlinkMacSystemFont', 'sans-serif'],
        inter: ['Inter', 'SF Mono', 'Monaco', 'monospace'],
        pretendard: ['Pretendard Variable', 'IBM Plex Sans KR', 'sans-serif'],
        ibm: ['IBM Plex Sans KR', 'Pretendard Variable', 'sans-serif'],
        mono: ['Inter', 'SF Mono', 'Monaco', 'Consolas', 'monospace'],
      },
      fontSize: {
        'xs': ['0.6875rem', { lineHeight: '1.2' }],
        'sm': ['0.75rem', { lineHeight: '1.3' }],
        'base': ['0.875rem', { lineHeight: '1.4' }],
        'lg': ['1rem', { lineHeight: '1.4' }],
        'xl': ['1.125rem', { lineHeight: '1.3' }],
        '2xl': ['1.25rem', { lineHeight: '1.25' }],
      },
      spacing: {
        '0.5': '0.125rem',
        '1': '0.25rem',
        '1.5': '0.375rem',
        '2': '0.5rem',
        '2.5': '0.625rem',
        '3': '0.75rem',
        '3.5': '0.875rem',
        '4': '1rem',
      },
      colors: {
        primary: {
          50: '#eff6ff',
          100: '#dbeafe',
          200: '#bfdbfe',
          300: '#93c5fd',
          400: '#60a5fa',
          500: '#3b82f6',
          600: '#2563eb',
          700: '#1d4ed8',
          800: '#1e40af',
          900: '#1e3a8a',
        }
      }
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
  ],
}