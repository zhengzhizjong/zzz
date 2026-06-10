<template>
  <div class="cashier-page">
    <el-row :gutter="16">
      <!-- 左侧：选择预约 -->
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>
            <span>选择预约</span>
          </template>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索客户姓名/手机号"
            prefix-icon="Search"
            clearable
            style="margin-bottom: 12px"
            @input="loadAppointments"
          />
          <div v-loading="loading" class="appointment-list">
            <div
              v-for="item in appointments"
              :key="item.id"
              :class="['appointment-item', { active: selectedAppointment?.id === item.id }]"
              @click="selectAppointment(item)"
            >
              <div class="item-top">
                <span class="customer-name">{{ item.customerName }}</span>
                <el-tag :type="statusTagType(item.status)" size="small">{{ statusLabel(item.status) }}</el-tag>
              </div>
              <div class="item-bottom">
                <span>{{ item.serviceName }}</span>
                <span>{{ item.technicianName }}</span>
              </div>
            </div>
            <div v-if="appointments.length === 0" class="empty-text">暂无已完成预约</div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：订单详情与支付 -->
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <span>收银台</span>
          </template>
          <div v-if="!selectedAppointment" class="empty-text">请先选择已完成的预约</div>
          <div v-else>
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="客户">{{ selectedAppointment.customerName }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ selectedAppointment.customerPhone }}</el-descriptions-item>
              <el-descriptions-item label="服务项目">{{ selectedAppointment.serviceName }}</el-descriptions-item>
              <el-descriptions-item label="技师">{{ selectedAppointment.technicianName }}</el-descriptions-item>
              <el-descriptions-item label="预约时间">{{ selectedAppointment.appointmentTime }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="statusTagType(selectedAppointment.status)" size="small">
                  {{ statusLabel(selectedAppointment.status) }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>

            <div v-if="orderInfo" style="margin-top: 16px">
              <el-divider content-position="left">订单信息</el-divider>
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="订单号">{{ orderInfo.orderNo }}</el-descriptions-item>
                <el-descriptions-item label="订单状态">
                  <el-tag :type="orderInfo.status === 'paid' ? 'success' : 'warning'" size="small">
                    {{ orderInfo.status === 'paid' ? '已支付' : '待支付' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="金额">
                  <span class="amount">¥{{ orderInfo.totalAmount }}</span>
                </el-descriptions-item>
              </el-descriptions>
            </div>

            <div v-if="!orderInfo" style="margin-top: 20px; text-align: center">
              <el-button type="primary" size="large" :loading="creatingOrder" @click="handleCreateOrder">
                生成订单
              </el-button>
            </div>

            <div v-if="orderInfo && orderInfo.status !== 'paid'" style="margin-top: 20px">
              <el-divider content-position="left">支付方式</el-divider>
              <el-radio-group v-model="payMethod" style="margin-bottom: 16px">
                <el-radio-button value="cash">现金</el-radio-button>
                <el-radio-button value="wechat">微信</el-radio-button>
                <el-radio-button value="alipay">支付宝</el-radio-button>
                <el-radio-button value="card">银行卡</el-radio-button>
                <el-radio-button value="member_card">会员卡</el-radio-button>
              </el-radio-group>
              <div style="text-align: center">
                <el-button type="success" size="large" :loading="paying" @click="handlePay">
                  确认收款 ¥{{ orderInfo.totalAmount }}
                </el-button>
              </div>
            </div>

            <div v-if="orderInfo && orderInfo.status === 'paid'" style="margin-top: 20px; text-align: center">
              <el-result icon="success" title="支付成功" sub-title="订单已完成支付" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAppointmentList } from '@/api/appointment'
import { createOrderFromAppointment, getOrderDetail } from '@/api/order'
import { post } from '@/utils/request'
import { getStoreId } from '@/utils/auth'

const route = useRoute()
const loading = ref(false)
const creatingOrder = ref(false)
const paying = ref(false)
const searchKeyword = ref('')
const appointments = ref<any[]>([])
const selectedAppointment = ref<any>(null)
const orderInfo = ref<any>(null)
const payMethod = ref('cash')

function statusTagType(status: string) {
  const map: Record<string, string> = {
    pending: 'warning', confirmed: '', in_service: 'success', completed: 'info', cancelled: 'danger'
  }
  return map[status] || ''
}

function statusLabel(status: string) {
  const map: Record<string, string> = {
    pending: '待确认', confirmed: '已确认', in_service: '服务中', completed: '已完成', cancelled: '已取消'
  }
  return map[status] || status
}

async function loadAppointments() {
  loading.value = true
  try {
    const storeId = getStoreId() || ''
    const params: any = {
      storeId,
      status: 'completed',
      page: 1,
      pageSize: 50
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    const res: any = await getAppointmentList(params)
    appointments.value = res.data?.list || res.data?.records || []
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

async function selectAppointment(item: any) {
  selectedAppointment.value = item
  orderInfo.value = null
  // 尝试获取已有订单
  try {
    const res: any = await getOrderDetail(item.id)
    if (res.data) {
      orderInfo.value = res.data
    }
  } catch {
    // no existing order
  }
}

async function handleCreateOrder() {
  if (!selectedAppointment.value) return
  creatingOrder.value = true
  try {
    const res: any = await createOrderFromAppointment(selectedAppointment.value.id)
    orderInfo.value = res.data
    ElMessage.success('订单创建成功')
  } catch {
    // handled by interceptor
  } finally {
    creatingOrder.value = false
  }
}

async function handlePay() {
  if (!orderInfo.value) return
  paying.value = true
  try {
    await post(`/api/v1/trade/orders/${orderInfo.value.id}/pay`, {
      paymentMethod: payMethod.value
    })
    ElMessage.success('支付成功')
    orderInfo.value.status = 'paid'
  } catch {
    // handled by interceptor
  } finally {
    paying.value = false
  }
}

onMounted(() => {
  loadAppointments()
  // 如果从预约页面跳转过来，自动选中
  const appointmentId = route.query.appointmentId as string
  if (appointmentId) {
    const found = appointments.value.find((a) => String(a.id) === appointmentId)
    if (found) {
      selectAppointment(found)
    }
  }
})
</script>

<style scoped>
.cashier-page {
  padding: 4px;
}
.appointment-list {
  max-height: 500px;
  overflow-y: auto;
}
.appointment-item {
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.appointment-item:hover {
  border-color: #409eff;
}
.appointment-item.active {
  border-color: #409eff;
  background: #ecf5ff;
}
.item-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.customer-name {
  font-weight: 600;
  color: #303133;
}
.item-bottom {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #909399;
}
.empty-text {
  color: #909399;
  text-align: center;
  padding: 40px 0;
}
.amount {
  font-size: 20px;
  font-weight: 700;
  color: #f56c6c;
}
</style>
