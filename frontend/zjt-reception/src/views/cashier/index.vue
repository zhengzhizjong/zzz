<template>
  <div class="cashier-page">
    <el-row :gutter="16">
      <!-- 左侧：会员查询 + 服务选择 -->
      <el-col :span="12">
        <!-- 会员查询 -->
        <el-card shadow="never" style="margin-bottom: 12px">
          <template #header>
            <span>会员查询</span>
          </template>
          <el-input
            v-model="memberPhone"
            placeholder="扫码或输入手机号查询会员"
            prefix-icon="Search"
            clearable
            @keyup.enter="searchMember"
          >
            <template #append>
              <el-button @click="searchMember" :loading="memberLoading">查询</el-button>
            </template>
          </el-input>
          <div v-if="memberInfo" class="member-info-area">
            <el-descriptions :column="3" border size="small" style="margin-top: 12px">
              <el-descriptions-item label="姓名">{{ memberInfo.name }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ memberInfo.phone }}</el-descriptions-item>
              <el-descriptions-item label="等级">
                <el-tag size="small">{{ memberInfo.levelName || '普通会员' }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="余额">¥{{ memberInfo.balance || '0.00' }}</el-descriptions-item>
              <el-descriptions-item label="积分">{{ memberInfo.points || 0 }}</el-descriptions-item>
              <el-descriptions-item label="">
                <el-button type="primary" link size="small" @click="goMemberDetail">查看详情</el-button>
              </el-descriptions-item>
            </el-descriptions>
          </div>
          <div v-else-if="memberSearched && !memberLoading" class="empty-text-sm">未找到会员，将作为散客结算</div>
        </el-card>

        <!-- 服务项目选择 -->
        <el-card shadow="never" style="margin-bottom: 12px">
          <template #header>
            <span>选择服务项目</span>
          </template>
          <div v-loading="servicesLoading">
            <el-checkbox-group v-model="selectedServiceIds">
              <div v-for="svc in services" :key="svc.id" class="service-item">
                <el-checkbox :value="svc.id">
                  <span class="svc-name">{{ svc.name }}</span>
                  <span class="svc-price">¥{{ svc.price }}</span>
                  <span class="svc-duration">{{ svc.duration }}分钟</span>
                </el-checkbox>
              </div>
            </el-checkbox-group>
            <div v-if="services.length === 0 && !servicesLoading" class="empty-text-sm">暂无服务项目</div>
          </div>
        </el-card>

        <!-- 技师选择 -->
        <el-card shadow="never">
          <template #header>
            <span>选择技师（可选）</span>
          </template>
          <el-select v-model="selectedTechnicianId" placeholder="不指定技师" clearable style="width: 100%">
            <el-option label="不指定技师" :value="0" />
            <el-option
              v-for="tech in technicians"
              :key="tech.id"
              :label="tech.name"
              :value="tech.id"
            >
              <span>{{ tech.name }}</span>
              <el-tag v-if="tech.onDuty" type="success" size="small" style="margin-left: 8px">在岗</el-tag>
            </el-option>
          </el-select>
        </el-card>
      </el-col>

      <!-- 右侧：优惠、疗程卡、结算 -->
      <el-col :span="12">
        <!-- 优惠券选择 -->
        <el-card shadow="never" style="margin-bottom: 12px" v-if="memberInfo">
          <template #header>
            <span>使用优惠券</span>
          </template>
          <el-select v-model="selectedCouponId" placeholder="不使用优惠券" clearable style="width: 100%">
            <el-option
              v-for="coupon in availableCoupons"
              :key="coupon.id"
              :label="coupon.couponName"
              :value="coupon.id"
            >
              <span>{{ coupon.couponName }}</span>
              <span style="float: right; color: #f56c6c">-¥{{ coupon.discountAmount }}</span>
            </el-option>
          </el-select>
          <div v-if="availableCoupons.length === 0" class="empty-text-sm">暂无可用优惠券</div>
        </el-card>

        <!-- 疗程卡选择 -->
        <el-card shadow="never" style="margin-bottom: 12px" v-if="memberInfo">
          <template #header>
            <span>使用疗程卡</span>
          </template>
          <el-select v-model="selectedCardId" placeholder="不使用疗程卡" clearable style="width: 100%">
            <el-option
              v-for="card in availableCards"
              :key="card.id"
              :label="card.cardName"
              :value="card.id"
            >
              <span>{{ card.cardName }}</span>
              <span style="float: right; color: #67c23a">剩余{{ card.remainCount }}次</span>
            </el-option>
          </el-select>
          <div v-if="availableCards.length === 0" class="empty-text-sm">暂无可用疗程卡</div>
        </el-card>

        <!-- 费用明细与结算 -->
        <el-card shadow="never">
          <template #header>
            <span>费用明细</span>
          </template>
          <div class="price-detail">
            <div class="price-row">
              <span>服务原价</span>
              <span>¥{{ originalPrice.toFixed(2) }}</span>
            </div>
            <div class="price-row" v-if="couponDiscount > 0">
              <span>优惠券抵扣</span>
              <span class="discount">-¥{{ couponDiscount.toFixed(2) }}</span>
            </div>
            <div class="price-row" v-if="selectedCardId">
              <span>疗程卡抵扣</span>
              <span class="discount">-¥{{ cardDeduct.toFixed(2) }}</span>
            </div>
            <el-divider />
            <div class="price-row total">
              <span>应付金额</span>
              <span class="total-amount">¥{{ finalPrice.toFixed(2) }}</span>
            </div>
          </div>

          <div style="margin-top: 16px">
            <div class="pay-label">支付方式</div>
            <el-radio-group v-model="payMethod" style="margin-bottom: 16px">
              <el-radio-button value="cash">现金</el-radio-button>
              <el-radio-button value="wechat">微信</el-radio-button>
              <el-radio-button value="alipay">支付宝</el-radio-button>
            </el-radio-group>
          </div>

          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="submitting"
            :disabled="selectedServiceIds.length === 0"
            @click="handleSubmit"
          >
            确认收款 ¥{{ finalPrice.toFixed(2) }}
          </el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMemberList, getMemberDetail } from '@/api/member'
