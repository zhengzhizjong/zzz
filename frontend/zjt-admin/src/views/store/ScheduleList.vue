<template>
  <div class="schedule-list">
    <!-- 筛选栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="门店">
          <el-select v-model="searchForm.storeId" placeholder="请选择门店" @change="handleStoreChange">
            <el-option
              v-for="store in storeOptions"
              :key="store.id"
              :label="store.storeName"
              :value="store.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="searchForm.date"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            @change="fetchData"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleBatchCreate">批量排班</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日历视图 -->
    <el-card shadow="never" class="table-card">
      <div class="calendar-header">
        <el-button :icon="ArrowLeft" circle @click="handlePrevMonth" />
        <span class="calendar-title">{{ currentMonthLabel }}</span>
        <el-button :icon="ArrowRight" circle @click="handleNextMonth" />
      </div>

      <div class="calendar-grid">
        <div class="calendar-weekdays">
          <div v-for="day in weekdays" :key="day" class="weekday-cell">{{ day }}</div>
        </div>
        <div class="calendar-body">
          <div
            v-for="(cell, idx) in calendarCells"
            :key="idx"
            class="calendar-cell"
            :class="{
              'other-month': !cell.currentMonth,
              'today': cell.isToday,
            }"
          >
            <div class="cell-date">{{ cell.day }}</div>
            <div v-if="cell.schedules.length > 0" class="cell-schedules">
              <div
                v-for="s in cell.schedules"
                :key="s.id"
                class="schedule-tag"
                :class="getScheduleClass(s.status)"
                @click="handleEditSchedule(s)"
              >
                {{ s.technicianName }} {{ getScheduleLabel(s.status) }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 批量排班弹窗 -->
    <el-dialog
      v-model="batchDialogVisible"
      title="批量排班"
      width="560px"
      destroy-on-close
    >
      <el-form ref="batchFormRef" :model="batchForm" :rules="batchFormRules" label-width="100px">
        <el-form-item label="技师" prop="technicianIds">
          <el-select v-model="batchForm.technicianIds" multiple placeholder="请选择技师" style="width: 100%">
            <el-option
              v-for="t in technicianOptions"
              :key="t.id"
              :label="t.name"
              :value="t.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排班类型" prop="status">
          <el-select v-model="batchForm.status" placeholder="请选择" style="width: 100%">
            <el-option label="上班" :value="1" />
            <el-option label="休息" :value="2" />
            <el-option label="请假" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围" prop="dateRange">
          <el-date-picker
            v-model="batchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="batchLoading" @click="handleBatchSubmit">确认</el-button>
      </template>
    </el-dialog>

    <!-- 单条排班编辑弹窗 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑排班"
      width="440px"
      destroy-on-close
    >
      <el-form ref="editFormRef" :model="editForm" :rules="editFormRules" label-width="80px">
        <el-form-item label="技师">
          <el-input :model-value="editForm.technicianName" disabled />
        </el-form-item>
        <el-form-item label="日期">
          <el-input :model-value="editForm.date" disabled />
        </el-form-item>
        <el-form-item label="排班类型" prop="status">
          <el-select v-model="editForm.status" style="width: 100%">
            <el-option label="上班" :value="1" />
            <el-option label="休息" :value="2" />
            <el-option label="请假" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleEditSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getScheduleByTechnician, batchCreateSchedule, updateSchedule } from '@/api/store/schedule'
import { getStoreList } from '@/api/store/info'
import { getTechnicianList } from '@/api/store/technician'
import type { StoreInfo } from '@/types/store'

interface ScheduleItem {
  id: number
  technicianId: number
  technicianName: string
  date: string
  status: number
}

interface CalendarCell {
  day: number
  dateStr: string
  currentMonth: boolean
  isToday: boolean
  schedules: ScheduleItem[]
}

const weekdays = ['日', '一', '二', '三', '四', '五', '六']

const storeOptions = ref<StoreInfo[]>([])
const technicianOptions = ref<{ id: number; name: string }[]>([])
const scheduleMap = ref<Record<string, ScheduleItem[]>>({})

const batchDialogVisible = ref(false)
const batchLoading = ref(false)
const batchFormRef = ref<FormInstance>()

const editDialogVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref<FormInstance>()

