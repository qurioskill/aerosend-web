import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  build: {
    outDir: '../src/main/resources/static/dist',
    emptyOutDir: true,
    rollupOptions: { input: 'src/main.js', output: { entryFileNames: 'main.js', assetFileNames: 'app.[ext]' } }
  },
  server: { proxy: { '/': 'http://localhost:8080' } }
})
