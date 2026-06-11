<template>
  <div class="member-detail-page">
    <el-page-header @back="goBack" title="返回" content="会员详情" />

    <div v-loading="loading" style="margin-top: 16px">
      <!-- 会员基本信息卡片 -->
      <el-card shadow="never" v-if="memberInfo">
        <div class="member-card">
          <el-avatar :size="64" style="background: #409eff; font-size: 24px">
            {{ memberInfo.name?.charAt(0) || '?' }}
          </el-avatar>
          <div class="member-info">
            <div class="member-name">{{ memberInfo.name }}</div>
            <div class="member-meta">
              <span>{{ memberInfo.phone }}</span>
              <el-tag size="small" style="margin-left: 8px">{{ memberInfo.levelName || '普通会员' }}</el-tag>
            </div>
          </div>
          <div class="member-stats">
            <div class="stat-item">
              <div class="stat-value">{{ memberInfo.points || 0 }}</div>
              <div class="stat-label">积分</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">¥{{ memberInfo.balance || '0.00' }}</div>
              <div class="stat-label">余额</div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- Tab 切换 -->
      <el-tabs v-model="activeTab" style="margin-top: 16px" v-if="memberInfo">
        <!-- 预约记录 -->
        <el-tab-pane label="预约记录" name="appointments">
          <el-table :data="appointments" size="small" stripe v-loading="tabLoading">
            <el-table-column prop="appointmentTime" label="预约时间" width="160" />
            <el-table-column prop="serviceName" label="服务项目" />
            <el-table-column prop="technicianName" label="技师" width="100" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="appointmentStatusType(row.status)" size="small">
                  {{ appointmentStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" />
          </el-table>
          <div v-if="appointments.length === 0 && !tabLoading" class="empty-text">暂无预约记录</div>
        </el-tab-pane>

        <!-- 消费记录 -->
        <el-tab-pane label="消费记录" name="orders">
          <el-table :data="orders" size="small" stripe v-loading="tabLoading">
            <el-table-column prop="orderNo" label="订单号" width="160" />
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
          <div v-if="orders.length === 0 && !tabLoading" class="empty-text">暂无消费记录</div>
        </el-tab-pane>

        <!-- 健康档案 -->
        <el-tab-pane label="健康档案" name="health">
          <div v-loading="tabLoading">
            <template v-if="healthProfile">
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="身高">{{ healthProfile.height }} cm</el-descriptions-item>
                <el-descriptions-item label="体重">{{ healthProfile.weight }} kg</el-descriptions-item>
                <el-descriptions-item label="血型">{{ healthProfile.bloodType || '未填写' }}</el-descriptions-item>
                <el-descriptions-item label="过敏史">{{ healthProfile.allergies || '无' }}</el-descriptions-item>
                <el-descriptions-item label="既往病史" :span="2">{{ healthProfile.medicalHistory || '无' }}</el-descriptions-item>
                <el-descriptions-item label="健康备注" :span="2">{{ healthProfile.notes || '无' }}</el-descriptions-item>
                <el-descriptions-item label="更新时间">{{ healthProfile.updatedAt }}</el-descriptions-item>
              </el-descriptions>
            </template>
            <div v-else class="empty-text">暂无健康档案</div>
          </div>
        </el-tab-pane>

        <!-- 疗程卡 -->
        <el-tab-pane label="疗程卡" name="cards">
          <el-table :data="treatmentCards" size="small" stripe v-loading="tabLoading">
            <el-table-column prop="cardName" label="卡名" />
            <el-table-column prop="serviceName" label="适用项目" />
            <el-table-column prop="totalCount" label="总次数" width="80" />
            <el-table-column prop="remainCount" label="剩余次数" width="80">
              <template #default="{ row }">
                <span :style="{ color: row.remainCount <= 3 ? '#f56c6c' : '' }">{{ row.remainCount }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="expireDate" label="到期日" width="120" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
                  {{ row.status === 'active' ? '有效' : '已过期' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="treatmentCards.length === 0 && !tabLoading" class="empty-text">暂无疗程卡</div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMemberDetail, getMemberHealthProfile } from '@/api/member'
import { get } from '@/utils/request'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const tabLoading = ref(false)
const memberInfo = ref<any>(null)
const healthProfile = ref<any>(null)
const activeTab = ref('appointments')
const appointments = ref<any[]>([])
const orders = ref<any[]>([])
const treatmentCards = ref<any[]>([])

const memberId = ref<string>('')

function goBack() {
  router.back()
}

function appointmentStatusType(status: string) {
  const map: Record<string, string> = {
    pending: 'warning', confirmed: '', in_service: 'success', completed: 'info', cancelled: 'danger'
  }
  return map[status] || ''
}

function appointmentStatusLabel(status: string) {
  const map: Record<string, string> = {
    pending: '待确认', confirmed: '已确认', in_service: '服务中', completed: '已完成', cancelled: '已取消'
  }
  return map[status] || status
}

async function loadMemberDetail() {
  loading.value = true
  try {
    const res: any = await getMemberDetail(memberId.value)
    memberInfo.value = res.data || null
  } catch {
    ElMessage.error('获取会员信息失败')
  } finally {
    loading.value = false
  }
}

async function loadTabData() {
  if (!memberId.value) return
  tabLoading.value = true
  try {
    if (activeTab.value === 'appointments') {
      const res: any = await get('/api/v1/trade/appointments/my', { params: { memberId: memberId.value, page: 1, pageSize: 20 } })
      appointments.value = res.data?.list || res.data?.records || []
    } else if (activeTab.value === 'orders') {
      const res: any = await get('/api/v1/trade/orders', { params: { memberId: memberId.value, page: 1, pageSize: 20 } })
      orders.value = res.data?.list || res.data?.records || []
    } else if (activeTab.value === 'health') {
      try {
        const res: any = await getMemberHealthProfile(Number(memberId.value))
        healthProfile.value = res.data || null
      } catch {
        healthProfile.value = null
      }
    } else if (activeTab.value === 'cards') {
      const res: any = await get('/api/v1/trade/treatment-cards/my', { params: { memberId: memberId.value, page: 1, pageSize: 50 } })
      treatmentCards.value = res.data?.list || res.data?.records || []
    }
  } catch {
    // handled by interceptor
  } finally {
    tabLoading.value = false
  }
}

watch(activeTab, () => {
  loadTabData()
})

onMounted(() => {
  memberId.value = route.params.id as string
  if (memberId.value) {
    loadMemberDetail()
    loadTabData()
  }
})
</script>

<style scoped>
.member-detail-page {
  padding: 4px;
}
.member-card {
  display: flex;
  align-items: center;
  gap: 20px;
}
.member-info {
  flex: 1;
}
.member-name {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}
.member-meta {
  font-size: 14px;
  color: #909399;
  display: flex;
  align-items: center;
}
.member-stats {
  display: flex;
  gap: 32px;
}
.stat-item {
  text-align: center;
}
.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
}
.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
.empty-text {
  color: #909399;
  text-align: center;
  padding: 40px 0;
}
</style>
