<template>
  <div class="technician-list-page">
    <van-sticky>
      <van-nav-bar title="技师团队" left-arrow @click-left="router.back()" />

      <!-- 技能等级筛选 -->
      <div class="level-tabs">
        <div
          v-for="tab in levelTabs"
          :key="tab.value"
          class="level-tab"
          :class="{ active: currentLevel === tab.value }"
          @click="onLevelChange(tab.value)"
        >{{ tab.label }}</div>
      </div>

      <!-- 门店筛选 -->
      <van-dropdown-menu active-color="#07C160">
        <van-dropdown-item v-model="currentStoreId" :options="storeOptions" @change="onStoreChange" />
      </van-dropdown-menu>
    </van-sticky>

    <!-- 技师列表 -->
    <van-list
      v-model:loading="listLoading"
      :finished="finished"
      finished-text="没有更多了"
      @load="onLoad"
    >
      <div class="tech-list" v-if="technicians.length">
        <div
          class="tech-card"
          v-for="item in technicians"
          :key="item.id"
          @click="onTechnicianTap(item)"
        >
          <div class="card-left">
            <div class="avatar-wrap">
              <div class="avatar-placeholder" :style="{ background: levelBgColor(item.skillLevel) }">
                <span class="level-icon">{{ levelIcon(item.skillLevel) }}</span>
              </div>
              <span class="duty-dot" :class="item.onDuty ? 'on' : 'off'"></span>
            </div>
          </div>

          <div class="card-info">
            <div class="info-header">
              <span class="tech-name">技师{{ techDisplayName(item) }}</span>
              <van-tag :color="levelColor(item.skillLevel)" size="medium" text-color="#fff">
                {{ levelLabel(item.skillLevel) }}
              </van-tag>
            </div>

            <div class="info-store" v-if="item.storeName">
              <van-icon name="shop-o" size="13" color="#909399" />
              <span>{{ item.storeName }}</span>
            </div>

            <div class="info-skills" v-if="parseSkills(item.skilledItems).length">
              <van-tag
                v-for="skill in parseSkills(item.skilledItems)"
                :key="skill"
                plain
                size="medium"
                color="#07C160"
                text-color="#07C160"
              >{{ skill }}</van-tag>
            </div>
          </div>

          <van-icon name="arrow" color="#c0c4cc" class="card-arrow" />
        </div>
      </div>
    </van-list>

    <!-- 首次加载中 -->
    <div class="loading-wrap" v-if="firstLoading">
      <van-loading size="24px">加载中...</van-loading>
    </div>

    <!-- 空状态 -->
    <van-empty v-if="!firstLoading && !listLoading && technicians.length === 0" description="暂无技师信息" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTechnicianList } from '@/api/technician'
import { getStoreList } from '@/api/store'

const router = useRouter()

// 技能等级配置
const LEVEL_MAP: Record<number, string> = { 1: '初级', 2: '中级', 3: '高级', 4: '资深', 5: '首席' }
const LEVEL_COLOR: Record<number, string> = { 1: '#909399', 2: '#07C160', 3: '#1989fa', 4: '#E6A23C', 5: '#F56C6C' }
const LEVEL_ICON: Record<number, string> = { 1: '★', 2: '★★', 3: '★★★', 4: '★★★★', 5: '★★★★★' }

const levelTabs = [
  { label: '全部', value: 0 },
  { label: '初级', value: 1 },
  { label: '中级', value: 2 },
  { label: '高级', value: 3 },
  { label: '资深', value: 4 },
  { label: '首席', value: 5 }
]

// 状态
const technicians = ref<any[]>([])
const currentLevel = ref(0)
const currentStoreId = ref('')
const storeOptions = ref<{ text: string; value: string }[]>([{ text: '全部门店', value: '' }])
const listLoading = ref(false)
const finished = ref(false)
const firstLoading = ref(true)
const page = ref(1)
const pageSize = 10

function levelLabel(level: number) { return LEVEL_MAP[level] || '未知' }
function levelColor(level: number) { return LEVEL_COLOR[level] || '#909399' }
function levelBgColor(level: number) {
  const c = LEVEL_COLOR[level] || '#909399'
  return c + '18'
}
function levelIcon(level: number) { return LEVEL_ICON[level] || '★' }

