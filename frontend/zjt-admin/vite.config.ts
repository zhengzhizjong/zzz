import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import path from 'path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  return {
    plugins: [
      vue(),
      AutoImport({ resolvers: [ElementPlusResolver()] }),
      Components({ resolvers: [ElementPlusResolver()] })
    ],
    resolve: {
      alias: { '@': path.resolve(__dirname, 'src') }
    },
    server: {
      port: 3000,
      host: '0.0.0.0',
      hmr: {
        port: 3000
      },
      proxy: {
        '/api': { target: env.VITE_API_BASE_URL, changeOrigin: true }
      }
    }
  }
})
