<template>
  <div class="store-list-page">
    <van-nav-bar title="门店列表" left-arrow @click-left="router.back()" />

    <van-sticky offset-top="46">
      <van-search
        v-model="keyword"
        placeholder="搜索门店名称"
        shape="round"
        @search="onSearch"
        @clear="onSearch"
      />
    </van-sticky>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <div
          class="store-card"
          v-for="item in stores"
          :key="item.id"
          @click="goDetail(item)"
        >
          <div class="card-top">
            <span class="store-name">{{ item.storeName }}</span>
            <van-tag :type="item.status === 1 ? 'success' : 'danger'" size="medium">
              {{ item.status === 1 ? '营业中' : '已打烊' }}
            </van-tag>
          </div>
          <div class="card-row">
            <van-icon name="location-o" size="14" color="#909399" />
            <span class="card-text">{{ item.address || '暂无地址信息' }}</span>
          </div>
          <div class="card-row">
            <van-icon name="clock-o" size="14" color="#909399" />
            <span class="card-text">{{ item.businessStartTime || '09:00' }} - {{ item.businessEndTime || '21:00' }}</span>
          </div>
          <div class="card-bottom">
            <span class="distance" v-if="item._distance !== undefined">{{ item._distance }}</span>
            <van-icon name="arrow" size="14" color="#c0c4cc" />
          </div>
        </div>
      </van-list>

      <van-empty v-if="!loading && !refreshing && stores.length === 0" description="暂无门店信息" />
    </van-pull-refresh>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { getStoreList } from '@/api/store'

const router = useRouter()

const stores = ref<any[]>([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(1)
const pageSize = 10
const keyword = ref('')

// 用户位置
const userLat = ref<number | null>(null)
const userLng = ref<number | null>(null)
const geoReady = ref(false)

// 获取用户位置
function getUserLocation() {
  if (!navigator.geolocation) {
    geoReady.value = true
    return
  }
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      userLat.value = pos.coords.latitude
      userLng.value = pos.coords.longitude
      geoReady.value = true
      // 位置获取成功后重新排序已有数据
      sortStoresByDistance()
    },
    () => {
      geoReady.value = true
    },
    { enableHighAccuracy: true, timeout: 5000, maximumAge: 60000 }
  )
}

// Haversine 公式计算距离（返回 km）
function haversineDistance(lat1: number, lng1: number, lat2: number, lng2: number): number {
  const R = 6371
  const dLat = toRad(lat2 - lat1)
  const dLng = toRad(lng2 - lng1)
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
    Math.sin(dLng / 2) * Math.sin(dLng / 2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return R * c
}

function toRad(deg: number): number {
  return deg * (Math.PI / 180)
}

// 格式化距离
function formatDistance(km: number): string {
  if (km < 1) {
    return Math.round(km * 1000) + 'm'
  }
  return km.toFixed(1) + 'km'
}

// 给门店列表添加距离字段并排序
function sortStoresByDistance() {
  if (userLat.value === null || userLng.value === null) return
  stores.value.forEach((item) => {
    if (item.latitude && item.longitude) {
      const dist = haversineDistance(userLat.value!, userLng.value!, item.latitude, item.longitude)
      item._distance = formatDistance(dist)
      item._distanceNum = dist
    } else {
      item._distance = undefined
      item._distanceNum = Infinity
    }
  })
  stores.value.sort((a, b) => (a._distanceNum ?? Infinity) - (b._distanceNum ?? Infinity))
}

// 加载数据
async function fetchStores(isRefresh = false) {
  if (isRefresh) {
    page.value = 1
    finished.value = false
  }

  try {
    const params: Record<string, any> = { page: page.value, pageSize }
    if (keyword.value) params.keyword = keyword.value

    const res: any = await getStoreList(params)
    const list = res.data?.list || res.data || []

    if (isRefresh) {
      stores.value = list
    } else {
      stores.value = stores.value.concat(list)
    }

    // 计算距离并排序
    if (userLat.value !== null && userLng.value !== null) {
      sortStoresByDistance()
    } else {
      // 没有位置信息时给每项加 _distanceNum 用于后续排序
      stores.value.forEach((item) => {
        if (item._distanceNum === undefined) item._distanceNum = Infinity
      })
    }

    page.value++
    finished.value = list.length < pageSize
  } catch {
    finished.value = true
  }
}

// van-list 的 load 回调
function onLoad() {
  fetchStores(false).finally(() => {
    loading.value = false
    refreshing.value = false
  })
}

// 下拉刷新
function onRefresh() {
  fetchStores(true).finally(() => {
    loading.value = false
    refreshing.value = false
  })
}

// 搜索
function onSearch() {
  stores.value = []
  page.value = 1
  finished.value = false
  loading.value = true
  fetchStores(true).finally(() => {
    loading.value = false
    refreshing.value = false
  })
}

function goDetail(item: any) {
  router.push(`/store/detail/${item.id}`)
}

// 初始化：先获取位置，再触发首次加载
getUserLocation()
</script>

<style scoped lang="scss">
.store-list-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.store-card {
  margin: 10px 12px;
  padding: 14px;
  background: #fff;
  border-radius: 10px;

  .card-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;

    .store-name {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      flex: 1;
      margin-right: 8px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .card-row {
    display: flex;
    align-items: center;
    margin-top: 6px;

    .card-text {
      font-size: 13px;
      color: #606266;
      margin-left: 4px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .card-bottom {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 10px;
    padding-top: 10px;
    border-top: 1px solid #f5f5f5;

    .distance {
      font-size: 13px;
      color: #07C160;
      font-weight: 500;
    }
  }
}
</style>
