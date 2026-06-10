<template>
  <div class="technician-detail-page">
    <!-- 导航栏 -->
    <van-nav-bar
      title="技师详情"
      left-arrow
      @click-left="router.back()"
    />

    <!-- 加载中 -->
    <div class="loading-wrap" v-if="loading">
      <van-loading size="24px">加载中...</van-loading>
    </div>

    <!-- 空状态 -->
    <van-empty v-if="!loading && !technician" description="技师信息不存在" />

    <!-- 技师信息 -->
    <template v-if="!loading && technician">
      <!-- 技师基本信息卡片 -->
      <div class="profile-card">
        <div class="profile-top">
          <div class="avatar-wrap">
            <van-image class="tech-avatar" round width="72" height="72" :src="technician.avatarUrl || ''" fit="cover">
              <template #error><div class="avatar-placeholder">👤</div></template>
            </van-image>
            <span class="online-dot" :class="technician.onDuty ? 'online' : 'offline'"></span>
          </div>
          <div class="profile-info">
            <div class="name-row">
              <span class="tech-name">{{ technician.name || technician.technicianNo || '技师' }}</span>
              <van-tag v-if="skillLevelText" type="success" size="medium">{{ skillLevelText }}</van-tag>
            </div>
            <div class="rating-row" v-if="technician.rating">
              <van-rate v-model="technician.rating" :size="14" color="#07C160" void-color="#eee" readonly allow-half />
              <span class="rating-value">{{ technician.rating }}</span>
            </div>
            <div class="store-name" v-if="technician.storeName" @click="onStoreTap">
              <van-icon name="shop-o" />
              <span>{{ technician.storeName }}</span>
              <van-icon name="arrow" size="12" color="#c0c4cc" />
            </div>
          </div>
        </div>
      </div>

      <!-- 技能标签卡片 -->
      <div class="skills-card" v-if="skillTags.length">
        <div class="card-title">擅长项目</div>
        <div class="skill-tags">
          <van-tag
            v-for="tag in skillTags"
            :key="tag"
            plain
            size="large"
            type="primary"
          >{{ tag }}</van-tag>
        </div>
      </div>

      <!-- 底部预约按钮 -->
      <div class="bottom-bar">
        <van-button type="primary" block round @click="onBookNow">预约该技师</van-button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast } from 'vant'
import { getTechnicianDetail } from '@/api/technician'

const router = useRouter()
const route = useRoute()

const technician = ref<any>(null)
const loading = ref(true)

const SKILL_LEVEL_MAP: Record<number, string> = {
  1: '初级',
  2: '中级',
  3: '高级',
  4: '专家'
}

const skillLevelText = computed(() => {
  if (!technician.value) return ''
  return SKILL_LEVEL_MAP[technician.value.skillLevel] || ''
})

const skillTags = computed(() => {
  if (!technician.value) return []
  const items = technician.value.skilledItems
  if (!items) return []
  if (Array.isArray(items)) return items
  if (typeof items === 'string') return items.split(',').filter((s: string) => s.trim())
  return []
})

onMounted(() => {
  const id = route.params.id || route.query.id
  if (id) {
    loadTechnicianDetail(id as string)
  } else {
    loading.value = false
  }
})

async function loadTechnicianDetail(id: string) {
  loading.value = true
  try {
    const res: any = await getTechnicianDetail(id)
    technician.value = res.data || null
  } catch {
    technician.value = null
  } finally {
    loading.value = false
  }
}

function onStoreTap() {
  if (!technician.value?.storeId) return
  router.push({ path: `/store/detail/${technician.value.storeId}` })
}

function onBookNow() {
  if (!technician.value) return
  if (technician.value.status !== 1) {
    showToast('该技师暂不可预约')
    return
  }
  router.push({
    path: '/appointment/step3',
    query: {
      storeId: String(technician.value.storeId || ''),
      techId: String(technician.value.id),
      techName: encodeURIComponent(technician.value.name || '')
    }
  })
}
</script>

<style scoped lang="scss">
.technician-detail-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 80px;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.profile-card {
  margin: 10px 12px;
  padding: 20px 14px;
  background: #fff;
  border-radius: 10px;

  .profile-top {
    display: flex;
    align-items: flex-start;
  }

  .avatar-wrap {
    position: relative;
    margin-right: 16px;
    flex-shrink: 0;

    .avatar-placeholder {
      width: 72px;
      height: 72px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f0f0f0;
      border-radius: 50%;
      font-size: 32px;
    }

    .online-dot {
      position: absolute;
      bottom: 4px;
      right: 4px;
      width: 12px;
      height: 12px;
      border-radius: 50%;
      border: 2px solid #fff;

      &.online { background: #07C160; }
      &.offline { background: #c0c4cc; }
    }
  }

  .profile-info {
    flex: 1;

    .name-row {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 6px;

      .tech-name {
        font-size: 18px;
        font-weight: 600;
        color: #303133;
      }
    }

    .rating-row {
      display: flex;
      align-items: center;
      gap: 6px;
      margin-bottom: 8px;

      .rating-value {
        font-size: 14px;
        color: #606266;
        font-weight: 500;
      }
    }

    .store-name {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: #07C160;
      cursor: pointer;
    }
  }
}

.skills-card {
  margin: 10px 12px;
  padding: 14px;
  background: #fff;
  border-radius: 10px;

  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 10px;
  }

  .skill-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  background: #fff;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
}
</style>