import { getTechnicianList } from '@/api/technician'
import { get, post } from '@/utils/request'
import { getStoreId } from '@/utils/auth'

const route = useRoute()
const router = useRouter()

// 会员
const memberPhone = ref('')
const memberLoading = ref(false)
const memberSearched = ref(false)
const memberInfo = ref<any>(null)

// 服务项目
const servicesLoading = ref(false)
const services = ref<any[]>([])
const selectedServiceIds = ref<number[]>([])

// 技师
const technicians = ref<any[]>([])
const selectedTechnicianId = ref<number | undefined>(undefined)

// 优惠券
const availableCoupons = ref<any[]>([])
const selectedCouponId = ref<number | undefined>(undefined)

// 疗程卡
const availableCards = ref<any[]>([])
const selectedCardId = ref<number | undefined>(undefined)

// 支付
const payMethod = ref('cash')
const submitting = ref(false)

// 计算
const originalPrice = computed(() => {
  return selectedServiceIds.value.reduce((sum, id) => {
    const svc = services.value.find(s => s.id === id)
    return sum + (svc ? Number(svc.price) || 0 : 0)
  }, 0)
})

const couponDiscount = computed(() => {
  if (!selectedCouponId.value) return 0
  const coupon = availableCoupons.value.find(c => c.id === selectedCouponId.value)
  return coupon ? Number(coupon.discountAmount) || 0 : 0
})

const cardDeduct = computed(() => {
  if (!selectedCardId.value) return 0
  // 疗程卡抵扣：选中服务项目的原价
  return originalPrice.value
})

const finalPrice = computed(() => {
  let price = originalPrice.value
  if (selectedCardId.value) {
    // 使用疗程卡时，抵扣全部服务价格
    price = 0
  } else {
    price -= couponDiscount.value
  }
  return Math.max(0, price)
})

