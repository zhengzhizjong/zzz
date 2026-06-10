<template>
  <div class="app-container">
    <router-view />
    <van-tabbar v-model="activeTab" route active-color="#07C160" inactive-color="#999" v-if="showTabBar">
      <van-tabbar-item to="/home" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item to="/appointment/step1" icon="calendar-o">预约</van-tabbar-item>
      <van-tabbar-item to="/mall" icon="shop-o">商场</van-tabbar-item>
      <van-tabbar-item to="/promotion" icon="gift-o">推广</van-tabbar-item>
      <van-tabbar-item to="/my" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeTab = ref(0)

const showTabBar = computed(() => {
  return route.meta.showTabBar === true
})

watch(() => route.path, (path) => {
  if (path.startsWith('/home')) activeTab.value = 0
  else if (path.startsWith('/appointment/step1')) activeTab.value = 1
  else if (path.startsWith('/mall')) activeTab.value = 2
  else if (path.startsWith('/promotion')) activeTab.value = 3
  else if (path.startsWith('/my')) activeTab.value = 4
}, { immediate: true })
</script>

<style>
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  background-color: #f5f5f5;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>
