<template>
  <div class="technician-page">
    <!-- 筛选 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="在岗状态">
          <el-select v-model="filters.onDuty" placeholder="全部" clearable style="width: 140px;">
            <el-option label="在岗" value="true" />
            <el-option label="离岗" value="false" />
          </el-select>
        </el-form-item>
        <el-form-item label="技能等级">
          <el-select v-model="filters.skillLevel" placeholder="全部" clearable style="width: 140px;">
            <el-option label="初级" :value="1" />
            <el-option label="中级" :value="2" />
            <el-option label="高级" :value="3" />
            <el-option label="资深" :value="4" />
            <el-option label="首席" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 技师列表 -->
    <el-card shadow="never" style="margin-top: 16px;">
      <el-table :data="technicianList" stripe v-loading="loading">
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="skillLevel" label="技能等级" width="100">
          <template #default="{ row }">
            <el-tag :type="skillLevelType(row.skillLevel)" size="small">{{ skillLevelLabel(row.skillLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="specialties" label="专长" />
        <el-table-column prop="onDuty" label="在岗状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.onDuty ? 'success' : 'info'" size="small">{{ row.onDuty ? '在岗' : '离岗' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="todayAppointments" label="今日预约" width="100" />
        <el-table-column prop="rating" label="评分" width="80" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">详情</el-button>
            <el-button v-if="!row.onDuty" type="success" link size="small" @click="handleCheckIn(row)">签到</el-button>
            <el-button v-if="row.onDuty" type="warning" link size="small" @click="handleCheckOut(row)">签退</el-button>
            <el-button type="info" link size="small" @click="handleViewSchedule(row)">排班</el-button>
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

    <!-- 技师详情对话框 -->
    <el-dialog v-model="detailVisible" title="技师详情" width="560px">
      <el-descriptions :column="2" border v-if="currentTechnician">
        <el-descriptions-item label="姓名">{{ currentTechnician.name }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentTechnician.phone }}</el-descriptions-item>
        <el-descriptions-item label="技能等级">{{ skillLevelLabel(currentTechnician.skillLevel) }}</el-descriptions-item>
        <el-descriptions-item label="在岗状态">{{ currentTechnician.onDuty ? '在岗' : '离岗' }}</el-descriptions-item>
        <el-descriptions-item label="专长" :span="2">{{ currentTechnician.specialties }}</el-descriptions-item>
        <el-descriptions-item label="评分">{{ currentTechnician.rating }}</el-descriptions-item>
        <el-descriptions-item label="今日预约">{{ currentTechnician.todayAppointments }}</el-descriptions-item>
        <el-descriptions-item label="入职日期" :span="2">{{ currentTechnician.joinDate }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 排班对话框 -->
    <el-dialog v-model="scheduleVisible" title="排班信息" width="560px">
      <el-table :data="scheduleList" stripe>
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="startTime" label="上班时间" width="100" />
        <el-table-column prop="endTime" label="下班时间" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'scheduled' ? 'primary' : 'success'" size="small">
              {{ row.status === 'scheduled' ? '已排班' : '已完成' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTechnicianList, getTechnicianDetail, checkIn, checkOut } from '@/api/technician'
import { getScheduleByTechnician } from '@/api/schedule'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)
const detailVisible = ref(false)
const scheduleVisible = ref(false)
const currentTechnician = ref<Record<string, any> | null>(null)
const scheduleList = ref<Array<Record<string, any>>>([])

const filters = reactive({ onDuty: '', skillLevel: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const technicianList = ref<Array<Record<string, any>>>([])

function skillLevelType(level: number) {
  const map: Record<number, string> = { 1: 'info', 2: '', 3: 'warning', 4: 'danger', 5: 'success' }
  return map[level] || ''
}

function skillLevelLabel(level: number) {
  const map: Record<number, string> = { 1: '初级', 2: '中级', 3: '高级', 4: '资深', 5: '首席' }
  return map[level] || '未知'
}

function resetFilters() {
  filters.onDuty = ''
  filters.skillLevel = ''
  pagination.page = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: pagination.page, pageSize: pagination.pageSize, storeId: userStore.storeId }
    if (filters.onDuty !== '') params.onDuty = filters.onDuty
    if (filters.skillLevel) params.skillLevel = filters.skillLevel
    const res: any = await getTechnicianList(params)
    technicianList.value = res.data?.list || res.data?.records || res.data || []
    pagination.total = res.data?.pagination?.total || res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function handleViewDetail(row: Record<string, any>) {
  try {
    const res: any = await getTechnicianDetail(row.id)
    currentTechnician.value = res.data || row
    detailVisible.value = true
  } catch {
    currentTechnician.value = row
    detailVisible.value = true
  }
}

async function handleCheckIn(row: Record<string, any>) {
  await ElMessageBox.confirm(`确定对 ${row.name} 进行签到吗？`, '签到确认')
  await checkIn(row.id)
  ElMessage.success('签到成功')
  loadData()
}

async function handleCheckOut(row: Record<string, any>) {
  await ElMessageBox.confirm(`确定对 ${row.name} 进行签退吗？`, '签退确认')
  await checkOut(row.id)
  ElMessage.success('签退成功')
  loadData()
}

async function handleViewSchedule(row: Record<string, any>) {
  try {
    const month = new Date().toISOString().slice(0, 7)
    const res: any = await getScheduleByTechnician(row.id, month)
    const list = res.data || []
    scheduleList.value = list.map((item: any) => ({
      date: item.scheduleDate,
      startTime: item.startTime,
      endTime: item.endTime,
      status: item.scheduleType === 1 ? 'scheduled' : 'rest',
      shiftType: item.shiftType || '',
      scheduleType: item.scheduleType,
    }))
  } catch {
    scheduleList.value = []
  }
  scheduleVisible.value = true
}

onMounted(() => loadData())
</script>

<style scoped>
.technician-page {
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
