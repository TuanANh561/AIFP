import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    host: true, // Bind 0.0.0.0 so iPhone Safari on local Wi-Fi can connect directly
    port: 5173,
    proxy: {
      '/api': {
        target: process.env.VITE_DEV_BACKEND_ORIGIN || 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  preview: {
    host: true,
    port: 4173,
  },
});
