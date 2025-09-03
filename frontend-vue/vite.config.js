import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  base: '/performance/vue-dist/',
  
  resolve: {
    alias: {
      '@': resolve(__dirname, './src'),
      '@shared': resolve(__dirname, './src/shared'),
      '@dashboard': resolve(__dirname, './src/apps/dashboard'),
      '@monitor': resolve(__dirname, './src/apps/monitor'),
      '@configure': resolve(__dirname, './src/apps/configure'),
      '@results': resolve(__dirname, './src/apps/results')
    }
  },
  
  build: {
    outDir: '../src/main/resources/static/vue-dist',
    emptyOutDir: true,
    rollupOptions: {
      input: {
        dashboard: resolve(__dirname, 'dashboard.html'),
        monitor: resolve(__dirname, 'monitor.html'),
        configure: resolve(__dirname, 'configure.html'),
        results: resolve(__dirname, 'results.html')
      },
      output: {
        entryFileNames: '[name].js',
        chunkFileNames: 'chunks/[name]-[hash].js',
        assetFileNames: 'assets/[name]-[hash].[ext]'
      }
    }
  },
  
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8097',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/performance/api')
      },
      '/ws': {
        target: 'ws://localhost:8097',
        ws: true,
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/ws/, '/performance/ws')
      }
    }
  }
})