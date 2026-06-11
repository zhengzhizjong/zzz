<template>
  <div class="notification-list-page">
    <van-nav-bar title="通知" left-arrow @click-left="router.back()" />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <div v-if="loading" class="loading-wrap">
        <van-loading type="spinner" color="#07C160">加载中...</van-loading>
      </div>

      <div v-else-if="notifications.length > 0" class="notification-list">
        <van-cell-group v-for="item in notifications" :key="item.id" class="notification-item" @click="onNotificationTap(item)">
          <van-cell
            :title="item.title"
            :label="item.content"
            :value="item.createdAt"
            is-link
          >
            <template #title>
              <div class="cell-title">
                <span class="title-text">{{ item.title }}</span>
                <van-tag v-if="!item.isRead" type="danger" size="medium" class="unread-tag">未读</van-tag>
              </div>
            </template>
            <template #label>
              <div class="cell-label">{{ item.content }}</div>
              <div class="cell-time">{{ item.createdAt }}</div>
            </template>
          </van-cell>
        </van-cell-group>
      </div>

      <van-empty v-if="!loading && notifications.length === 0" description="暂无通知" />
    </van-pull-refresh>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNotificationList, markNotificationRead } from '@/api/notification'

const router = useRouter()

interface Notification {
  id: number
  title: string
  content: string
  type: string
  isRead: boolean
  createdAt: string
}

const notifications = ref<Notification[]>([])
const loading = ref(false)
const refreshing = ref(false)

onMounted(() => {
  loadNotifications()
})

async function loadNotifications() {
  loading.value = true
  try {
    const res: any = await getNotificationList({ page: 1, pageSize: 50 })
    notifications.value = res.data?.list || res.data?.records || res.data || []
  } catch {
    notifications.value = []
  } finally {
    loading.value = false
  }
}

async function onRefresh() {
  try {
    await loadNotifications()
  } finally {
    refreshing.value = false
  }
}

async function onNotificationTap(item: Notification) {
  if (!item.isRead) {
    try {
      await markNotificationRead(item.id)
      item.isRead = true
    } catch {
      // 标记失败不影响体验
    }
  }
}
</script>

<style scoped lang="scss">
.notification-list-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.notification-list {
  padding: 0 0 12px;
}

.notification-item {
  margin-bottom: 8px;

  .cell-title {
    display: flex;
    align-items: center;
    gap: 6px;

    .title-text {
      font-size: 15px;
      font-weight: 500;
      color: #303133;
    }

    .unread-tag {
      flex-shrink: 0;
    }
  }

  .cell-label {
    font-size: 13px;
    color: #606266;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .cell-time {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }
}
</style>
