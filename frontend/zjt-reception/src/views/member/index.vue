<template>
  <div class="member-page">
    <el-card shadow="never">
      <div class="search-area">
        <el-input
          v-model="phone"
          placeholder="请输入手机号查询会员"
          prefix-icon="Phone"
          clearable
          style="width: 300px"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button :icon="Search" @click="handleSearch" />
          </template>
        </el-input>
      </div>

      <div v-if="!memberInfo && !loading" class="empty-text">请输入手机号查询会员信息</div>

      <div v-loading="loading">
        <template v-if="memberInfo">
          <el-divider content-position="left">会员信息</el-divider>
          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="姓名">{{ memberInfo.name }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ memberInfo.phone }}</el-descriptions-item>
            <el-descriptions-item label="会员等级">
              <el-tag size="small">{{ memberInfo.levelName || '普通会员' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="余额">¥{{ memberInfo.balance || '0.00' }}</el-descriptions-item>
            <el-descriptions-item label="积分">{{ memberInfo.points || 0 }}</el-descriptions-item>
            <el-descriptions-item label="注册时间">{{ memberInfo.createdAt }}</el-descriptions-item>
          </el-descriptions>

          <el-tabs v-model="activeTab" style="margin-top: 16px">
            <el-tab-pane label="消费记录" name="orders">
              <el-table :data="orders" size="small" stripe>
                <el-table-column prop="orderNo" label="订单号" width="140" />
                <el-table-column prop="serviceName" label="服务项目" />
                <el-table-column prop="totalAmount" label="金额" width="100">
                  <template #default="{ row }">¥{{ row.totalAmount }}</template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="80">
                  <template #default="{ row }">
                    <el-tag :type="row.status === 'paid' ? 'success' : 'warning'" size="small">
                      {{ row.status === 'paid' ? '已付' : '待付' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createdAt" label="时间" width="160" />
              </el-table>
              <div v-if="orders.length === 0" class="empty-text">暂无消费记录</div>
            </el-tab-pane>

            <el-tab-pane label="次卡" name="cards">
              <el-table :data="treatmentCards" size="small" stripe>
                <el-table-column prop="cardName" label="卡名" />
                <el-table-column prop="totalCount" label="总次数" width="80" />
                <el-table-column prop="remainCount" label="剩余次数" width="80" />
                <el-table-column prop="expireDate" label="到期日" width="120" />
                <el-table-column prop="status" label="状态" width="80">
                  <template #default="{ row }">
                    <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
                      {{ row.status === 'active' ? '有效' : '已过期' }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
              <div v-if="treatmentCards.length === 0" class="empty-text">暂无次卡</div>
            </el-tab-pane>

            <el-tab-pane label="优惠券" name="coupons">
              <el-table :data="coupons" size="small" stripe>
                <el-table-column prop="couponName" label="券名" />
                <el-table-column prop="discountAmount" label="优惠金额" width="100">
                  <template #default="{ row }">¥{{ row.discountAmount }}</template>
                </el-table-column>
                <el-table-column prop="expireDate" label="到期日" width="120" />
                <el-table-column prop="status" label="状态" width="80">
                  <template #default="{ row }">
                    <el-tag :type="row.status === 'available' ? 'success' : 'info'" size="small">
                      {{ row.status === 'available' ? '可用' : '已使用' }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
              <div v-if="coupons.length === 0" class="empty-text">暂无优惠券</div>
            </el-tab-pane>
          </el-tabs>
        </template>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getMemberList, getMemberDetail } from '@/api/member'
import { get } from '@/utils/request'

const route = useRoute()
const loading = ref(false)
const phone = ref('')
const memberInfo = ref<any>(null)
const activeTab = ref('orders')
const orders = ref<any[]>([])
const treatmentCards = ref<any[]>([])
const coupons = ref<any[]>([])

async function handleSearch() {
  if (!phone.value) {
    ElMessage.warning('请输入手机号')
    return
  }
  loading.value = true
  memberInfo.value = null
  orders.value = []
  treatmentCards.value = []
  coupons.value = []
  try {
    const res: any = await getMemberList({ phone: phone.value, page: 1, pageSize: 1 })
    const list = res.data?.list || res.data?.records || []
    if (list.length === 0) {
      ElMessage.info('未找到该会员')
      return
    }
    const member = list[0]
    const detailRes: any = await getMemberDetail(member.id)
    memberInfo.value = detailRes.data || member
    await loadMemberData(member.id)
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

async function loadMemberData(memberId: number | string) {
  try {
    const [ordersRes, cardsRes, couponsRes] = await Promise.all([
      get('/api/v1/trade/orders', { params: { memberId, page: 1, pageSize: 20 } }),
      get('/api/v1/trade/treatment-cards', { params: { memberId, page: 1, pageSize: 50 } }),
      get('/api/v1/trade/coupons', { params: { memberId, page: 1, pageSize: 50 } })
    ])
    orders.value = (ordersRes as any).data?.list || (ordersRes as any).data?.records || []
    treatmentCards.value = (cardsRes as any).data?.list || (cardsRes as any).data?.records || []
    coupons.value = (couponsRes as any).data?.list || (couponsRes as any).data?.records || []
  } catch {
    // silently handle
  }
}

onMounted(() => {
  const phoneParam = route.query.phone as string
  if (phoneParam) {
    phone.value = phoneParam
    handleSearch()
  }
})
</script>

<style scoped>
.member-page {
  padding: 4px;
}
.search-area {
  margin-bottom: 16px;
}
.empty-text {
  color: #909399;
  text-align: center;
  padding: 40px 0;
}
</style>
