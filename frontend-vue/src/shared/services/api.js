import axios from 'axios'

// Create axios instance
const api = axios.create({
  baseURL: '/performance/api/dashboard',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor
api.interceptors.request.use(
  config => {
    // Add auth token if exists
    const token = localStorage.getItem('auth-token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// Response interceptor
api.interceptors.response.use(
  response => {
    // Handle wrapped API responses with success/data structure
    if (response.data && response.data.hasOwnProperty('success') && response.data.hasOwnProperty('data')) {
      // If it's a wrapped response, return the inner data
      return response.data.data
    }
    // Otherwise return the full response data
    return response.data
  },
  error => {
    console.error('Response error:', error)
    if (error.response?.status === 401) {
      // Handle unauthorized
      localStorage.removeItem('auth-token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api