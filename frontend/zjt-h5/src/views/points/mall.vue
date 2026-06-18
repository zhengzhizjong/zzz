<template>
  <div class="points-mall-page">
    <van-nav-bar title="积分商城" left-arrow @click-left="router.back()" />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <!-- 当前积分 -->
      <div class="points-header">
        <div class="points-card">
          <div class="points-label">当前积分</div>
          <div class="points-value">{{ currentPoints }}</div>
        </div>
      </div>

      <!-- 分类筛选 -->
      <div class="category-filter">
        <van-tabs v-model:active="activeCategory" color="#07C160" @change="onCategoryChange">
          <van-tab v-for="cat in categories" :key="cat.key" :title="cat.name" />
        </van-tabs>
      </div>

      <!-- 商品网格 -->
      <div class="goods-grid">
        <div v-for="item in filteredGoods" :key="item.id" class="goods-item">
          <div class="goods-img">
            <div class="goods-img-placeholder">{{ item.icon }}</div>
          </div>
          <div class="goods-name">{{ item.name }}</div>
          <div class="goods-points">
            <span class="points-num">{{ item.points }}</span>
            <span class="points-unit">积分</span>
          </div>
          <van-button
            type="success"
            size="mini"
            round
            @click="onExchange(item)"
          >兑换</van-button>
        </div>
      </div>

      <van-empty v-if="filteredGoods.length === 0" description="暂无商品" />
    </van-pull-refresh>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showDialog } from 'vant'
import { useUserStore } from '@/stores/user'
import { getPointsBalance, getPointsMallItems, exchangePointsGoods } from '@/api/points'

const router = useRouter()
const userStore = useUserStore()

const currentPoints = ref(0)
const refreshing = ref(false)
const activeCategory = ref(0)

const categories = [
  { key: 'all', name: '全部' },
  { key: 'health', name: '养生好物' },
  { key: 'service', name: '服务体验' },
  { key: 'coupon', name: '优惠券' }
]

interface GoodsItem {
  id: number
  name: string
  points: number
  icon: string
  category: string
}

// 模拟数据（API 失败时回退使用）
const mockGoodsList: GoodsItem[] = [
  { id: 1, name: '艾灸体验券', points: 500, icon: '🔥', category: 'service' },
  { id: 2, name: '足浴养生套餐', points: 800, icon: '🦶', category: 'service' },
  { id: 3, name: '肩颈推拿体验', points: 600, icon: '💆', category: 'service' },
  { id: 4, name: '养生茶礼盒', points: 1200, icon: '🍵', category: 'health' },
  { id: 5, name: '足浴药包', points: 300, icon: '🌿', category: 'health' },
  { id: 6, name: '艾灸条套装', points: 450, icon: '🕯️', category: 'health' },
  { id: 7, name: '满100减20券', points: 200, icon: '🎫', category: 'coupon' },
  { id: 8, name: '满200减50券', points: 400, icon: '🎫', category: 'coupon' },
  { id: 9, name: '拔罐体验券', points: 350, icon: '🔴', category: 'service' },
  { id: 10, name: '精油香薰', points: 900, icon: '🫧', category: 'health' }
]

const goodsList = ref<GoodsItem[]>([])

const filteredGoods = computed(() => {
  const cat = categories[activeCategory.value].key
  if (cat === 'all') return goodsList.value
  return goodsList.value.filter((item) => item.category === cat)
})

onMounted(async () => {
  await loadPointsAndGoods()
})

async function loadPointsAndGoods() {
  // 加载积分余额
  try {
    const res: any = await getPointsBalance()
    currentPoints.value = res.data?.points ?? res.data ?? 0
  } catch {
    if (userStore.userInfo?.points !== undefined) {
      currentPoints.value = userStore.userInfo.points || 0
    }
  }

  // 加载商品列表
  try {
    const res: any = await getPointsMallItems()
    const list = res.data?.list || res.data || []
    goodsList.value = list
  } catch {
    // API 失败时回退到 mock 数据
    goodsList.value = mockGoodsList
  }
}

function onCategoryChange() {
  // 筛选由computed处理
}

async function onExchange(item: GoodsItem) {
  try {
    await showDialog({
      title: '确认兑换',
      message: `确定使用 ${item.points} 积分兑换「${item.name}」吗？`,
      showCancelButton: true,
      confirmButtonText: '确认兑换',
      cancelButtonText: '再想想'
    })
    await exchangePointsGoods(item.id)
    showToast('兑换成功')
    // 兑换成功后刷新积分和商品
    await loadPointsAndGoods()
  } catch {
    // 用户取消或兑换失败
  }
}

async function onRefresh() {
  await loadPointsAndGoods()
  refreshing.value = false
}
</script>

<style scoped lang="scss">
.points-mall-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.points-header {
  padding: 16px;

  .points-card {
    background: linear-gradient(135deg, #07C160, #06ad56);
    border-radius: 12px;
    padding: 24px 20px;
    color: #fff;
    text-align: center;

    .points-label {
      font-size: 14px;
      opacity: 0.85;
    }

    .points-value {
      font-size: 36px;
      font-weight: 700;
      margin-top: 8px;
    }
  }
}

.category-filter {
  margin-bottom: 8px;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  padding: 0 12px 12px;
}

.goods-item {
  background: #fff;
  border-radius: 10px;
  padding: 14px;
  display: flex;
  flex-direction: column;
  align-items: center;

  .goods-img {
    width: 80px;
    height: 80px;
    margin-bottom: 8px;

    .goods-img-placeholder {
      width: 100%;
      height: 100%;
      background: linear-gradient(135deg, #e8f5e9, #c8e6c9);
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 36px;
    }
  }

  .goods-name {
    font-size: 14px;
    font-weight: 500;
    color: #303133;
    text-align: center;
    margin-bottom: 4px;
  }

  .goods-points {
    margin-bottom: 8px;

    .points-num {
      font-size: 16px;
      font-weight: 700;
      color: #FA5151;
    }

    .points-unit {
      font-size: 12px;
      color: #909399;
      margin-left: 2px;
    }
  }
}
</style>
