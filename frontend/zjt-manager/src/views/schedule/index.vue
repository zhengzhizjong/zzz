<template>
  <div class="schedule-page">
    <!-- 顶部筛选 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="门店">
          <el-select v-model="filters.storeId" placeholder="选择门店" style="width: 180px;" disabled>
            <el-option :label="storeName" :value="filters.storeId" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择周">
          <el-date-picker
            v-model="filters.weekStart"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            :clearable="false"
            style="width: 160px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="goPrevWeek">上一周</el-button>
          <el-button @click="goNextWeek">下一周</el-button>
          <el-button type="success" @click="batchDialogVisible = true">批量排班</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 排班日历 -->
    <el-card shadow="never" style="margin-top: 16px;">
      <el-table :data="scheduleTableData" stripe v-loading="loading" border>
        <el-table-column prop="technicianName" label="技师" width="100" fixed />
        <el-table-column v-for="day in weekDays" :key="day.date" :label="day.label" min-width="110" align="center">
          <template #default="{ row }">
            <div
              class="schedule-cell"
              :class="getShiftClass(row.shifts[day.date])"
              @click="handleCellClick(row, day.date)"
            >
              {{ getShiftLabel(row.shifts[day.date]) }}
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 单元格编辑弹窗 -->
    <el-dialog v-model="cellDialogVisible" title="修改班次" width="400px">
      <el-form label-width="80px">
        <el-form-item label="技师">
          <span>{{ currentCell.technicianName }}</span>
        </el-form-item>
        <el-form-item label="日期">
          <span>{{ currentCell.date }}</span>
        </el-form-item>
        <el-form-item label="班次">
          <el-select v-model="currentCell.shift" placeholder="请选择班次" style="width: 100%;">
            <el-option label="早班" value="morning" />
            <el-option label="中班" value="afternoon" />
            <el-option label="晚班" value="evening" />
            <el-option label="休息" value="rest" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cellDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveCell">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量排班弹窗 -->
    <el-dialog v-model="batchDialogVisible" title="批量排班" width="500px">
      <el-form :model="batchForm" label-width="80px">
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="batchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="选择技师">
          <el-select v-model="batchForm.technicianIds" multiple placeholder="请选择技师" style="width: 100%;">
            <el-option
              v-for="tech in technicianOptions"
              :key="tech.id"
              :label="tech.name"
              :value="tech.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="班次">
          <el-select v-model="batchForm.shift" placeholder="请选择班次" style="width: 100%;">
            <el-option label="早班" value="morning" />
            <el-option label="中班" value="afternoon" />
            <el-option label="晚班" value="evening" />
            <el-option label="休息" value="rest" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchSave" :loading="batchLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getScheduleList, updateSchedule, batchCreateSchedule } from '@/api/schedule'
import { getTechnicianList } from '@/api/technician'

const userStore = useUserStore()
const loading = ref(false)
const batchLoading = ref(false)
const cellDialogVisible = ref(false)
const batchDialogVisible = ref(false)

const storeName = computed(() => userStore.userInfo?.storeName || '当前门店')

const filters = reactive({
  storeId: userStore.storeId || '',
  weekStart: getMonday(new Date())
})

const technicianOptions = ref<Array<Record<string, any>>>([])
const scheduleList = ref<Array<Record<string, any>>>([])

const currentCell = reactive({
  scheduleId: null as number | null,
  technicianId: null as number | null,
  technicianName: '',
  date: '',
  shift: ''
})

const batchForm = reactive({
  dateRange: null as string[] | null,
  technicianIds: [] as number[],
  shift: ''
})

const weekDays = computed(() => {
  const start = new Date(filters.weekStart)
  const dayNames = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  return Array.from({ length: 7 }, (_, i) => {
    const d = new Date(start)
    d.setDate(d.getDate() + i)
    const dateStr = d.toISOString().slice(0, 10)
    return { date: dateStr, label: `${dayNames[i]}\n${dateStr.slice(5)}` }
  })
})

const scheduleTableData = computed(() => {
  const map: Record<number, Record<string, any>> = {}
  for (const tech of technicianOptions.value) {
    map[tech.id] = { technicianId: tech.id, technicianName: tech.name, shifts: {} as Record<string, any> }
  }
  for (const item of scheduleList.value) {
    const tid = item.technicianId || item.employeeId
    const date = item.date
    if (map[tid]) {
      map[tid].shifts[date] = item
    }
  }
  return Object.values(map)
})

