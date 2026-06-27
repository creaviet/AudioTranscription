import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': 'http://localhost:8081',
      '/openapi.yaml': 'http://localhost:8081',
      '/swagger-ui': 'http://localhost:8081',
    },
  },
  test: {
    globals: true,
  },
})
