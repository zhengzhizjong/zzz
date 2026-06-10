<template>
  <div class="technician-detail-page">
    <van-nav-bar title="技师详情" left-arrow @click-left="router.back()" />

    <!-- 加载中 -->
    <div class="loading-wrap" v-if="loading">
      <van-loading size="24px">加载中...</van-loading>
    </div>

    <!-- 空状态 -->
    <van-empty v-if="!loading && !technician" description="技师信息不存在" />

    <!-- 技师信息 -->
    <template v-if="!loading && technician">
      <!-- 基本信息卡片 -->
      <div class="profile-card">
        <div class="profile-top">
          <div class="avatar-wrap">
            <div class="avatar-placeholder" :style="{ background: levelBgColor(technician.skillLevel) }">
              <span class="level-icon">{{ levelIcon(technician.skillLevel) }}</span>
            </div>
            <span class="duty-dot" :class="technician.onDuty ? 'on' : 'off'"></span>
          </div>
          <div class="profile-info">
            <div class="name-row">
              <span class="tech-name">技师{{ techDisplayName }}</span>
              <van-tag :color="levelColor(technician.skillLevel)" size="medium" text-color="#fff">
                {{ levelLabel(technician.skillLevel) }}
              </van-tag>
            </div>
            <div class="duty-status">
              <span class="duty-indicator" :class="technician.onDuty ? 'on' : 'off'"></span>
              <span :class="technician.onDuty ? 'duty-text-on' : 'duty-text-off'">
                {{ technician.onDuty ? '在岗' : '休息中' }}
              </span>
            </div>
            <div class="store-name" v-if="technician.storeName" @click="onStoreTap">
              <van-icon name="shop-o" size="14" color="#07C160" />
              <span>{{ technician.storeName }}</span>
              <van-icon name="arrow" size="12" color="#c0c4cc" />
            </div>
          </div>
        </div>
      </div>

      <!-- 擅长项目卡片 -->
      <div class="skills-card" v-if="skillTags.length">
        <div class="card-title">擅长项目</div>
        <div class="skill-tags">
          <van-tag
            v-for="tag in skillTags"
            :key="tag"
            plain
            size="large"
            color="#07C160"
            text-color="#07C160"
          >{{ tag }}</van-tag>
        </div>
      </div>

      <!-- 底部预约按钮 -->
      <div class="bottom-bar">
        <van-button type="primary" block round color="#07C160" @click="onBookNow">
          预约该技师
        </van-button>
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

// 技能等级配置
const LEVEL_MAP: Record<number, string> = { 1: '初级', 2: '中级', 3: '高级', 4: '资深', 5: '首席' }
const LEVEL_COLOR: Record<number, string> = { 1: '#909399', 2: '#07C160', 3: '#1989fa', 4: '#E6A23C', 5: '#F56C6C' }
const LEVEL_ICON: Record<number, string> = { 1: '★', 2: '★★', 3: '★★★', 4: '★★★★', 5: '★★★★★' }

function levelLabel(level: number) { return LEVEL_MAP[level] || '未知' }
function levelColor(level: number) { return LEVEL_COLOR[level] || '#909399' }
function levelBgColor(level: number) {
  const c = LEVEL_COLOR[level] || '#909399'
  return c + '18'
}
function levelIcon(level: number) { return LEVEL_ICON[level] || '★' }

const techDisplayName = computed(() => {
  if (!technician.value?.techNo) return ''
  const no = String(technician.value.techNo)
  return no.length > 4 ? no.slice(-4) : no
})

const skillTags = computed(() => {
  if (!technician.value) return []
  const items = technician.value.skilledItems
  if (!items) return []
  if (Array.isArray(items)) return items
  try {
    const parsed = JSON.parse(String(items))
    if (Array.isArray(parsed)) return parsed
  } catch {}
  return String(items).split(',').map((s: string) => s.trim()).filter(Boolean)
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
  router.push(`/store/detail/${technician.value.storeId}`)
}

function onBookNow() {
  if (!technician.value) return
  if (technician.value.status !== 1) {
    showToast('该技师暂不可预约')
    return
  }
  const sessionId = 'sid_' + Date.now() + '_' + Math.random().toString(36).substring(2, 8)
  router.push({
    path: '/appointment/step3',
    query: {
      storeId: String(technician.value.storeId || ''),
      storeName: encodeURIComponent(technician.value.storeName || ''),
      techId: String(technician.value.id),
      techName: encodeURIComponent('技师' + (techDisplayName.value || '')),
      sessionId
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
  padding: 20px 16px;
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
      border-radius: 50%;

      .level-icon {
        font-size: 16px;
        letter-spacing: -3px;
        line-height: 1;
      }
    }

    .duty-dot {
      position: absolute;
      bottom: 4px;
      right: 4px;
      width: 12px;
      height: 12px;
      border-radius: 50%;
      border: 2px solid #fff;

      &.on { background: #07C160; }
      &.off { background: #c0c4cc; }
    }
  }

  .profile-info {
    flex: 1;

    .name-row {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;

      .tech-name {
        font-size: 18px;
        font-weight: 600;
        color: #303133;
      }
    }

    .duty-status {
      display: flex;
      align-items: center;
      gap: 6px;
      margin-bottom: 8px;

      .duty-indicator {
        width: 8px;
        height: 8px;
        border-radius: 50%;

        &.on { background: #07C160; }
        &.off { background: #c0c4cc; }
      }

      .duty-text-on {
        font-size: 13px;
        color: #07C160;
      }

      .duty-text-off {
        font-size: 13px;
        color: #909399;
      }
    }

    .store-name {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      font-size: 14px;
      color: #07C160;
      cursor: pointer;
    }
  }
}

.skills-card {
  margin: 10px 12px;
  padding: 16px;
  background: #fff;
  border-radius: 10px;

  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 12px;
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