const searchForm = reactive({
  storeId: undefined as number | undefined,
  date: '' as string,
})

const currentYear = ref(new Date().getFullYear())
const currentMonth = ref(new Date().getMonth())

const batchForm = reactive({
  technicianIds: [] as number[],
  status: 1,
  dateRange: null as [string, string] | null,
})

const batchFormRules: FormRules = {
  technicianIds: [{ required: true, message: '请选择技师', trigger: 'change' }],
  status: [{ required: true, message: '请选择排班类型', trigger: 'change' }],
  dateRange: [{ required: true, message: '请选择日期范围', trigger: 'change' }],
}

const editForm = reactive({
  id: undefined as number | undefined,
  technicianName: '',
  date: '',
  status: 1,
})

const editFormRules: FormRules = {
  status: [{ required: true, message: '请选择排班类型', trigger: 'change' }],
}

const currentMonthLabel = computed(() => {
  return `${currentYear.value}年${currentMonth.value + 1}月`
})

const calendarCells = computed<CalendarCell[]>(() => {
  const year = currentYear.value
  const month = currentMonth.value
  const firstDay = new Date(year, month, 1).getDay()
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  const daysInPrevMonth = new Date(year, month, 0).getDate()
  const today = new Date()
  const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`

  const cells: CalendarCell[] = []

  // 上月填充
  for (let i = firstDay - 1; i >= 0; i--) {
    const day = daysInPrevMonth - i
    const prevMonth = month === 0 ? 11 : month - 1
    const prevYear = month === 0 ? year - 1 : year
    const dateStr = `${prevYear}-${String(prevMonth + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`
    cells.push({
      day,
      dateStr,
      currentMonth: false,
      isToday: dateStr === todayStr,
      schedules: scheduleMap.value[dateStr] || [],
    })
  }

  // 本月
  for (let day = 1; day <= daysInMonth; day++) {
    const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`
    cells.push({
      day,
      dateStr,
      currentMonth: true,
      isToday: dateStr === todayStr,
      schedules: scheduleMap.value[dateStr] || [],
    })
  }

  // 下月填充
  const remaining = 42 - cells.length
  for (let day = 1; day <= remaining; day++) {
    const nextMonth = month === 11 ? 0 : month + 1
    const nextYear = month === 11 ? year + 1 : year
    const dateStr = `${nextYear}-${String(nextMonth + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`
    cells.push({
      day,
      dateStr,
      currentMonth: false,
      isToday: dateStr === todayStr,
      schedules: scheduleMap.value[dateStr] || [],
    })
  }

  return cells
})

function getScheduleLabel(status: number): string {
  if (status === 1) return '上班'
  if (status === 2) return '休息'
  if (status === 3) return '请假'
  return '未知'
}

function getScheduleClass(status: number): string {
  if (status === 1) return 'schedule-work'
  if (status === 2) return 'schedule-rest'
  if (status === 3) return 'schedule-leave'
  return ''
}

async function fetchStoreOptions() {
  try {
    const res: any = await getStoreList({ page: 1, pageSize: 999 })
    storeOptions.value = res.data.list || []
    if (storeOptions.value.length > 0 && !searchForm.storeId) {
      searchForm.storeId = storeOptions.value[0].id
    }
  } catch {
    storeOptions.value = []
  }
}

async function fetchTechnicianOptions() {
  if (!searchForm.storeId) return
  try {
    const res: any = await getTechnicianList({ page: 1, pageSize: 999, storeId: searchForm.storeId })
    technicianOptions.value = (res.data.list || []).map((t: any) => ({ id: t.id, name: t.name }))
  } catch {
    technicianOptions.value = []
  }
}

async function fetchData() {
  if (!searchForm.storeId) return
  const month = `${currentYear.value}-${String(currentMonth.value + 1).padStart(2, '0')}`
  try {
    // 获取门店下所有技师
    const techRes: any = await getTechnicianList({ page: 1, pageSize: 999, storeId: searchForm.storeId })
    const technicians = techRes.data.list || []

    // 对每个技师查询月度排班，汇总到 scheduleMap
    const map: Record<string, ScheduleItem[]> = {}
    for (const tech of technicians) {
      try {
        const res: any = await getScheduleByTechnician(tech.id, month)
        const list: any[] = res.data || []
        list.forEach((item) => {
          const dateStr = item.scheduleDate || item.date
          if (!dateStr) return
          if (!map[dateStr]) map[dateStr] = []
          map[dateStr].push({
            id: item.id,
            technicianId: tech.id,
            technicianName: tech.name || tech.technicianName,
            date: dateStr,
            status: item.status,
          })
        })
      } catch {
        // 单个技师查询失败不影响其他技师
      }
    }
    scheduleMap.value = map
  } catch {
    scheduleMap.value = {}
  }
}

function handleStoreChange() {
  fetchTechnicianOptions()
  fetchData()
}

function handlePrevMonth() {
  if (currentMonth.value === 0) {
    currentMonth.value = 11
    currentYear.value--
  } else {
    currentMonth.value--
  }
  fetchData()
}

function handleNextMonth() {
  if (currentMonth.value === 11) {
    currentMonth.value = 0
    currentYear.value++
  } else {
    currentMonth.value++
  }
  fetchData()
}

function handleBatchCreate() {
  if (!searchForm.storeId) {
    ElMessage.warning('请先选择门店')
    return
  }
  batchForm.technicianIds = []
  batchForm.status = 1
  batchForm.dateRange = null
  batchDialogVisible.value = true
}

async function handleBatchSubmit() {
  if (!batchFormRef.value) return
  await batchFormRef.value.validate()
  if (!batchForm.dateRange || batchForm.dateRange.length !== 2) return

  batchLoading.value = true
  try {
    await batchCreateSchedule({
      storeId: searchForm.storeId,
      technicianIds: batchForm.technicianIds,
      status: batchForm.status,
      startDate: batchForm.dateRange[0],
      endDate: batchForm.dateRange[1],
    })
    ElMessage.success('批量排班成功')
    batchDialogVisible.value = false
    fetchData()
  } catch {
    // 错误已在拦截器中处理
  } finally {
    batchLoading.value = false
  }
}

function handleEditSchedule(schedule: ScheduleItem) {
  editForm.id = schedule.id
  editForm.technicianName = schedule.technicianName
  editForm.date = schedule.date
  editForm.status = schedule.status
  editDialogVisible.value = true
}

async function handleEditSubmit() {
  if (!editFormRef.value) return
  await editFormRef.value.validate()
  if (!editForm.id) return

  editLoading.value = true
  try {
    await updateSchedule(editForm.id, { status: editForm.status })
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    fetchData()
  } catch {
    // 错误已在拦截器中处理
  } finally {
    editLoading.value = false
  }
}

onMounted(async () => {
  await fetchStoreOptions()
  if (searchForm.storeId) {
    await fetchTechnicianOptions()
    fetchData()
  }
})
</script>

<style scoped lang="scss">
.schedule-list {
  padding: 20px;
}

.search-card {
  margin-bottom: 16px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.calendar-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 16px;
}

.calendar-title {
  font-size: 18px;
  font-weight: 600;
  min-width: 120px;
  text-align: center;
}

.calendar-grid {
  border: 1px solid #ebeef5;
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
}

.weekday-cell {
  padding: 10px;
  text-align: center;
  font-weight: 600;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;

  &:not(:last-child) {
    border-right: 1px solid #ebeef5;
  }
}

.calendar-body {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
}

.calendar-cell {
  min-height: 100px;
  padding: 6px;
  border-bottom: 1px solid #ebeef5;
  border-right: 1px solid #ebeef5;

  &:nth-child(7n) {
    border-right: none;
  }

  &.other-month {
    background: #fafafa;

    .cell-date {
      color: #c0c4cc;
    }
  }

  &.today {
    background: #f0f9eb;

    .cell-date {
      color: #07C160;
      font-weight: 700;
    }
  }
}

.cell-date {
  font-size: 14px;
  margin-bottom: 4px;
  color: #303133;
}

.cell-schedules {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.schedule-tag {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 3px;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  &.schedule-work {
    background: #e1f3d8;
    color: #67c23a;
  }

  &.schedule-rest {
    background: #f4f4f5;
    color: #909399;
  }

  &.schedule-leave {
    background: #faecd8;
    color: #e6a23c;
  }
}
</style>
