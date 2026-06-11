<template>
  <router-view v-if="!showTabBar" />
  <div v-else class="app-container">
    <div class="main-content">
      <router-view />
    </div>
    <van-tabbar v-model="active" route active-color="#07C160" inactive-color="#999">
      <van-tabbar-item icon="wap-home-o" to="/workspace">工作台</van-tabbar-item>
      <van-tabbar-item icon="chart-trending-o" to="/performance">业绩</van-tabbar-item>
      <van-tabbar-item icon="share-o" to="/promotion">推广</van-tabbar-item>
      <van-tabbar-item icon="bookmark-o" to="/learning">学习</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/my">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const active = ref(0)

const tabPaths = ['/workspace', '/performance', '/promotion', '/learning', '/my']

const showTabBar = computed(() => {
  return tabPaths.includes(route.path)
})

watch(() => route.path, (path) => {
  const idx = tabPaths.indexOf(path)
  if (idx !== -1) {
    active.value = idx
  }
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #f5f5f5;
}

.app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-content {
  flex: 1;
  padding-bottom: 50px;
  overflow-y: auto;
}
</style>
