<template>
  <div class="service-list-page">
    <!-- 导航栏 -->
    <van-nav-bar title="服务项目" left-arrow @click-left="router.back()" />

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-wrap">
      <van-loading type="spinner" color="#07C160">加载中...</van-loading>
    </div>

    <!-- 服务项目列表 -->
    <div v-else-if="serviceItems.length > 0" class="service-list">
      <div class="service-card" v-for="item in serviceItems" :key="item.id">
        <div class="card-top">
          <span class="item-name">{{ item.itemName }}</span>
          <van-tag type="success" size="medium">{{ item.categoryName || '默认分类' }}</van-tag>
        </div>
        <div class="card-info">
          <span class="price">¥{{ item.price }}</span>
          <span class="duration">{{ item.duration }}分钟</span>
        </div>
        <div class="card-desc" v-if="item.description">{{ item.description }}</div>
        <div class="card-footer">
          <van-button
            type="success"
            size="small"
            round
            @click="onBookTap(item)"
          >立即预约</van-button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <van-empty v-if="!loading && serviceItems.length === 0" description="暂无服务项目" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getServiceItemList } from '@/api/service'

const router = useRouter()

const serviceItems = ref<any[]>([])
const loading = ref(false)

onMounted(() => {
  loadServiceItems()
})

async function loadServiceItems() {
  loading.value = true
  try {
    const res: any = await getServiceItemList({ status: 1 })
    serviceItems.value = res.data || []
  } catch {
    serviceItems.value = []
  } finally {
    loading.value = false
  }
}

function onBookTap(item: any) {
  router.push({
    path: '/appointment/step1',
    query: { serviceItemId: item.id }
  })
}
</script>

<style scoped lang="scss">
.service-list-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.service-list {
  padding: 10px 12px;
}

.service-card {
  margin-bottom: 10px;
  padding: 14px 16px;
  background: #fff;
  border-radius: 10px;

  .card-top {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    .item-name {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }

  .card-info {
    display: flex;
    align-items: baseline;
    gap: 12px;
    margin-bottom: 6px;

    .price {
      font-size: 18px;
      font-weight: 700;
      color: #FA5151;
    }

    .duration {
      font-size: 13px;
      color: #909399;
    }
  }

  .card-desc {
    font-size: 13px;
    color: #606266;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    margin-bottom: 10px;
  }

  .card-footer {
    display: flex;
    justify-content: flex-end;
  }
}
</style>
