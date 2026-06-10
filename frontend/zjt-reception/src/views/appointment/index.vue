<template>
  <div class="appointment-page">
    <el-card shadow="never">
      <div class="page-header">
        <div class="filter-area">
          <el-radio-group v-model="statusFilter" @change="loadAppointments">
            <el-radio-button value="">全部</el-radio-button>
            <el-radio-button value="pending">待确认</el-radio-button>
            <el-radio-button value="confirmed">已确认</el-radio-button>
            <el-radio-button value="in_service">服务中</el-radio-button>
            <el-radio-button value="completed">已完成</el-radio-button>
            <el-radio-button value="cancelled">已取消</el-radio-button>
          </el-radio-group>
        </div>
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon> 散客预约
        </el-button>
      </div>

      <el-table :data="appointments" v-loading="loading" stripe>
        <el-table-column prop="id" label="预约号" width="80" />
        <el-table-column prop="customerName" label="客户" width="100" />
        <el-table-column prop="customerPhone" label="手机号" width="130" />
        <el-table-column prop="serviceName" label="服务项目" />
        <el-table-column prop="technicianName" label="技师" width="80" />
        <el-table-column prop="appointmentTime" label="预约时间" width="160" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'confirmed' || row.status === 'pending'">
              <el-button type="primary" size="small" @click="handleCheckIn(row)">签到</el-button>
              <el-button type="danger" size="small" @click="handleCancel(row)">取消</el-button>
            </template>
            <template v-if="row.status === 'in_service'">
              <el-button type="success" size="small" @click="handleComplete(row)">完成</el-button>
            </template>
            <template v-if="row.status === 'completed'">
              <el-button type="warning" size="small" @click="handleCreateOrder(row)">收银</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-area">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="loadAppointments"
        />
      </div>
    </el-card>

    <!-- 散客预约弹窗 -->
    <el-dialog v-model="showCreateDialog" title="散客预约" width="500px" destroy-on-close>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="80px">
        <el-form-item label="客户姓名" prop="customerName">
          <el-input v-model="createForm.customerName" placeholder="请输入客户姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="customerPhone">
          <el-input v-model="createForm.customerPhone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="服务项目" prop="serviceId">
          <el-input v-model="createForm.serviceId" placeholder="服务项目ID" />
        </el-form-item>
        <el-form-item label="技师" prop="technicianId">
          <el-input v-model="createForm.technicianId" placeholder="技师ID（可选）" />
        </el-form-item>
        <el-form-item label="预约时间" prop="appointmentTime">
          <el-date-picker v-model="createForm.appointmentTime" type="datetime" placeholder="选择时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="2" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="handleCreateAppointment">确认预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { getAppointmentList, checkInAppointment, completeAppointment, cancelAppointment } from '@/api/appointment'
import { post } from '@/utils/request'
import { getStoreId } from '@/utils/auth'

const router = useRouter()
const loading = ref(false)
const appointments = ref<any[]>([])
const statusFilter = ref('')
const showCreateDialog = ref(false)
const createLoading = ref(false)
const createFormRef = ref<FormInstance>()

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

const createForm = reactive({
  customerName: '',
  customerPhone: '',
  serviceId: '',
  technicianId: '',
  appointmentTime: '',
  remark: ''
})

const createRules = {
  customerName: [{ required: true, message: '请输入客户姓名', trigger: 'blur' }],
  customerPhone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  appointmentTime: [{ required: true, message: '请选择预约时间', trigger: 'change' }]
}

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
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    if (statusFilter.value) {
      params.status = statusFilter.value
    }
    const res: any = await getAppointmentList(params)
    appointments.value = res.data?.list || res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

async function handleCheckIn(row: any) {
  try {
    await ElMessageBox.confirm(`确认为客户 ${row.customerName} 签到？`, '签到确认')
    await checkInAppointment(row.id)
    ElMessage.success('签到成功')
    loadAppointments()
  } catch {
    // cancelled or error
  }
}

async function handleComplete(row: any) {
  try {
    await ElMessageBox.confirm(`确认完成 ${row.customerName} 的服务？`, '完成确认')
    await completeAppointment(row.id)
    ElMessage.success('已完成')
    loadAppointments()
  } catch {
    // cancelled or error
  }
}

async function handleCancel(row: any) {
  try {
    await ElMessageBox.confirm(`确认取消 ${row.customerName} 的预约？`, '取消确认', { type: 'warning' })
    await cancelAppointment(row.id)
    ElMessage.success('已取消')
    loadAppointments()
  } catch {
    // cancelled or error
  }
}

function handleCreateOrder(row: any) {
  router.push({ path: '/cashier', query: { appointmentId: String(row.id) } })
}

async function handleCreateAppointment() {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return
  createLoading.value = true
  try {
    const storeId = getStoreId() || ''
    await post('/api/v1/trade/appointments', {
      ...createForm,
      storeId,
      type: 'walk_in'
    })
    ElMessage.success('预约创建成功')
    showCreateDialog.value = false
    createFormRef.value?.resetFields()
    loadAppointments()
  } catch {
    // handled by interceptor
  } finally {
    createLoading.value = false
  }
}

onMounted(() => {
  loadAppointments()
})
</script>

<style scoped>
.appointment-page {
  padding: 4px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.pagination-area {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
