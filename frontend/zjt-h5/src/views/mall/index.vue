<template>
  <div class="mall-page">
    <van-nav-bar title="忠济堂商场" :border="false" />

    <!-- Banner区域 -->
    <div class="banner">
      <div class="banner-content">
        <h2 class="banner-title">养生好物</h2>
        <p class="banner-subtitle">精选推荐</p>
      </div>
    </div>

    <!-- 分类Tab -->
    <van-tabs v-model:active="activeTab" animated sticky offset-top="46" color="#07C160" title-active-color="#07C160">
      <van-tab title="疗程卡">
        <div v-if="cardLoading" style="text-align: center; padding: 40px 0;">
          <van-loading size="24px">加载中...</van-loading>
        </div>
        <div class="product-list" v-else-if="cards.length > 0">
          <div class="product-card" v-for="item in cards" :key="item.id">
            <div class="card-icon">🃏</div>
            <div class="card-info">
              <div class="card-name">{{ item.cardName || item.name || '疗程卡' }}</div>
              <div class="card-detail">
                <span class="card-remain">剩余次数：{{ item.remainingCount ?? item.totalCount ?? '--' }}</span>
                <span class="card-expire" v-if="item.expiryDate">有效期至：{{ item.expiryDate }}</span>
              </div>
            </div>
            <van-button type="primary" size="small" round @click="handleBuyCard(item)">购买</van-button>
          </div>
        </div>
        <van-empty v-else description="暂无疗程卡" />
      </van-tab>

      <van-tab title="优惠券">
        <div v-if="couponLoading" style="text-align: center; padding: 40px 0;">
          <van-loading size="24px">加载中...</van-loading>
        </div>
        <div class="product-list" v-else-if="coupons.length > 0">
          <div class="coupon-card" v-for="item in coupons" :key="item.id">
            <div class="coupon-left">
              <div class="coupon-value">
                <template v-if="item.discountType === 1 || item.type === 1">
                  <span class="coupon-unit">¥</span>{{ item.discountValue || item.amount || 0 }}
                </template>
                <template v-else>
                  {{ item.discountValue || item.discount || 0 }}<span class="coupon-unit">折</span>
                </template>
              </div>
              <div class="coupon-threshold">满{{ item.threshold || item.minAmount || 0 }}可用</div>
            </div>
            <div class="coupon-right">
              <div class="coupon-name">{{ item.couponName || item.name || '优惠券' }}</div>
              <div class="coupon-remain" v-if="item.remainingQuantity != null">剩余 {{ item.remainingQuantity }} 张</div>
              <van-button type="primary" size="small" round :disabled="item.claimed" @click="handleClaimCoupon(item)">
                {{ item.claimed ? '已领取' : '领取' }}
              </van-button>
            </div>
          </div>
        </div>
        <van-empty v-else description="暂无优惠券" />
      </van-tab>

      <van-tab title="服务项目">
        <div v-if="serviceLoading" style="text-align: center; padding: 40px 0;">
          <van-loading size="24px">加载中...</van-loading>
        </div>
        <div class="product-list" v-else-if="services.length > 0">
          <div class="product-card" v-for="item in services" :key="item.id">
            <div class="card-icon">💆</div>
            <div class="card-info">
              <div class="card-name">{{ item.itemName || item.name || '服务项目' }}</div>
              <div class="card-detail">
                <span class="service-price">¥{{ item.price ?? '--' }}</span>
                <span class="service-duration" v-if="item.duration">{{ item.duration }}分钟</span>
              </div>
              <div class="card-desc" v-if="item.description">{{ item.description }}</div>
            </div>
            <van-button type="primary" size="small" round @click="handleBookService(item)">预约</van-button>
          </div>
        </div>
        <van-empty v-else description="暂无服务项目" />
      </van-tab>
    </van-tabs>

    <!-- 底部TabBar占位 -->
    <div class="tabbar-placeholder"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getServiceItemList } from '@/api/service'
