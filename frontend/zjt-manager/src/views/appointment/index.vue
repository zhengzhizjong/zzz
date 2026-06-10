<template>
  <div class="appointment-page">
    <!-- 筛选区域 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable style="width: 140px;">
            <el-option label="待确认" value="pending" />
            <el-option label="已确认" value="confirmed" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
          <el-button type="success" @click="showCreateDialog">新建预约</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 预约列表 -->
    <el-card shadow="never" style="margin-top: 16px;">
      <el-table :data="appointmentList" stripe v-loading="loading">
        <el-table-column prop="id" label="预约编号" width="100" />
        <el-table-column prop="appointmentTime" label="预约时间" width="180" />
        <el-table-column prop="memberName" label="会员" width="120" />
        <el-table-column prop="memberPhone" label="联系电话" width="130" />
        <el-table-column prop="technicianName" label="技师" width="100" />
        <el-table-column prop="serviceName" label="服务项目" />
        <el-table-column prop="duration" label="时长(分钟)" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'pending'" type="primary" link size="small" @click="handleConfirm(row)">确认</el-button>
            <el-button v-if="row.status === 'pending' || row.status === 'confirmed'" type="warning" link size="small" @click="handleModify(row)">修改</el-button>
            <el-button v-if="row.status !== 'cancelled' && row.status !== 'completed'" type="danger" link size="small" @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="loadData"
        />
      </div>
    </el-card>

    <!-- 新建/修改预约对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '修改预约' : '新建预约'" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="会员" prop="memberId">
          <el-input v-model="form.memberId" placeholder="会员ID" />
        </el-form-item>
        <el-form-item label="技师" prop="technicianId">
          <el-input v-model="form.technicianId" placeholder="技师ID" />
        </el-form-item>
        <el-form-item label="服务项目" prop="serviceId">
          <el-input v-model="form.serviceId" placeholder="服务项目ID" />
        </el-form-item>
        <el-form-item label="预约时间" prop="appointmentTime">
          <el-date-picker v-model="form.appointmentTime" type="datetime" placeholder="选择时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getAppointmentList, modifyAppointment, cancelAppointment, createAppointment } from '@/api/appointment'

const loading = ref(false)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const editingId = ref('')
const formRef = ref<FormInstance>()

const filters = reactive({
  dateRange: null as string[] | null,
  status: ''
})

const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const appointmentList = ref<Array<Record<string, any>>>([])

const form = reactive({
  memberId: '',
  technicianId: '',
  serviceId: '',
  appointmentTime: '',
  remark: ''
})

const formRules: FormRules = {
  memberId: [{ required: true, message: '请输入会员ID', trigger: 'blur' }],
  technicianId: [{ required: true, message: '请输入技师ID', trigger: 'blur' }],
  serviceId: [{ required: true, message: '请输入服务项目ID', trigger: 'blur' }],
  appointmentTime: [{ required: true, message: '请选择预约时间', trigger: 'change' }]
}

function statusType(status: string) {
  const map: Record<string, string> = { pending: 'warning', confirmed: 'primary', completed: 'success', cancelled: 'info' }
  return map[status] || 'info'
}

function statusLabel(status: string) {
  const map: Record<string, string> = { pending: '待确认', confirmed: '已确认', completed: '已完成', cancelled: '已取消' }
  return map[status] || status
}

function resetFilters() {
  filters.dateRange = null
  filters.status = ''
  pagination.page = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: pagination.page, pageSize: pagination.pageSize }
    if (filters.status) params.status = filters.status
    if (filters.dateRange && filters.dateRange.length === 2) {
      params.startDate = filters.dateRange[0]
      params.endDate = filters.dateRange[1]
    }
    const res: any = await getAppointmentList(params)
    appointmentList.value = res.data?.list || res.data || []
    pagination.total = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function showCreateDialog() {
  isEdit.value = false
  editingId.value = ''
  Object.assign(form, { memberId: '', technicianId: '', serviceId: '', appointmentTime: '', remark: '' })
  dialogVisible.value = true
}

function handleModify(row: Record<string, any>) {
  isEdit.value = true
  editingId.value = row.id
  Object.assign(form, {
    memberId: row.memberId || '',
    technicianId: row.technicianId || '',
    serviceId: row.serviceId || '',
    appointmentTime: row.appointmentTime || '',
    remark: row.remark || ''
  })
  dialogVisible.value = true
}

async function handleConfirm(row: Record<string, any>) {
  await modifyAppointment(row.id, { status: 'confirmed' })
  ElMessage.success('已确认预约')
  loadData()
}

async function handleCancel(row: Record<string, any>) {
  await ElMessageBox.confirm('确定取消该预约吗？', '提示', { type: 'warning' })
  await cancelAppointment(row.id)
  ElMessage.success('已取消预约')
  loadData()
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await modifyAppointment(editingId.value, form)
      ElMessage.success('修改成功')
    } else {
      await createAppointment(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.appointment-page {
  padding: 0;
}
.filter-card :deep(.el-card__body) {
  padding-bottom: 2px;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
