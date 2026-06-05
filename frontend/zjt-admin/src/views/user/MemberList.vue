<template>
  <div class="member-list">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="会员号/昵称/手机号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="等级">
          <el-select v-model="searchForm.level" placeholder="全部等级" clearable>
            <el-option
              v-for="item in LEVEL_OPTIONS"
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
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="level" label="等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.level)" size="small">
              {{ getLevelLabel(row.level) }}
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
        <el-table-column prop="createdAt" label="注册时间" width="170" />
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">详情</el-button>
            <el-button type="warning" link size="small" @click="handleEdit(row)">编辑</el-button>
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
    <el-dialog v-model="detailVisible" title="会员详情" width="560px" destroy-on-close>
      <el-descriptions :column="2" border v-if="currentDetail">
        <el-descriptions-item label="会员号">{{ currentDetail.memberNo }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ currentDetail.nickname }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentDetail.phone }}</el-descriptions-item>
        <el-descriptions-item label="等级">
          <el-tag :type="getLevelType(currentDetail.level)" size="small">
            {{ getLevelLabel(currentDetail.level) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="消费总额">¥{{ currentDetail.totalConsumption?.toFixed(2) ?? '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="到店次数">{{ currentDetail.visitCount }}次</el-descriptions-item>
        <el-descriptions-item label="余额">¥{{ currentDetail.balance?.toFixed(2) ?? '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ currentDetail.createdAt }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑会员" width="500px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="editFormRules" label-width="80px">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="等级" prop="level">
          <el-select v-model="editForm.level" placeholder="请选择等级" style="width: 100%">
            <el-option
              v-for="item in LEVEL_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
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
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getMemberList, getMemberDetail, updateMember, type MemberInfo } from '@/api/user/member'

const LEVEL_OPTIONS = [
  { value: 1, label: '普通会员', type: 'info' },
  { value: 2, label: '银卡会员', type: '' },
  { value: 3, label: '金卡会员', type: 'warning' },
  { value: 4, label: '钻石会员', type: 'danger' },
]

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
  level: undefined as number | undefined,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const editForm = reactive({
  nickname: '',
  level: undefined as number | undefined,
})

const editFormRules: FormRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  level: [{ required: true, message: '请选择等级', trigger: 'change' }],
}

function getLevelLabel(level: number): string {
  return LEVEL_OPTIONS.find(o => o.value === level)?.label || '未知'
}

function getLevelType(level: number): string {
  return LEVEL_OPTIONS.find(o => o.value === level)?.type || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getMemberList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      level: searchForm.level,
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
  searchForm.level = undefined
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
  editForm.nickname = row.nickname
  editForm.level = row.level
  editVisible.value = true
}

async function handleEditSubmit() {
  const valid = await editFormRef.value?.validate().catch(() => false)
  if (!valid) return

  editLoading.value = true
  try {
    await updateMember(currentEditId.value!, {
      nickname: editForm.nickname,
      level: editForm.level,
    })
    ElMessage.success('更新成功')
    editVisible.value = false
    fetchData()
  } catch {
    // 错误已在拦截器中处理
  } finally {
    editLoading.value = false
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
