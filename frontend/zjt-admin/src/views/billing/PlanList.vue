<template>
  <div class="plan-list">
    <!-- 操作栏 -->
    <el-card class="search-card" shadow="never">
      <el-button type="primary" @click="handleAdd">新增套餐</el-button>
    </el-card>

    <!-- 套餐列表 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="planList" v-loading="loading" stripe border>
        <el-table-column prop="planName" label="套餐名称" min-width="150" />
        <el-table-column prop="planCode" label="套餐编码" width="130" />
        <el-table-column label="价格" width="120" align="center">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="durationDays" label="时长(天)" width="100" align="center" />
        <el-table-column prop="maxStores" label="最大门店数" width="110" align="center" />
        <el-table-column prop="maxTechnicians" label="最大技师数" width="110" align="center" />
        <el-table-column prop="aiQuota" label="AI配额" width="100" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="formVisible"
      :title="currentPlanId ? '编辑套餐' : '新增套餐'"
      width="650px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="editForm" :rules="formRules" label-width="110px">
        <el-form-item label="套餐名称" prop="planName">
          <el-input v-model="editForm.planName" placeholder="请输入套餐名称" />
        </el-form-item>
        <el-form-item label="套餐编码" prop="planCode">
          <el-input v-model="editForm.planCode" placeholder="请输入套餐编码" />
        </el-form-item>
        <el-form-item label="价格(元)" prop="price">
          <el-input-number v-model="editForm.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="时长(天)" prop="durationDays">
          <el-input-number v-model="editForm.durationDays" :min="1" />
        </el-form-item>
        <el-form-item label="最大门店数" prop="maxStores">
          <el-input-number v-model="editForm.maxStores" :min="1" />
        </el-form-item>
        <el-form-item label="最大技师数" prop="maxTechnicians">
          <el-input-number v-model="editForm.maxTechnicians" :min="1" />
        </el-form-item>
        <el-form-item label="AI配额" prop="aiQuota">
          <el-input-number v-model="editForm.aiQuota" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="editForm.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
        </el-form-item>
        <el-form-item label="功能特性">
          <el-input
            v-model="editForm.featuresJson"
            type="textarea"
            :rows="6"
            placeholder='请输入JSON格式功能特性，如：{"features":["预约管理","会员管理"]}'
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getPlanList, createPlan, updatePlan, type PlanInfo } from '@/api/billing/plan'

const loading = ref(false)
const planList = ref<PlanInfo[]>([])
const formVisible = ref(false)
const currentPlanId = ref<number | undefined>(undefined)
const formRef = ref<FormInstance>()

const editForm = reactive({
  planName: '',
  planCode: '',
  price: 0,
  durationDays: 30,
  maxStores: 5,
  maxTechnicians: 20,
  aiQuota: 100,
  status: 1,
  featuresJson: '',
})

const formRules: FormRules = {
  planName: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  planCode: [{ required: true, message: '请输入套餐编码', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  durationDays: [{ required: true, message: '请输入时长', trigger: 'blur' }],
}

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getPlanList()
    planList.value = res.data || []
  } catch {
    planList.value = []
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  currentPlanId.value = undefined
  editForm.planName = ''
  editForm.planCode = ''
  editForm.price = 0
  editForm.durationDays = 30
  editForm.maxStores = 5
  editForm.maxTechnicians = 20
  editForm.aiQuota = 100
  editForm.status = 1
  editForm.featuresJson = ''
  formVisible.value = true
}

function handleEdit(row: PlanInfo) {
  currentPlanId.value = row.id
  editForm.planName = row.planName
  editForm.planCode = row.planCode
  editForm.price = row.price
  editForm.durationDays = row.durationDays
  editForm.maxStores = row.maxStores
  editForm.maxTechnicians = row.maxTechnicians
  editForm.aiQuota = row.aiQuota
  editForm.status = row.status
  editForm.featuresJson = row.featuresJson || ''
  formVisible.value = true
}

async function handleSave() {
  if (!formRef.value) return
  await formRef.value.validate()
  // 校验featuresJson格式
  if (editForm.featuresJson) {
    try {
      JSON.parse(editForm.featuresJson)
    } catch {
      ElMessage.error('功能特性JSON格式不正确')
      return
    }
  }
  try {
    if (currentPlanId.value) {
      await updatePlan(currentPlanId.value, editForm)
    } else {
      await createPlan(editForm)
    }
    ElMessage.success(currentPlanId.value ? '编辑成功' : '新增成功')
    formVisible.value = false
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
.plan-list {
  padding: 20px;
}

.search-card {
  margin-bottom: 16px;
}
</style>
