import { ref, onMounted, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'

// AIDEV-NOTE: WebSocket composable for real-time communication
export function useWebSocket() {
  const client = ref(null)
  const connected = ref(false)
  const connecting = ref(false)
  const error = ref(null)
  const subscriptions = ref(new Map())

  const connect = (url = '/ws') => {
    if (connecting.value || connected.value) return

    connecting.value = true
    error.value = null

    // 개발 서버에서는 Vite 프록시를 통해 연결
    const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const wsHost = window.location.host // 개발 서버의 호스트 사용 (localhost:5173)
    const wsUrl = import.meta.env.DEV 
      ? `${wsProtocol}//${wsHost}${url}` // 개발: Vite 프록시 사용
      : `${wsProtocol}//${window.location.hostname}:8097/performance${url}` // 프로덕션: 직접 연결
    
    console.log('WebSocket connecting to:', wsUrl)

    client.value = new Client({
      brokerURL: wsUrl,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      
      onConnect: (frame) => {
        console.log('WebSocket connected:', frame)
        connected.value = true
        connecting.value = false
      },
      
      onDisconnect: (frame) => {
        console.log('WebSocket disconnected:', frame)
        connected.value = false
        connecting.value = false
      },
      
      onStompError: (frame) => {
        console.error('STOMP error:', frame)
        error.value = frame.headers['message'] || 'Connection error'
        connecting.value = false
      }
    })

    try {
      client.value.activate()
    } catch (err) {
      console.error('Failed to activate WebSocket:', err)
      error.value = err.message
      connecting.value = false
    }
  }

  const disconnect = () => {
    if (client.value && connected.value) {
      // Unsubscribe all
      subscriptions.value.forEach(sub => sub.unsubscribe())
      subscriptions.value.clear()
      
      client.value.deactivate()
      connected.value = false
    }
  }

  const subscribe = (destination, callback) => {
    if (!client.value || !connected.value) {
      console.warn('WebSocket not connected')
      return null
    }

    try {
      const subscription = client.value.subscribe(destination, (message) => {
        try {
          const data = JSON.parse(message.body)
          callback(data)
        } catch (err) {
          console.error('Failed to parse message:', err)
          callback(message.body)
        }
      })

      subscriptions.value.set(destination, subscription)
      return subscription
    } catch (err) {
      console.error('Failed to subscribe:', err)
      return null
    }
  }

  const unsubscribe = (destination) => {
    const subscription = subscriptions.value.get(destination)
    if (subscription) {
      subscription.unsubscribe()
      subscriptions.value.delete(destination)
    }
  }

  const send = (destination, body) => {
    if (!client.value || !connected.value) {
      console.warn('WebSocket not connected')
      return false
    }

    try {
      client.value.publish({
        destination,
        body: typeof body === 'string' ? body : JSON.stringify(body)
      })
      return true
    } catch (err) {
      console.error('Failed to send message:', err)
      return false
    }
  }

  onUnmounted(() => {
    disconnect()
  })

  return {
    connected,
    connecting,
    error,
    connect,
    disconnect,
    subscribe,
    unsubscribe,
    send
  }
}