// vite.config.ts
import { defineConfig, loadEnv } from "file:///workspace/frontend/zjt-admin/node_modules/vite/dist/node/index.js";
import vue from "file:///workspace/frontend/zjt-admin/node_modules/@vitejs/plugin-vue/dist/index.mjs";
import AutoImport from "file:///workspace/frontend/zjt-admin/node_modules/unplugin-auto-import/dist/vite.js";
import Components from "file:///workspace/frontend/zjt-admin/node_modules/unplugin-vue-components/dist/vite.js";
import { ElementPlusResolver } from "file:///workspace/frontend/zjt-admin/node_modules/unplugin-vue-components/dist/resolvers.js";
import path from "path";
var __vite_injected_original_dirname = "/workspace/frontend/zjt-admin";
var vite_config_default = defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd());
  return {
    plugins: [
      vue(),
      AutoImport({ resolvers: [ElementPlusResolver()] }),
      Components({ resolvers: [ElementPlusResolver()] })
    ],
    resolve: {
      alias: { "@": path.resolve(__vite_injected_original_dirname, "src") }
    },
    base: "/admin/",
    server: {
      port: 3001,
      host: "0.0.0.0",
      hmr: {
        port: 3001
      },
      proxy: {
        "/api": { target: env.VITE_API_BASE_URL, changeOrigin: true }
      }
    }
  };
});
export {
  vite_config_default as default
};
//# sourceMappingURL=data:application/json;base64,ewogICJ2ZXJzaW9uIjogMywKICAic291cmNlcyI6IFsidml0ZS5jb25maWcudHMiXSwKICAic291cmNlc0NvbnRlbnQiOiBbImNvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9kaXJuYW1lID0gXCIvd29ya3NwYWNlL2Zyb250ZW5kL3pqdC1hZG1pblwiO2NvbnN0IF9fdml0ZV9pbmplY3RlZF9vcmlnaW5hbF9maWxlbmFtZSA9IFwiL3dvcmtzcGFjZS9mcm9udGVuZC96anQtYWRtaW4vdml0ZS5jb25maWcudHNcIjtjb25zdCBfX3ZpdGVfaW5qZWN0ZWRfb3JpZ2luYWxfaW1wb3J0X21ldGFfdXJsID0gXCJmaWxlOi8vL3dvcmtzcGFjZS9mcm9udGVuZC96anQtYWRtaW4vdml0ZS5jb25maWcudHNcIjtpbXBvcnQgeyBkZWZpbmVDb25maWcsIGxvYWRFbnYgfSBmcm9tICd2aXRlJ1xuaW1wb3J0IHZ1ZSBmcm9tICdAdml0ZWpzL3BsdWdpbi12dWUnXG5pbXBvcnQgQXV0b0ltcG9ydCBmcm9tICd1bnBsdWdpbi1hdXRvLWltcG9ydC92aXRlJ1xuaW1wb3J0IENvbXBvbmVudHMgZnJvbSAndW5wbHVnaW4tdnVlLWNvbXBvbmVudHMvdml0ZSdcbmltcG9ydCB7IEVsZW1lbnRQbHVzUmVzb2x2ZXIgfSBmcm9tICd1bnBsdWdpbi12dWUtY29tcG9uZW50cy9yZXNvbHZlcnMnXG5pbXBvcnQgcGF0aCBmcm9tICdwYXRoJ1xuXG5leHBvcnQgZGVmYXVsdCBkZWZpbmVDb25maWcoKHsgbW9kZSB9KSA9PiB7XG4gIGNvbnN0IGVudiA9IGxvYWRFbnYobW9kZSwgcHJvY2Vzcy5jd2QoKSlcbiAgcmV0dXJuIHtcbiAgICBwbHVnaW5zOiBbXG4gICAgICB2dWUoKSxcbiAgICAgIEF1dG9JbXBvcnQoeyByZXNvbHZlcnM6IFtFbGVtZW50UGx1c1Jlc29sdmVyKCldIH0pLFxuICAgICAgQ29tcG9uZW50cyh7IHJlc29sdmVyczogW0VsZW1lbnRQbHVzUmVzb2x2ZXIoKV0gfSlcbiAgICBdLFxuICAgIHJlc29sdmU6IHtcbiAgICAgIGFsaWFzOiB7ICdAJzogcGF0aC5yZXNvbHZlKF9fZGlybmFtZSwgJ3NyYycpIH1cbiAgICB9LFxuICAgIGJhc2U6ICcvYWRtaW4vJyxcbiAgICBzZXJ2ZXI6IHtcbiAgICAgIHBvcnQ6IDMwMDEsXG4gICAgICBob3N0OiAnMC4wLjAuMCcsXG4gICAgICBobXI6IHtcbiAgICAgICAgcG9ydDogMzAwMVxuICAgICAgfSxcbiAgICAgIHByb3h5OiB7XG4gICAgICAgICcvYXBpJzogeyB0YXJnZXQ6IGVudi5WSVRFX0FQSV9CQVNFX1VSTCwgY2hhbmdlT3JpZ2luOiB0cnVlIH1cbiAgICAgIH1cbiAgICB9XG4gIH1cbn0pXG4iXSwKICAibWFwcGluZ3MiOiAiO0FBQXlRLFNBQVMsY0FBYyxlQUFlO0FBQy9TLE9BQU8sU0FBUztBQUNoQixPQUFPLGdCQUFnQjtBQUN2QixPQUFPLGdCQUFnQjtBQUN2QixTQUFTLDJCQUEyQjtBQUNwQyxPQUFPLFVBQVU7QUFMakIsSUFBTSxtQ0FBbUM7QUFPekMsSUFBTyxzQkFBUSxhQUFhLENBQUMsRUFBRSxLQUFLLE1BQU07QUFDeEMsUUFBTSxNQUFNLFFBQVEsTUFBTSxRQUFRLElBQUksQ0FBQztBQUN2QyxTQUFPO0FBQUEsSUFDTCxTQUFTO0FBQUEsTUFDUCxJQUFJO0FBQUEsTUFDSixXQUFXLEVBQUUsV0FBVyxDQUFDLG9CQUFvQixDQUFDLEVBQUUsQ0FBQztBQUFBLE1BQ2pELFdBQVcsRUFBRSxXQUFXLENBQUMsb0JBQW9CLENBQUMsRUFBRSxDQUFDO0FBQUEsSUFDbkQ7QUFBQSxJQUNBLFNBQVM7QUFBQSxNQUNQLE9BQU8sRUFBRSxLQUFLLEtBQUssUUFBUSxrQ0FBVyxLQUFLLEVBQUU7QUFBQSxJQUMvQztBQUFBLElBQ0EsTUFBTTtBQUFBLElBQ04sUUFBUTtBQUFBLE1BQ04sTUFBTTtBQUFBLE1BQ04sTUFBTTtBQUFBLE1BQ04sS0FBSztBQUFBLFFBQ0gsTUFBTTtBQUFBLE1BQ1I7QUFBQSxNQUNBLE9BQU87QUFBQSxRQUNMLFFBQVEsRUFBRSxRQUFRLElBQUksbUJBQW1CLGNBQWMsS0FBSztBQUFBLE1BQzlEO0FBQUEsSUFDRjtBQUFBLEVBQ0Y7QUFDRixDQUFDOyIsCiAgIm5hbWVzIjogW10KfQo=
