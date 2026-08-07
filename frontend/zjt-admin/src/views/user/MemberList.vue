<template>
  <div class="member-list">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="会员号/昵称/手机号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.memberType" placeholder="全部类型" clearable>
            <el-option
              v-for="item in MEMBER_TYPE_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option
              v-for="item in STATUS_OPTIONS"
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
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="memberNo" label="会员号" width="130" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="gender" label="性别" width="80" align="center">
          <template #default="{ row }">
            {{ getGenderLabel(row.gender) }}
          </template>
        </el-table-column>
        <el-table-column prop="memberType" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getMemberTypeTag(row.memberType)" size="small">
              {{ getMemberTypeLabel(row.memberType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalConsumption" label="消费总额" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.totalConsumption?.toFixed(2) ?? '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="visitCount" label="到店次数" width="100" align="center" />
        <el-table-column prop="balance" label="余额" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.balance?.toFixed(2) ?? '0.00' }}
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="90" align="center" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="170" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">详情</el-button>
            <el-button type="warning" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              :type="row.status === 1 ? 'danger' : 'success'"
              link
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="会员详情" width="640px" destroy-on-close>
      <el-descriptions :column="2" border v-if="currentDetail">
        <el-descriptions-item label="会员号">{{ currentDetail.memberNo }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentDetail.name }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ currentDetail.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentDetail.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ getGenderLabel(currentDetail.gender) }}</el-descriptions-item>
        <el-descriptions-item label="生日">{{ currentDetail.birthday || '-' }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="getMemberTypeTag(currentDetail.memberType)" size="small">
            {{ getMemberTypeLabel(currentDetail.memberType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentDetail.status === 1 ? 'success' : 'danger'" size="small">
            {{ currentDetail.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="消费总额">¥{{ currentDetail.totalConsumption?.toFixed(2) ?? '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="到店次数">{{ currentDetail.visitCount ?? 0 }}次</el-descriptions-item>
        <el-descriptions-item label="余额">¥{{ currentDetail.balance?.toFixed(2) ?? '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="积分">{{ currentDetail.points ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="来源渠道">{{ getSourceChannelLabel(currentDetail.sourceChannel) }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ currentDetail.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentDetail.updatedAt || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑会员" width="560px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="editFormRules" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="editForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="editForm.gender">
            <el-radio :label="0">未知</el-radio>
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="生日" prop="birthday">
          <el-date-picker v-model="editForm.birthday" type="date" placeholder="选择生日" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="类型" prop="memberType">
          <el-select v-model="editForm.memberType" placeholder="请选择类型" style="width: 100%">
            <el-option
              v-for="item in MEMBER_TYPE_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="editForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleEditSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getMemberList, getMemberDetail, updateMember, type MemberInfo, type MemberUpdateRequest } from '@/api/user/member'

const MEMBER_TYPE_OPTIONS = [
  { value: 1, label: '散客', tag: 'info' },
  { value: 2, label: '会员', tag: '' },
  { value: 3, label: 'VIP', tag: 'warning' },
]

const STATUS_OPTIONS = [
  { value: 1, label: '正常' },
  { value: 0, label: '禁用' },
]

const SOURCE_CHANNEL_MAP: Record<string, string> = {
  phone: '手机注册',
  wechat: '微信',
  miniapp: '小程序',
  store: '门店',
  referral: '推荐',
}

const loading = ref(false)
const editLoading = ref(false)
const tableData = ref<MemberInfo[]>([])
const detailVisible = ref(false)
const editVisible = ref(false)
const currentDetail = ref<MemberInfo | null>(null)
const currentEditId = ref<number | undefined>(undefined)
const editFormRef = ref<FormInstance>()

const searchForm = reactive({
  keyword: '',
  memberType: undefined as number | undefined,
  status: undefined as number | undefined,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const editForm = reactive({
  name: '',
  realName: '',
  phone: '',
  gender: 0 as number,
  birthday: '',
  memberType: undefined as number | undefined,
  status: 1 as number,
})

const editFormRules: FormRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
}

function getMemberTypeLabel(type: number): string {
  return MEMBER_TYPE_OPTIONS.find(o => o.value === type)?.label || '未知'
}

function getMemberTypeTag(type: number): string {
  return MEMBER_TYPE_OPTIONS.find(o => o.value === type)?.tag || 'info'
}

function getGenderLabel(gender: number): string {
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '未知'
}

function getSourceChannelLabel(channel: string): string {
  return SOURCE_CHANNEL_MAP[channel] || channel || '-'
}

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getMemberList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      memberType: searchForm.memberType,
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
  searchForm.keyword = ''
  searchForm.memberType = undefined
  searchForm.status = undefined
  pagination.page = 1
  fetchData()
}

async function handleViewDetail(row: MemberInfo) {
  try {
    const res: any = await getMemberDetail(row.id)
    currentDetail.value = res.data || null
  } catch {
    currentDetail.value = row
  }
  detailVisible.value = true
}

function handleEdit(row: MemberInfo) {
  currentEditId.value = row.id
  editForm.name = row.name || ''
  editForm.realName = row.realName || ''
  editForm.phone = row.phone || ''
  editForm.gender = row.gender ?? 0
  editForm.birthday = row.birthday || ''
  editForm.memberType = row.memberType
  editForm.status = row.status ?? 1
  editVisible.value = true
}

async function handleEditSubmit() {
  const valid = await editFormRef.value?.validate().catch(() => false)
  if (!valid) return

  editLoading.value = true
  try {
    const data: MemberUpdateRequest = {
      name: editForm.name,
      realName: editForm.realName,
      phone: editForm.phone,
      gender: editForm.gender,
      birthday: editForm.birthday || undefined,
      memberType: editForm.memberType,
      status: editForm.status,
    }
    await updateMember(currentEditId.value!, data)
    ElMessage.success('更新成功')
    editVisible.value = false
    fetchData()
  } catch {
    // 错误已在拦截器中处理
  } finally {
    editLoading.value = false
  }
}

async function handleToggleStatus(row: MemberInfo) {
  const newStatus = row.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确认${actionText}会员「${row.name}」？`, '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await updateMember(row.id, { status: newStatus })
    ElMessage.success(`${actionText}成功`)
    fetchData()
  } catch {
    // 用户取消或请求失败
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.member-list {
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
