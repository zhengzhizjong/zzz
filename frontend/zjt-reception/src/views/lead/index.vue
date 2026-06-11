<template>
  <div class="lead-page">
    <el-card shadow="never">
      <template #header>
        <div class="header-bar">
          <span class="title">线索管理</span>
          <div class="filters">
            <el-select v-model="filters.status" placeholder="状态" clearable style="width: 120px" @change="loadLeads">
              <el-option label="待跟进" :value="1" />
              <el-option label="跟进中" :value="2" />
              <el-option label="已转化" :value="3" />
              <el-option label="无效" :value="4" />
            </el-select>
            <el-select v-model="filters.source" placeholder="来源渠道" clearable style="width: 140px" @change="loadLeads">
              <el-option label="线上咨询" value="online" />
              <el-option label="电话" value="phone" />
              <el-option label="到店" value="walk_in" />
              <el-option label="转介绍" value="referral" />
              <el-option label="其他" value="other" />
            </el-select>
            <el-date-picker
              v-model="filters.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 260px"
              @change="loadLeads"
            />
          </div>
        </div>
      </template>

      <el-table :data="leads" size="small" stripe v-loading="loading">
        <el-table-column prop="customerName" label="客户姓名" width="100" />
        <el-table-column prop="customerPhone" label="手机号" width="130" />
        <el-table-column prop="source" label="来源渠道" width="100">
          <template #default="{ row }">{{ sourceLabel(row.source) }}</template>
        </el-table-column>
        <el-table-column prop="content" label="咨询内容" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assignedStaffName" label="分配员工" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" type="primary" link size="small" @click="handleFollow(row)">跟进</el-button>
            <el-button v-if="row.status === 1 || row.status === 2" type="success" link size="small" @click="handleConvert(row)">转化</el-button>
            <el-button v-if="row.status !== 4 && row.status !== 3" type="danger" link size="small" @click="handleInvalid(row)">标记无效</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-area" v-if="total > 0">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadLeads"
        />
      </div>
    </el-card>

    <!-- 跟进对话框 -->
    <el-dialog v-model="followDialogVisible" title="跟进线索" width="500px">
      <el-form :model="followForm" label-width="80px">
        <el-form-item label="跟进记录">
          <el-input v-model="followForm.followNote" type="textarea" :rows="4" placeholder="请输入跟进内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="followDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitFollow">确认跟进</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getLeadList, updateLead } from '@/api/lead'

const loading = ref(false)
const submitting = ref(false)
const leads = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)

const filters = reactive({
  status: undefined as number | undefined,
  source: undefined as string | undefined,
  dateRange: null as string[] | null
})

const followDialogVisible = ref(false)
const currentLead = ref<any>(null)
const followForm = reactive({
  followNote: ''
})

function statusTagType(status: number) {
  const map: Record<number, string> = { 1: 'warning', 2: '', 3: 'success', 4: 'info' }
  return map[status] || ''
}

function statusLabel(status: number) {
  const map: Record<number, string> = { 1: '待跟进', 2: '跟进中', 3: '已转化', 4: '无效' }
  return map[status] || '未知'
}

function sourceLabel(source: string) {
  const map: Record<string, string> = { online: '线上咨询', phone: '电话', walk_in: '到店', referral: '转介绍', other: '其他' }
  return map[source] || source
}

async function loadLeads() {
  loading.value = true
  try {
    const params: any = { page: page.value, pageSize: pageSize.value }
    if (filters.status !== undefined && filters.status !== null) params.status = filters.status
    if (filters.source) params.source = filters.source
    if (filters.dateRange && filters.dateRange.length === 2) {
      params.startDate = filters.dateRange[0]
      params.endDate = filters.dateRange[1]
    }
    const res: any = await getLeadList(params)
    leads.value = res.data?.list || res.data?.records || []
    total.value = res.data?.total || 0
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function handleFollow(row: any) {
  currentLead.value = row
  followForm.followNote = ''
  followDialogVisible.value = true
}

async function submitFollow() {
  if (!followForm.followNote) {
    ElMessage.warning('请输入跟进内容')
    return
  }
  submitting.value = true
  try {
    await updateLead(currentLead.value.id, { status: 2, followNote: followForm.followNote })
    ElMessage.success('跟进成功')
    followDialogVisible.value = false
    loadLeads()
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

async function handleConvert(row: any) {
  try {
    await ElMessageBox.confirm('确认将该线索转化为客户？', '转化确认', { type: 'success' })
    await updateLead(row.id, { status: 3 })
    ElMessage.success('转化成功')
    loadLeads()
  } catch {
    // cancelled
  }
}

async function handleInvalid(row: any) {
  try {
    await ElMessageBox.confirm('确认将该线索标记为无效？', '标记无效', { type: 'warning' })
    await updateLead(row.id, { status: 4 })
    ElMessage.success('已标记为无效')
    loadLeads()
  } catch {
    // cancelled
  }
}

onMounted(() => {
  loadLeads()
})
</script>

<style scoped>
.lead-page {
  padding: 4px;
}
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.title {
  font-size: 16px;
  font-weight: 600;
}
.filters {
  display: flex;
  gap: 8px;
  align-items: center;
}
.pagination-area {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
