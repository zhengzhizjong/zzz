<template>
  <div class="store-list-page">
    <!-- 搜索栏 -->
    <van-sticky>
      <van-search
        v-model="keyword"
        placeholder="搜索门店名称"
        shape="round"
        @search="onSearch"
        @clear="onSearch"
      />
    </van-sticky>

    <!-- 门店列表 -->
    <van-list
      v-model:loading="loading"
      :finished="!hasMore"
      finished-text="没有更多了"
      @load="loadStoreList"
    >
      <div class="store-card" v-for="item in stores" :key="item.id" @click="onStoreTap(item)">
        <div class="store-header">
          <span class="store-name">{{ item.storeName || item.name }}</span>
          <van-tag :type="item.status === 1 ? 'success' : 'danger'" size="medium">
            {{ item.status === 1 ? '营业中' : '已打烊' }}
          </van-tag>
        </div>
        <div class="store-info">
          <span class="info-item">📍 {{ item.address || '暂无地址信息' }}</span>
        </div>
        <div class="store-info">
          <span class="info-item">🕐 {{ item.businessStartTime || '09:00' }} - {{ item.businessEndTime || '21:00' }}</span>
        </div>
      </div>
    </van-list>

    <!-- 空状态 -->
    <van-empty v-if="!loading && stores.length === 0" description="暂无门店信息" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { getStoreList } from '@/api/store'

const router = useRouter()

const stores = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const hasMore = ref(true)
const keyword = ref('')

async function loadStoreList() {
  if (loading.value) return
  loading.value = true

  try {
    const params: Record<string, any> = { page: page.value, pageSize }
    if (keyword.value) params.keyword = keyword.value

    const res: any = await getStoreList(params)
    const list = res.data?.list || res.data || []
    stores.value = stores.value.concat(list)
    page.value++
    hasMore.value = list.length >= pageSize
  } catch {
    hasMore.value = false
  } finally {
    loading.value = false
  }
}

function onSearch() {
  stores.value = []
  page.value = 1
  hasMore.value = true
  loadStoreList()
}

function onStoreTap(item: any) {
  router.push(`/store/detail/${item.id}`)
}
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

  .store-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    .store-name {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }

  .store-info {
    margin-top: 4px;

    .info-item {
      font-size: 13px;
      color: #606266;
    }
  }
}
</style>
