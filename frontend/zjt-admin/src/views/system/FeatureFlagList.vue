<template>
  <div class="feature-flag-list">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="开关键/名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="primary" @click="handleAdd">新增开关</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 开关列表 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="flagKey" label="开关键" width="200" />
        <el-table-column prop="flagName" label="开关名称" width="180" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.enabled"
              active-color="#07C160"
              @change="handleToggle(row, $event)"
            />
          </template>
        </el-table-column>
        <el-table-column label="灰度百分比" width="140" align="center">
          <template #default="{ row }">
            {{ row.grayPercent }}%
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="updatedAt" label="更新时间" width="170" />
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑功能开关' : '新增功能开关'"
      width="520px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="开关键" prop="flagKey">
          <el-input v-model="form.flagKey" :disabled="isEdit" placeholder="请输入开关键" />
        </el-form-item>
        <el-form-item label="开关名称" prop="flagName">
          <el-input v-model="form.flagName" placeholder="请输入开关名称" />
        </el-form-item>
        <el-form-item label="启用状态" prop="enabled">
          <el-switch v-model="form.enabled" active-color="#07C160" />
        </el-form-item>
        <el-form-item label="灰度百分比" prop="grayPercent">
          <el-slider v-model="form.grayPercent" :min="0" :max="100" :step="1" show-input />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getFeatureFlagList, createFeatureFlag, updateFeatureFlag,
  toggleFeatureFlag, deleteFeatureFlag,
  type FeatureFlagInfo,
} from '@/api/system/config'

const loading = ref(false)
const tableData = ref<FeatureFlagInfo[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentId = ref<number | undefined>(undefined)
const formRef = ref<FormInstance>()

const searchForm = reactive({ keyword: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const form = reactive({
  flagKey: '',
  flagName: '',
  enabled: true,
  grayPercent: 100,
  description: '',
})

const formRules: FormRules = {
  flagKey: [{ required: true, message: '请输入开关键', trigger: 'blur' }],
  flagName: [{ required: true, message: '请输入开关名称', trigger: 'blur' }],
}

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getFeatureFlagList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
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
  pagination.page = 1
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  currentId.value = undefined
  form.flagKey = ''
  form.flagName = ''
  form.enabled = true
  form.grayPercent = 100
  form.description = ''
  dialogVisible.value = true
}

function handleEdit(row: FeatureFlagInfo) {
  isEdit.value = true
  currentId.value = row.id
  form.flagKey = row.flagKey
  form.flagName = row.flagName
  form.enabled = row.enabled
  form.grayPercent = row.grayPercent
  form.description = row.description
  dialogVisible.value = true
}

async function handleToggle(row: FeatureFlagInfo, val: boolean) {
  try {
    await toggleFeatureFlag(row.id, val)
    ElMessage.success(val ? '已开启' : '已关闭')
    fetchData()
  } catch {
    // 错误已在拦截器中处理
  }
}

async function handleSave() {
  if (!formRef.value) return
  await formRef.value.validate()
  try {
    if (isEdit.value && currentId.value) {
      await updateFeatureFlag(currentId.value, {
        flagName: form.flagName,
        enabled: form.enabled,
        grayPercent: form.grayPercent,
        description: form.description,
      })
      ElMessage.success('编辑成功')
    } else {
      await createFeatureFlag({
        flagKey: form.flagKey,
        flagName: form.flagName,
        enabled: form.enabled,
        grayPercent: form.grayPercent,
        description: form.description,
      })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {
    // 错误已在拦截器中处理
  }
}

async function handleDelete(row: FeatureFlagInfo) {
  try {
    await ElMessageBox.confirm(`确认删除功能开关「${row.flagName}」？删除后不可恢复。`, '删除确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteFeatureFlag(row.id)
    ElMessage.success('删除成功')
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
.feature-flag-list {
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