function getMonday(d: Date): string {
  const date = new Date(d)
  const day = date.getDay()
  const diff = date.getDate() - day + (day === 0 ? -6 : 1)
  date.setDate(diff)
  return date.toISOString().slice(0, 10)
}

function getShiftLabel(shift: Record<string, any> | undefined) {
  if (!shift) return '未排'
  const map: Record<string, string> = { morning: '早班', afternoon: '中班', evening: '晚班', rest: '休息' }
  return map[shift.shift || shift.type] || '未排'
}

function getShiftClass(shift: Record<string, any> | undefined) {
  if (!shift) return 'shift-none'
  const map: Record<string, string> = { morning: 'shift-morning', afternoon: 'shift-afternoon', evening: 'shift-evening', rest: 'shift-rest' }
  return map[shift.shift || shift.type] || 'shift-none'
}

function goPrevWeek() {
  const d = new Date(filters.weekStart)
  d.setDate(d.getDate() - 7)
  filters.weekStart = d.toISOString().slice(0, 10)
  loadData()
}

function goNextWeek() {
  const d = new Date(filters.weekStart)
  d.setDate(d.getDate() + 7)
  filters.weekStart = d.toISOString().slice(0, 10)
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const [techRes, scheduleRes]: any[] = await Promise.all([
      getTechnicianList({ storeId: filters.storeId, pageSize: 100 }),
      getScheduleList({ storeId: filters.storeId, weekStart: filters.weekStart })
    ])
    technicianOptions.value = techRes.data?.list || techRes.data || []
    scheduleList.value = scheduleRes.data?.list || scheduleRes.data || []
  } finally {
    loading.value = false
  }
}

function handleCellClick(row: Record<string, any>, date: string) {
  const shift = row.shifts[date]
  currentCell.scheduleId = shift?.id || null
  currentCell.technicianId = row.technicianId
  currentCell.technicianName = row.technicianName
  currentCell.date = date
  currentCell.shift = shift?.shift || shift?.type || ''
  cellDialogVisible.value = true
}

async function handleSaveCell() {
  try {
    const data = {
      storeId: filters.storeId,
      technicianId: currentCell.technicianId,
      date: currentCell.date,
      shift: currentCell.shift
    }
    if (currentCell.scheduleId) {
      await updateSchedule(currentCell.scheduleId, data)
    } else {
      await batchCreateSchedule({ schedules: [data] })
    }
    ElMessage.success('排班更新成功')
    cellDialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error('排班更新失败')
  }
}

async function handleBatchSave() {
  if (!batchForm.dateRange || batchForm.technicianIds.length === 0 || !batchForm.shift) {
    ElMessage.warning('请完整填写批量排班信息')
    return
  }
  batchLoading.value = true
  try {
    const [startDate, endDate] = batchForm.dateRange
    await batchCreateSchedule({
      storeId: filters.storeId,
      startDate,
      endDate,
      technicianIds: batchForm.technicianIds,
      shift: batchForm.shift
    })
    ElMessage.success('批量排班成功')
    batchDialogVisible.value = false
    batchForm.dateRange = null
    batchForm.technicianIds = []
    batchForm.shift = ''
    loadData()
  } catch {
    ElMessage.error('批量排班失败')
  } finally {
    batchLoading.value = false
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.schedule-page {
  padding: 0;
}
.filter-card :deep(.el-card__body) {
  padding-bottom: 2px;
}
.schedule-cell {
  padding: 6px 0;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  text-align: center;
  transition: opacity 0.2s;
}
.schedule-cell:hover {
  opacity: 0.8;
}
.shift-morning {
  background: #e1f3d8;
  color: #67c23a;
}
.shift-afternoon {
  background: #d9ecff;
  color: #409eff;
}
.shift-evening {
  background: #fde2e2;
  color: #f56c6c;
}
.shift-rest {
  background: #f4f4f5;
  color: #909399;
}
.shift-none {
  background: #fafafa;
  color: #c0c4cc;
}
</style>
