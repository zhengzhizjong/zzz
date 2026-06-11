<template>
  <div class="config-list">
    <!-- 筛选栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="平台">
          <el-select v-model="searchForm.platform" placeholder="全部平台" clearable>
            <el-option
              v-for="item in PLATFORM_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="primary" @click="handleAdd">新增配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="platform" label="平台名称" width="130" align="center">
          <template #default="{ row }">
            {{ getPlatformLabel(row.platform) }}
          </template>
        </el-table-column>
        <el-table-column prop="appKey" label="AppKey" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastSyncTime" label="最后同步时间" width="170" />
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
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
      :title="isEdit ? '编辑配置' : '新增配置'"
      width="560px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="平台" prop="platform">
          <el-select v-model="form.platform" placeholder="请选择平台" style="width: 100%">
            <el-option
              v-for="item in PLATFORM_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="AppKey" prop="appKey">
          <el-input v-model="form.appKey" placeholder="请输入AppKey" />
        </el-form-item>
        <el-form-item label="AppSecret" prop="appSecret">
          <el-input v-model="form.appSecret" placeholder="请输入AppSecret" show-password />
        </el-form-item>
        <el-form-item label="回调URL" prop="callbackUrl">
          <el-input v-model="form.callbackUrl" placeholder="请输入回调URL" />
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
import { getConfigList, createConfig, updateConfig, type ConfigInfo } from '@/api/integration/config'

const PLATFORM_OPTIONS = [
  { value: 'wecom', label: '企微' },
  { value: 'douyin', label: '抖音' },
  { value: 'meituan', label: '美团' },
  { value: 'dianping', label: '大众点评' },
  { value: 'xiaohongshu', label: '小红书' },
]

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<ConfigInfo[]>([])
const formVisible = ref(false)
const isEdit = ref(false)
const currentId = ref<number | undefined>(undefined)
const formRef = ref<FormInstance>()

const searchForm = reactive({
  platform: undefined as string | undefined,
  status: undefined as number | undefined,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

const form = reactive({
  platform: '',
  appKey: '',
  appSecret: '',
  callbackUrl: '',
})

const formRules: FormRules = {
  platform: [{ required: true, message: '请选择平台', trigger: 'change' }],
  appKey: [{ required: true, message: '请输入AppKey', trigger: 'blur' }],
  appSecret: [{ required: true, message: '请输入AppSecret', trigger: 'blur' }],
  callbackUrl: [{ required: true, message: '请输入回调URL', trigger: 'blur' }],
}

function getPlatformLabel(platform: string): string {
  return PLATFORM_OPTIONS.find(o => o.value === platform)?.label || platform
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      platform: searchForm.platform || undefined,
      status: searchForm.status,
    }
    const res: any = await getConfigList(params)
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
  searchForm.platform = undefined
  searchForm.status = undefined
  pagination.page = 1
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  currentId.value = undefined
  Object.assign(form, {
    platform: '',
    appKey: '',
    appSecret: '',
    callbackUrl: '',
  })
  formVisible.value = true
}

function handleEdit(row: ConfigInfo) {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(form, {
    platform: row.platform,
    appKey: row.appKey,
    appSecret: row.appSecret,
    callbackUrl: row.callbackUrl,
  })
  formVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()

  submitLoading.value = true
  try {
    const data = {
      platform: form.platform,
      appKey: form.appKey,
      appSecret: form.appSecret,
      callbackUrl: form.callbackUrl,
    }
    if (isEdit.value && currentId.value) {
      await updateConfig(currentId.value, data)
      ElMessage.success('更新成功')
    } else {
      await createConfig(data)
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

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.config-list {
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
