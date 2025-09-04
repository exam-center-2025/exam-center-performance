import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useWebSocket } from './useWebSocket'
import { usePerformanceStore } from '../stores/performance'
import performanceApi from '../services/performanceApi'

// AIDEV-NOTE: Monitoring composable for real-time test monitoring
export function useMonitoring(testId) {
  const performanceStore = usePerformanceStore()
  const ws = useWebSocket()
  
  const isMonitoring = ref(false)
  const error = ref(null)
  const reconnectAttempts = ref(0)
  const maxReconnectAttempts = ref(10)
  
  // Subscription references for cleanup
  const subscriptions = ref(new Map())
  
  // Auto-reconnect timer
  let reconnectTimer = null
  
  // Connection status
  const connectionStatus = computed(() => {
    if (ws.connecting.value) return 'connecting'
    if (ws.connected.value) return 'connected'
    return 'disconnected'
  })
  
  // Start monitoring
  async function startMonitoring() {
    if (isMonitoring.value) {
      console.log('Monitoring already active for test:', testId)
      return
    }
    
    console.log('Starting monitoring for test:', testId)
    isMonitoring.value = true
    error.value = null
    
    try {
      // Connect WebSocket if not connected
      if (!ws.connected.value) {
        await connectWebSocket()
      }
      
      // Subscribe to test channels
      await subscribeToTestChannels()
      
      // Load initial data
      await loadInitialData()
      
      console.log('Monitoring started successfully')
    } catch (err) {
      console.error('Failed to start monitoring:', err)
      error.value = err.message
      isMonitoring.value = false
      throw err
    }
  }
  
  // Stop monitoring
  function stopMonitoring() {
    console.log('Stopping monitoring for test:', testId)
    isMonitoring.value = false
    
    // Unsubscribe from all channels
    unsubscribeFromAll()
    
    // Clear reconnect timer
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    
    // Clear monitoring data
    performanceStore.clearMonitoringData()
    
    console.log('Monitoring stopped')
  }
  
  // WebSocket connection
  async function connectWebSocket() {
    return new Promise((resolve, reject) => {
      ws.connect()
      
      // Wait for connection with timeout
      const timeout = setTimeout(() => {
        reject(new Error('WebSocket connection timeout'))
      }, 10000)
      
      const checkConnection = () => {
        if (ws.connected.value) {
          clearTimeout(timeout)
          reconnectAttempts.value = 0
          resolve()
        } else if (ws.error.value) {
          clearTimeout(timeout)
          reject(new Error(ws.error.value))
        } else {
          setTimeout(checkConnection, 100)
        }
      }
      
      checkConnection()
    })
  }
  
  // Subscribe to test channels
  async function subscribeToTestChannels() {
    if (!ws.connected.value) {
      throw new Error('WebSocket not connected')
    }
    
    // Subscribe to metrics
    const metricsSubscription = ws.subscribe(
      `/topic/metrics/${testId}`,
      handleMetricsMessage
    )
    
    // Subscribe to status
    const statusSubscription = ws.subscribe(
      `/topic/status/${testId}`,
      handleStatusMessage
    )
    
    // Subscribe to logs
    const logsSubscription = ws.subscribe(
      `/topic/logs/${testId}`,
      handleLogMessage
    )
    
    // Store subscriptions for cleanup
    subscriptions.value.set('metrics', metricsSubscription)
    subscriptions.value.set('status', statusSubscription)
    subscriptions.value.set('logs', logsSubscription)
    
    console.log('Subscribed to test channels:', testId)
  }
  
  // Unsubscribe from all channels
  function unsubscribeFromAll() {
    subscriptions.value.forEach((subscription, channel) => {
      if (subscription) {
        subscription.unsubscribe()
        console.log('Unsubscribed from:', channel)
      }
    })
    subscriptions.value.clear()
  }
  
  // Load initial data
  async function loadInitialData() {
    try {
      // Load recent metrics history
      const historyPromise = performanceApi.getTestMetricsHistory(testId, 300)
        .then(history => {
          if (history && history.length > 0) {
            history.forEach(metric => {
              performanceStore.updateMetrics(metric)
            })
          }
        })
        .catch(err => {
          console.warn('Failed to load metrics history:', err)
        })
      
      // Load test status
      const statusPromise = performanceApi.getTestStatus(testId)
        .then(status => {
          if (status) {
            performanceStore.updateTestStatus(status)
          }
        })
        .catch(err => {
          console.warn('Failed to load test status:', err)
        })
      
      // Load recent logs
      const logsPromise = performanceApi.getTestLogs(testId, 50)
        .then(logs => {
          if (logs && logs.length > 0) {
            // Add logs in reverse order (newest first)
            logs.reverse().forEach(log => {
              performanceStore.addLogEntry(log)
            })
          }
        })
        .catch(err => {
          console.warn('Failed to load test logs:', err)
        })
      
      // Wait for all initial data to load (but don't fail if some fail)
      await Promise.allSettled([historyPromise, statusPromise, logsPromise])
      
    } catch (err) {
      console.warn('Some initial data failed to load:', err)
      // Don't throw here - monitoring can continue without initial data
    }
  }
  
  // Message handlers
  function handleMetricsMessage(data) {
    try {
      console.debug('Received metrics:', data)
      // AIDEV-NOTE: Ensure errorCount and totalRequests are included in metrics
      const enrichedMetrics = {
        ...data,
        errorCount: data.errorCount ?? data.errors ?? 0,
        totalRequests: data.totalRequests ?? data.requests ?? 0
      }
      performanceStore.updateMetrics(enrichedMetrics)
    } catch (err) {
      console.error('Failed to handle metrics message:', err)
    }
  }
  
  function handleStatusMessage(data) {
    try {
      console.debug('Received status:', data)
      performanceStore.updateTestStatus(data)
      
      // Auto-stop monitoring if test is completed
      if (data.status && ['COMPLETED', 'FAILED', 'CANCELLED', 'STOPPED'].includes(data.status)) {
        setTimeout(() => {
          stopMonitoring()
        }, 5000) // Wait 5 seconds before stopping
      }
    } catch (err) {
      console.error('Failed to handle status message:', err)
    }
  }
  
  function handleLogMessage(data) {
    try {
      console.debug('Received log:', data)
      performanceStore.addLogEntry(data)
    } catch (err) {
      console.error('Failed to handle log message:', err)
    }
  }
  
  // Auto-reconnect logic
  function attemptReconnect() {
    if (reconnectAttempts.value >= maxReconnectAttempts.value) {
      console.error('Max reconnect attempts reached')
      error.value = 'Connection failed after maximum retry attempts'
      return
    }
    
    reconnectAttempts.value++
    console.log(`Attempting to reconnect (${reconnectAttempts.value}/${maxReconnectAttempts.value})...`)
    
    reconnectTimer = setTimeout(async () => {
      try {
        await connectWebSocket()
        await subscribeToTestChannels()
        console.log('Reconnected successfully')
      } catch (err) {
        console.error('Reconnect failed:', err)
        attemptReconnect() // Try again
      }
    }, Math.min(5000 * reconnectAttempts.value, 30000)) // Exponential backoff, max 30s
  }
  
  // Watch for connection loss and auto-reconnect
  function watchConnection() {
    // This would be better with a proper watcher, but for simplicity using polling
    const checkInterval = setInterval(() => {
      if (isMonitoring.value && !ws.connected.value && !ws.connecting.value) {
        console.warn('WebSocket connection lost, attempting reconnect...')
        attemptReconnect()
      }
    }, 5000)
    
    // Cleanup on unmount
    onUnmounted(() => {
      clearInterval(checkInterval)
    })
  }
  
  // Lifecycle hooks
  onMounted(() => {
    watchConnection()
  })
  
  onUnmounted(() => {
    stopMonitoring()
    ws.disconnect()
    
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
    }
  })
  
  return {
    // State
    isMonitoring,
    error,
    reconnectAttempts,
    
    // Connection status
    connected: ws.connected,
    connecting: ws.connecting,
    connectionStatus,
    
    // Methods
    startMonitoring,
    stopMonitoring,
    
    // Direct access to store data
    currentMetrics: computed(() => performanceStore.currentMetrics),
    metricsHistory: computed(() => performanceStore.metricsHistory),
    testStatus: computed(() => performanceStore.testStatus),
    logEntries: computed(() => performanceStore.logEntries)
  }
}