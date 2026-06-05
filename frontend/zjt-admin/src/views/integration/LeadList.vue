<template>
  <div class="lead-list">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="来源平台">
          <el-select v-model="searchForm.sourcePlatform" placeholder="全部" clearable>
            <el-option
              v-for="item in PLATFORM_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option
              v-for="item in LEAD_STATUS_OPTIONS"
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
          <el-button type="primary" @click="handleAdd">新增线索</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 线索列表 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="sourcePlatform" label="来源平台" width="110" />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column prop="intentionLevel" label="意向等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getIntentionType(row.intentionLevel)" size="small">
              {{ row.intentionLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="tags" label="标签" min-width="150" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getLeadStatusType(row.status)" size="small">
              {{ getLeadStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="260" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleFollowUp(row)">跟进</el-button>
            <el-button type="success" link size="small" @click="handleAssign(row)">分配</el-button>
            <el-button type="warning" link size="small" @click="handleConvert(row)">转化</el-button>
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

    <!-- 新增线索弹窗 -->
    <el-dialog
      v-model="addVisible"
      title="新增线索"
      width="500px"
      destroy-on-close
    >
      <el-form ref="addFormRef" :model="addForm" :rules="addRules" label-width="100px">
        <el-form-item label="来源平台" prop="sourcePlatform">
          <el-select v-model="addForm.sourcePlatform" placeholder="请选择来源平台">
            <el-option
              v-for="item in PLATFORM_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="addForm.contactName" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="addForm.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="意向等级" prop="intentionLevel">
          <el-select v-model="addForm.intentionLevel" placeholder="请选择意向等级">
            <el-option label="高" value="高" />
            <el-option label="中" value="中" />
            <el-option label="低" value="低" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="addForm.tags" placeholder="请输入标签，逗号分隔" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveAdd">确认</el-button>
      </template>
    </el-dialog>

    <!-- 分配弹窗 -->
    <el-dialog
      v-model="assignVisible"
      title="分配线索"
      width="400px"
      destroy-on-close
    >
      <el-form label-width="80px">
        <el-form-item label="分配给">
          <el-input-number v-model="assignForm.assignedTo" :min="1" placeholder="请输入员工ID" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveAssign">确认分配</el-button>
      </template>
    </el-dialog>

    <!-- 转化弹窗 -->
    <el-dialog
      v-model="convertVisible"
      title="转化线索"
      width="400px"
      destroy-on-close
    >
      <el-form label-width="100px">
        <el-form-item label="关联会员ID">
          <el-input-number v-model="convertForm.memberId" :min="1" placeholder="请输入会员ID" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="convertVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveConvert">确认转化</el-button>
      </template>
    </el-dialog>

    <!-- 跟进记录弹窗 -->
    <el-dialog
      v-model="followUpVisible"
      title="跟进记录"
      width="500px"
      destroy-on-close
    >
      <el-form label-width="80px">
        <el-form-item label="跟进备注">
          <el-input v-model="followUpNotes" type="textarea" :rows="4" placeholder="请输入跟进记录" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="followUpVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveFollowUp">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getLeadList, createLead, updateLead, assignLead, convertLead, type LeadInfo } from '@/api/integration/lead'

const PLATFORM_OPTIONS = [
  { value: 'douyin', label: '抖音' },
  { value: 'meituan', label: '美团' },
  { value: 'dianping', label: '大众点评' },
  { value: 'wechat', label: '微信' },
  { value: 'offline', label: '线下' },
  { value: 'other', label: '其他' },
]

const LEAD_STATUS_OPTIONS = [
  { value: 0, label: '新线索', type: '' },
  { value: 1, label: '跟进中', type: 'warning' },
  { value: 2, label: '已转化', type: 'success' },
  { value: 3, label: '已流失', type: 'info' },
]

const loading = ref(false)
const tableData = ref<LeadInfo[]>([])
const addVisible = ref(false)
const assignVisible = ref(false)
const convertVisible = ref(false)
const followUpVisible = ref(false)
const currentLeadId = ref<number | undefined>(undefined)
const addFormRef = ref<FormInstance>()
const followUpNotes = ref('')

const searchForm = reactive({
  sourcePlatform: undefined as string | undefined,
  status: undefined as number | undefined,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const addForm = reactive({
  sourcePlatform: '',
  contactName: '',
  contactPhone: '',
  intentionLevel: '',
  tags: '',
})

const addRules: FormRules = {
  sourcePlatform: [{ required: true, message: '请选择来源平台', trigger: 'change' }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
}

const assignForm = reactive({
  assignedTo: undefined as number | undefined,
})

const convertForm = reactive({
  memberId: undefined as number | undefined,
})

function getLeadStatusLabel(status: number): string {
  return LEAD_STATUS_OPTIONS.find(o => o.value === status)?.label || '未知'
}

function getLeadStatusType(status: number): string {
  return LEAD_STATUS_OPTIONS.find(o => o.value === status)?.type || 'info'
}

function getIntentionType(level: string): string {
  if (level === '高') return 'danger'
  if (level === '中') return 'warning'
  return 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getLeadList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      sourcePlatform: searchForm.sourcePlatform || undefined,
      status: searchForm.status,
    })
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
  searchForm.sourcePlatform = undefined
  searchForm.status = undefined
  pagination.page = 1
  fetchData()
}

function handleAdd() {
  addForm.sourcePlatform = ''
  addForm.contactName = ''
  addForm.contactPhone = ''
  addForm.intentionLevel = ''
  addForm.tags = ''
  addVisible.value = true
}

async function handleSaveAdd() {
  if (!addFormRef.value) return
  await addFormRef.value.validate()
  try {
    await createLead(addForm)
    ElMessage.success('新增线索成功')
    addVisible.value = false
    fetchData()
  } catch {
    // 错误已在拦截器中处理
  }
}

function handleAssign(row: LeadInfo) {
  currentLeadId.value = row.id
  assignForm.assignedTo = undefined
  assignVisible.value = true
}

async function handleSaveAssign() {
  if (!assignForm.assignedTo) {
    ElMessage.warning('请输入员工ID')
    return
  }
  try {
    await assignLead(currentLeadId.value!, assignForm.assignedTo)
    ElMessage.success('分配成功')
    assignVisible.value = false
    fetchData()
  } catch {
    // 错误已在拦截器中处理
  }
}

function handleConvert(row: LeadInfo) {
  currentLeadId.value = row.id
  convertForm.memberId = undefined
  convertVisible.value = true
}

async function handleSaveConvert() {
  if (!convertForm.memberId) {
    ElMessage.warning('请输入会员ID')
    return
  }
  try {
    await convertLead(currentLeadId.value!, convertForm.memberId)
    ElMessage.success('转化成功')
    convertVisible.value = false
    fetchData()
  } catch {
    // 错误已在拦截器中处理
  }
}

function handleFollowUp(row: LeadInfo) {
  currentLeadId.value = row.id
  followUpNotes.value = row.followUpNotes || ''
  followUpVisible.value = true
}

async function handleSaveFollowUp() {
  if (!followUpNotes.value.trim()) {
    ElMessage.warning('请输入跟进记录')
    return
  }
  try {
    await updateLead(currentLeadId.value!, {
      followUpNotes: followUpNotes.value,
      status: 1,
    })
    ElMessage.success('跟进记录保存成功')
    followUpVisible.value = false
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
.lead-list {
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
