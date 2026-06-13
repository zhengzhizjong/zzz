<template>
  <div class="activity-list">
    <!-- 筛选栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="类型">
          <el-select v-model="searchForm.activityType" placeholder="全部类型" clearable>
            <el-option
              v-for="item in ACTIVITY_TYPE_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option
              v-for="item in ACTIVITY_STATUS_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="primary" @click="handleAdd">新增活动</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="activityName" label="活动名" min-width="150" show-overflow-tooltip />
        <el-table-column prop="activityType" label="类型" width="100" align="center">
          <template #default="{ row }">
            {{ getTypeLabel(row.activityType) }}
          </template>
        </el-table-column>
        <el-table-column prop="rulesJson" label="优惠规则" min-width="180" show-overflow-tooltip />
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column prop="endTime" label="结束时间" width="170" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              link
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="formVisible"
      :title="isEdit ? '编辑活动' : '新增活动'"
      width="560px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="活动名" prop="activityName">
          <el-input v-model="form.activityName" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="类型" prop="activityType">
          <el-select v-model="form.activityType" placeholder="请选择类型" style="width: 100%">
            <el-option
              v-for="item in ACTIVITY_TYPE_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="优惠规则" prop="rulesJson">
          <el-input v-model="form.rulesJson" placeholder="请输入优惠规则，如：满100减20" />
        </el-form-item>
        <el-form-item label="时间范围" prop="dateRange">
          <el-date-picker
            v-model="form.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
            <el-option
              v-for="item in ACTIVITY_STATUS_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getActivityList, createActivity, updateActivity, updateActivityStatus, type ActivityInfo } from '@/api/trade/activity'

const ACTIVITY_TYPE_OPTIONS = [
  { value: 1, label: '满减' },
  { value: 2, label: '折扣' },
  { value: 3, label: '赠品' },
]

const ACTIVITY_STATUS_OPTIONS = [
  { value: 0, label: '未开始', type: 'info' },
  { value: 1, label: '进行中', type: 'success' },
  { value: 2, label: '已结束', type: 'danger' },
]

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<ActivityInfo[]>([])
const formVisible = ref(false)
const isEdit = ref(false)
const currentId = ref<number | undefined>(undefined)
const formRef = ref<FormInstance>()

const searchForm = reactive({
  activityType: undefined as number | undefined,
  status: undefined as number | undefined,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const form = reactive({
  activityName: '',
  activityType: undefined as number | undefined,
  rulesJson: '',
  dateRange: null as [string, string] | null,
  status: undefined as number | undefined,
})

const formRules: FormRules = {
  activityName: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  activityType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  rulesJson: [{ required: true, message: '请输入优惠规则', trigger: 'blur' }],
  dateRange: [{ required: true, message: '请选择时间范围', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

function getTypeLabel(type: number): string {
  return ACTIVITY_TYPE_OPTIONS.find(o => o.value === type)?.label || '未知'
}

function getStatusLabel(status: number): string {
  return ACTIVITY_STATUS_OPTIONS.find(o => o.value === status)?.label || '未知'
}

function getStatusType(status: number): string {
  return ACTIVITY_STATUS_OPTIONS.find(o => o.value === status)?.type || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      activityType: searchForm.activityType,
      status: searchForm.status,
    }
    const res: any = await getActivityList(params)
    tableData.value = res.data.list || []
    pagination.total = res.data.pagination.total
  } catch {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.activityType = undefined
  searchForm.status = undefined
  pagination.page = 1
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  currentId.value = undefined
  Object.assign(form, {
    activityName: '',
    activityType: undefined,
    rulesJson: '',
    dateRange: null,
    status: undefined,
  })
  formVisible.value = true
}

function handleEdit(row: ActivityInfo) {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(form, {
    activityName: row.activityName,
    activityType: row.activityType,
    rulesJson: row.rulesJson,
    dateRange: [row.startTime, row.endTime] as [string, string],
    status: row.status,
  })
  formVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  if (!form.dateRange || form.dateRange.length !== 2) return

  submitLoading.value = true
  try {
    const data = {
      activityName: form.activityName,
      activityType: form.activityType,
      rulesJson: form.rulesJson,
      startTime: form.dateRange[0],
      endTime: form.dateRange[1],
      status: form.status,
    }
    if (isEdit.value && currentId.value) {
      await updateActivity(currentId.value, data)
      ElMessage.success('更新成功')
    } else {
      await createActivity(data)
      ElMessage.success('创建成功')
    }
    formVisible.value = false
    fetchData()
  } catch {
    // 错误已在拦截器中处理
  } finally {
    submitLoading.value = false
  }
}

async function handleToggleStatus(row: ActivityInfo) {
  const newStatus = row.status === 1 ? 2 : 1
  try {
    await updateActivityStatus(row.id, newStatus)
    ElMessage.success('状态更新成功')
    fetchData()
  } catch {
    // 错误已在拦截器中处理
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.activity-list {
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

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
