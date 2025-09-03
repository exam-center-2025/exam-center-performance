import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'

// AIDEV-NOTE: Monitor app initialization with testId support

// Import styles conditionally - skip for now as styles are in HTML

// Get testId from global config or URL
const getTestId = () => {
  if (window.MONITOR_CONFIG && window.MONITOR_CONFIG.testId) {
    return window.MONITOR_CONFIG.testId
  }
  
  // Extract from URL query parameters
  const urlParams = new URLSearchParams(window.location.search)
  const testIdParam = urlParams.get('testId')
  if (testIdParam) {
    return testIdParam
  }
  
  // Fallback: extract from URL path
  const pathParts = window.location.pathname.split('/')
  const testId = pathParts[pathParts.length - 1]
  
  // Handle different URL patterns
  if (testId && testId !== 'monitor' && !testId.includes('.html')) {
    return testId
  }
  
  return 'unknown'
}

const testId = getTestId()
console.log('Extracted testId:', testId)

// Create Vue app with testId prop
const app = createApp(App, {
  testId: testId
})

// Use Pinia for state management
const pinia = createPinia()
app.use(pinia)

// Global error handler
app.config.errorHandler = (error, instance, info) => {
  console.error('Vue Monitor App Error:', error)
  console.error('Component:', instance)
  console.error('Error Info:', info)
}

// Mount app
app.mount('#monitor-app')

console.log('Vue Monitor App initialized for testId:', testId)

// Expose app for debugging in development
if (import.meta.env.DEV) {
  window.monitorVueApp = app
  window.monitorTestId = testId
}