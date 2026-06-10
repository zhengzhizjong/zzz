import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { VantResolver } from '@vant/auto-import-resolver'
import path from 'path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  return {
    plugins: [
      vue(),
      AutoImport({
        resolvers: [VantResolver()]
      }),
      Components({
        resolvers: [VantResolver()]
      })
    ],
    resolve: {
      alias: { '@': path.resolve(__dirname, 'src') }
    },
    server: {
      port: 3100,
      host: '0.0.0.0',
      hmr: {
        port: 3100
      },
      proxy: {
        '/api': { target: env.VITE_API_BASE_URL, changeOrigin: true }
      }
    },
    css: {
      preprocessorOptions: {
        scss: {
          additionalData: `@import "@/styles/variables.scss";`
        }
      }
    }
  }
})
