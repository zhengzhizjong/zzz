<template>
  <div class="technician-list-page">
    <!-- 搜索栏 -->
    <van-sticky>
      <van-search
        v-model="keyword"
        placeholder="搜索技师姓名"
        shape="round"
        @search="onSearch"
        @clear="onSearch"
      />
    </van-sticky>

    <!-- 技师列表 -->
    <div class="tech-list" v-if="!loading">
      <div
        class="tech-card"
        :class="{ 'card-disabled': item.isFullyBooked }"
        v-for="item in technicians"
        :key="item.id"
        @click="onTechnicianTap(item)"
      >
        <div class="tech-left">
          <div class="avatar-wrap">
            <van-image class="tech-avatar" round width="52" height="52" :src="item.avatarUrl || ''" fit="cover">
              <template #error><div class="avatar-placeholder">👤</div></template>
            </van-image>
            <span class="online-dot" :class="item.isOnline !== false ? 'online' : 'offline'" v-if="!item.isFullyBooked"></span>
          </div>
        </div>

        <div class="tech-info">
          <div class="tech-header">
            <span class="tech-name" :class="{ 'text-disabled': item.isFullyBooked }">{{ item.name }}</span>
            <van-tag v-if="item.levelName" type="success" size="medium">{{ item.levelName }}</van-tag>
          </div>

          <div class="tech-rating" v-if="item.rating">
            <van-rate v-model="item.rating" :size="12" color="#07C160" void-color="#eee" readonly allow-half />
            <span class="rating-text">{{ item.rating }}</span>
          </div>

          <div class="tech-skills" v-if="item.skillTags && item.skillTags.length">
            <van-tag v-for="tag in item.skillTags" :key="tag" plain size="medium" type="primary">{{ tag }}</van-tag>
          </div>
        </div>

        <!-- 约满遮罩 -->
        <div class="fully-booked-mask" v-if="item.isFullyBooked">
          <span class="fully-booked-text">约满</span>
        </div>
      </div>
    </div>

    <!-- 加载中 -->
    <div class="loading-wrap" v-if="loading">
      <van-loading size="24px">加载中...</van-loading>
    </div>

    <!-- 空状态 -->
    <van-empty v-if="!loading && technicians.length === 0" description="暂无技师信息" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast } from 'vant'
import { getTechnicianList } from '@/api/technician'

const router = useRouter()
const route = useRoute()

const technicians = ref<any[]>([])
const loading = ref(false)
const keyword = ref('')
const storeId = ref('')

const SKILL_LEVEL_MAP: Record<number, string> = {
  1: '初级',
  2: '中级',
  3: '高级',
  4: '专家'
}

onMounted(() => {
  storeId.value = (route.query.storeId as string) || ''
  loadTechnicianList()
})

async function loadTechnicianList() {
  loading.value = true
  try {
    const params: Record<string, any> = { storeId: storeId.value }
    if (keyword.value) params.keyword = keyword.value

    const res: any = await getTechnicianList(params)
    const list = (res.data?.list || res.data || []).map((item: any) => ({
      ...item,
      name: item.name || item.technicianNo || '技师',
      levelName: item.levelName || SKILL_LEVEL_MAP[item.skillLevel] || '',
      isFullyBooked: item.isFullyBooked || false,
      isOnline: item.isOnline !== false
    }))
    technicians.value = list
  } catch {} finally {
    loading.value = false
  }
}

function onSearch() {
  loadTechnicianList()
}

function onTechnicianTap(item: any) {
  if (item.isFullyBooked) {
    showToast('该技师已约满')
    return
  }
  router.push({
    path: '/appointment/step3',
    query: { storeId: storeId.value, techId: item.id }
  })
}
</script>

<style scoped lang="scss">
.technician-list-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.tech-list {
  padding: 0 12px;
}

.tech-card {
  display: flex;
  align-items: flex-start;
  padding: 14px;
  margin-top: 10px;
  background: #fff;
  border-radius: 10px;
  position: relative;

  &.card-disabled {
    opacity: 0.6;
  }

  .tech-left {
    margin-right: 12px;
    flex-shrink: 0;

    .avatar-wrap {
      position: relative;

      .avatar-placeholder {
        width: 52px;
        height: 52px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #f0f0f0;
        border-radius: 50%;
        font-size: 24px;
      }

      .online-dot {
        position: absolute;
        bottom: 2px;
        right: 2px;
        width: 10px;
        height: 10px;
        border-radius: 50%;
        border: 2px solid #fff;

        &.online { background: #07C160; }
        &.offline { background: #c0c4cc; }
      }
    }
  }

  .tech-info {
    flex: 1;

    .tech-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 4px;

      .tech-name {
        font-size: 16px;
        font-weight: 600;
        color: #303133;

        &.text-disabled { color: #c0c4cc; }
      }
    }

    .tech-rating {
      display: flex;
      align-items: center;
      gap: 4px;
      margin: 4px 0;

      .rating-text {
        font-size: 12px;
        color: #606266;
      }
    }

    .tech-skills {
      display: flex;
      flex-wrap: wrap;
      gap: 4px;
      margin-top: 6px;
    }
  }

  .fully-booked-mask {
    position: absolute;
    top: 0;
    right: 0;
    width: 60px;
    height: 60px;
    overflow: hidden;

    .fully-booked-text {
      position: absolute;
      top: 8px;
      right: -16px;
      width: 80px;
      text-align: center;
      background: #fa5151;
      color: #fff;
      font-size: 11px;
      padding: 2px 0;
      transform: rotate(45deg);
    }
  }
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}
</style>