import { getCouponList, claimCoupon } from '@/api/coupon'
import { getMyCards } from '@/api/treatment'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref(0)

const cards = ref<any[]>([])
const coupons = ref<any[]>([])
const services = ref<any[]>([])

const cardLoading = ref(false)
const couponLoading = ref(false)
const serviceLoading = ref(false)

onMounted(() => {
  loadCards()
  loadCoupons()
  loadServices()
})

async function loadCards() {
  cardLoading.value = true
  try {
    const memberId = userStore.userInfo?.id
    if (!memberId) {
      cards.value = []
      return
    }
    const res: any = await getMyCards(memberId)
    cards.value = res.data?.list || res.data || []
  } catch {
    cards.value = []
  } finally {
    cardLoading.value = false
  }
}

async function loadCoupons() {
  couponLoading.value = true
  try {
    const res: any = await getCouponList({ page: 1, pageSize: 50 })
    coupons.value = (res.data?.list || res.data?.records || res.data || []).map((item: any) => ({
      ...item,
      claimed: false
    }))
  } catch {
    coupons.value = []
  } finally {
    couponLoading.value = false
  }
}

async function loadServices() {
  serviceLoading.value = true
  try {
    const res: any = await getServiceItemList({ status: 1 })
    services.value = res.data?.list || res.data || []
  } catch {
    services.value = []
  } finally {
    serviceLoading.value = false
  }
}

async function handleClaimCoupon(item: any) {
  const memberId = userStore.userInfo?.id
  if (!memberId) {
    showToast('请先登录')
    router.push('/login')
    return
  }
  try {
    await claimCoupon(item.id, memberId)
    showToast('领取成功')
    item.claimed = true
  } catch {
    showToast('领取失败，请稍后重试')
  }
}

function handleBuyCard(item: any) {
  showToast('购买功能开发中')
}

function handleBookService(item: any) {
  router.push('/appointment/step1')
}
</script>

<style scoped lang="scss">
.mall-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.banner {
  background: linear-gradient(135deg, #07C160, #06ad56);
  padding: 30px 20px;
  color: #fff;

  .banner-content {
    text-align: center;

    .banner-title {
      font-size: 24px;
      font-weight: 700;
      margin: 0 0 6px;
    }

    .banner-subtitle {
      font-size: 14px;
      opacity: 0.85;
      margin: 0;
    }
  }
}

.product-list {
  padding: 12px;
}

.product-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 10px;
  padding: 14px;
  margin-bottom: 10px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);

  .card-icon {
    font-size: 32px;
    margin-right: 12px;
    flex-shrink: 0;
  }

  .card-info {
    flex: 1;
    min-width: 0;

    .card-name {
      font-size: 15px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 4px;
    }

    .card-detail {
      font-size: 12px;
      color: #909399;
      display: flex;
      gap: 10px;
      flex-wrap: wrap;

      .service-price {
        color: #07C160;
        font-size: 14px;
        font-weight: 600;
      }

      .service-duration {
        color: #909399;
      }
    }

    .card-desc {
      font-size: 12px;
      color: #909399;
      margin-top: 4px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

.coupon-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 10px;
  margin-bottom: 10px;
  overflow: hidden;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);

  .coupon-left {
    width: 100px;
    padding: 14px 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #07C160, #06ad56);
    color: #fff;
    flex-shrink: 0;

    .coupon-value {
      font-size: 24px;
      font-weight: 700;

      .coupon-unit {
        font-size: 12px;
        font-weight: 400;
      }
    }

    .coupon-threshold {
      font-size: 10px;
      opacity: 0.85;
      margin-top: 2px;
    }
  }

  .coupon-right {
    flex: 1;
    padding: 14px;
    display: flex;
    flex-direction: column;
    align-items: flex-start;

    .coupon-name {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 4px;
    }

    .coupon-remain {
      font-size: 11px;
      color: #909399;
      margin-bottom: 8px;
    }
  }
}

.tabbar-placeholder {
  height: 60px;
}
</style>