function techDisplayName(item: any) {
  if (item.techNo) {
    const no = String(item.techNo)
    return no.length > 4 ? no.slice(-4) : no
  }
  return ''
}

function parseSkills(skilledItems: string | undefined) {
  if (!skilledItems) return []
  if (Array.isArray(skilledItems)) return skilledItems
  try {
    const parsed = JSON.parse(String(skilledItems))
    if (Array.isArray(parsed)) return parsed
  } catch {}
  return String(skilledItems).split(',').map((s: string) => s.trim()).filter(Boolean)
}

// 加载门店列表
async function loadStores() {
  try {
    const res: any = await getStoreList({ pageSize: 100 })
    const list = res.data?.list || res.data || []
    storeOptions.value = [
      { text: '全部门店', value: '' },
      ...list.map((s: any) => ({ text: s.name || s.storeName || '', value: String(s.id || s.storeId) }))
    ]
  } catch {}
}

// 加载技师列表
async function loadList(reset = false) {
  if (reset) {
    page.value = 1
    technicians.value = []
    finished.value = false
  }

  listLoading.value = true
  try {
    const params: Record<string, any> = { page: page.value, pageSize }
    if (currentLevel.value) params.skillLevel = currentLevel.value
    if (currentStoreId.value) params.storeId = currentStoreId.value

    const res: any = await getTechnicianList(params)
    const list = res.data?.list || []
    const total = res.data?.total || 0

    if (reset) {
      technicians.value = list
    } else {
      technicians.value = [...technicians.value, ...list]
    }

    // 排序：skillLevel 高→低，再按 storeName
    technicians.value.sort((a: any, b: any) => {
      const diff = (b.skillLevel || 0) - (a.skillLevel || 0)
      if (diff !== 0) return diff
      return String(a.storeName || '').localeCompare(String(b.storeName || ''))
    })

    finished.value = technicians.value.length >= total
    page.value++
  } catch {
    finished.value = true
  } finally {
    listLoading.value = false
    firstLoading.value = false
  }
}

function onLoad() {
  loadList()
}

function onLevelChange(val: number) {
  currentLevel.value = val
  loadList(true)
}

function onStoreChange() {
  loadList(true)
}

function onTechnicianTap(item: any) {
  router.push(`/technician/detail/${item.id}`)
}

onMounted(() => {
  loadStores()
})
</script>

<style scoped lang="scss">
.technician-list-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.level-tabs {
  display: flex;
  background: #fff;
  padding: 0 4px;
  border-bottom: 1px solid #f0f0f0;

  .level-tab {
    flex: 1;
    text-align: center;
    padding: 10px 0;
    font-size: 13px;
    color: #606266;
    position: relative;
    transition: color 0.2s;

    &.active {
      color: #07C160;
      font-weight: 600;

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 20px;
        height: 3px;
        background: #07C160;
        border-radius: 2px;
      }
    }
  }
}

.tech-list {
  padding: 0 12px;
}

.tech-card {
  display: flex;
  align-items: center;
  padding: 14px;
  margin-top: 10px;
  background: #fff;
  border-radius: 10px;

  .card-left {
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
        border-radius: 50%;

        .level-icon {
          font-size: 12px;
          letter-spacing: -2px;
          line-height: 1;
        }
      }

      .duty-dot {
        position: absolute;
        bottom: 2px;
        right: 2px;
        width: 10px;
        height: 10px;
        border-radius: 50%;
        border: 2px solid #fff;

        &.on { background: #07C160; }
        &.off { background: #c0c4cc; }
      }
    }
  }

  .card-info {
    flex: 1;
    min-width: 0;

    .info-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 4px;

      .tech-name {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }
    }

    .info-store {
      display: flex;
      align-items: center;
      gap: 3px;
      font-size: 12px;
      color: #909399;
      margin-bottom: 6px;
    }

    .info-skills {
      display: flex;
      flex-wrap: wrap;
      gap: 4px;
    }
  }

  .card-arrow {
    flex-shrink: 0;
    margin-left: 8px;
  }
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}
</style>