async function searchMember() {
  if (!memberPhone.value) {
    ElMessage.warning('请输入手机号')
    return
  }
  memberLoading.value = true
  memberSearched.value = true
  memberInfo.value = null
  availableCoupons.value = []
  availableCards.value = []
  selectedCouponId.value = undefined
  selectedCardId.value = undefined
  try {
    const res: any = await getMemberList({ phone: memberPhone.value, page: 1, pageSize: 1 })
    const list = res.data?.list || res.data?.records || []
    if (list.length === 0) {
      return
    }
    const member = list[0]
    const detailRes: any = await getMemberDetail(member.id)
    memberInfo.value = detailRes.data || member
    await loadMemberCouponsAndCards(member.id)
  } catch {
    // handled by interceptor
  } finally {
    memberLoading.value = false
  }
}

async function loadMemberCouponsAndCards(memberId: number | string) {
  try {
    const [couponsRes, cardsRes] = await Promise.all([
      get('/api/v1/trade/coupons', { params: { memberId, status: 'available', page: 1, pageSize: 50 } }).catch(() => ({ data: { list: [] } })),
      get('/api/v1/trade/treatment-cards/my', { params: { memberId, status: 'active', page: 1, pageSize: 50 } }).catch(() => ({ data: { list: [] } }))
    ])
    availableCoupons.value = (couponsRes as any).data?.list || (couponsRes as any).data?.records || []
    availableCards.value = (cardsRes as any).data?.list || (cardsRes as any).data?.records || []
  } catch {
    // silently handle
  }
}

async function loadServices() {
  servicesLoading.value = true
  try {
    const res: any = await get('/api/v1/store/services', { params: { page: 1, pageSize: 100 } })
    services.value = res.data?.list || res.data?.records || []
  } catch {
    // handled by interceptor
  } finally {
    servicesLoading.value = false
  }
}

async function loadTechnicians() {
  const storeId = getStoreId() || ''
  try {
    const res: any = await getTechnicianList({ storeId, page: 1, pageSize: 50 })
    technicians.value = res.data?.list || res.data?.records || []
  } catch {
    // handled by interceptor
  }
}

function goMemberDetail() {
  if (memberInfo.value) {
    router.push(`/member/detail/${memberInfo.value.id}`)
  }
}

async function handleSubmit() {
  if (selectedServiceIds.value.length === 0) {
    ElMessage.warning('请选择服务项目')
    return
  }
  submitting.value = true
  try {
    // 1. 创建订单
    const orderData: any = {
      serviceIds: selectedServiceIds.value,
      technicianId: selectedTechnicianId.value || undefined,
      couponId: selectedCouponId.value || undefined,
      treatmentCardId: selectedCardId.value || undefined,
      totalAmount: originalPrice.value,
      discountAmount: couponDiscount.value,
      payAmount: finalPrice.value
    }
    if (memberInfo.value) {
      orderData.memberId = memberInfo.value.id
    }
    const orderRes: any = await post('/api/v1/trade/orders', orderData)
    const orderId = orderRes.data?.id || orderRes.data

    // 2. 创建支付
    if (finalPrice.value > 0) {
      await post('/api/v1/trade/payments/create', {
        orderId,
        paymentMethod: payMethod.value,
        amount: finalPrice.value
      })
    }

    ElMessage.success('收款成功')
    // 3. 跳转到订单详情
    router.push(`/cashier?orderId=${orderId}`)
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadServices()
  loadTechnicians()
  // 如果从预约页面跳转
  const phoneParam = route.query.phone as string
  if (phoneParam) {
    memberPhone.value = phoneParam
    searchMember()
  }
})
</script>

<style scoped>
.cashier-page {
  padding: 4px;
}
.member-info-area {
  margin-top: 8px;
}
.service-item {
  margin-bottom: 8px;
}
.svc-name {
  font-weight: 500;
}
.svc-price {
  color: #f56c6c;
  margin-left: 12px;
}
.svc-duration {
  color: #909399;
  margin-left: 8px;
  font-size: 12px;
}
.price-detail {
  padding: 0 4px;
}
.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}
.price-row.total {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.discount {
  color: #67c23a;
}
.total-amount {
  font-size: 24px;
  font-weight: 700;
  color: #f56c6c;
}
.pay-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}
.empty-text-sm {
  color: #909399;
  text-align: center;
  padding: 12px 0;
  font-size: 13px;
}
</style>
