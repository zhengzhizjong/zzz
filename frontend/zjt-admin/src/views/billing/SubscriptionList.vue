<template>
  <div class="subscription-list">
    <!-- 租户选择 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" class="search-form">
        <el-form-item label="选择租户">
          <el-select v-model="selectedTenantId" placeholder="请选择租户" filterable clearable @change="handleTenantChange">
            <el-option
              v-for="item in tenantOptions"
              :key="item.id"
              :label="item.tenantName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 订阅信息 -->
    <el-card v-if="subscription" shadow="never" class="info-card">
      <template #header>
        <span class="card-title">当前订阅信息</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="租户名称">{{ subscription.tenantName }}</el-descriptions-item>
        <el-descriptions-item label="套餐名称">
          <el-tag type="success">{{ subscription.planName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="订阅状态">
          <el-tag :type="subscription.status === 1 ? 'success' : subscription.status === 2 ? 'warning' : 'danger'">
            {{ subscription.status === 1 ? '生效中' : subscription.status === 2 ? '已过期' : '已取消' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ subscription.startDate }}</el-descriptions-item>
        <el-descriptions-item label="到期时间">{{ subscription.endDate }}</el-descriptions-item>
        <el-descriptions-item label="自动续费">
          <el-tag :type="subscription.autoRenew ? 'success' : 'info'" size="small">
            {{ subscription.autoRenew ? '已开启' : '未开启' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <div class="action-bar">
        <el-button type="primary" @click="handleRenew">续费</el-button>
        <el-button type="danger" @click="handleCancel">取消订阅</el-button>
        <el-button type="success" @click="subscribeVisible = true">变更套餐</el-button>
      </div>
    </el-card>

    <el-empty v-else-if="!loading" description="请选择租户查看订阅信息" />

    <!-- 变更套餐弹窗 -->
    <el-dialog v-model="subscribeVisible" title="变更套餐" width="500px" destroy-on-close>
      <el-form ref="subscribeFormRef" :model="subscribeForm" :rules="subscribeRules" label-width="100px">
        <el-form-item label="选择套餐" prop="planId">
          <el-select v-model="subscribeForm.planId" placeholder="请选择套餐">
            <el-option
              v-for="item in planOptions"
              :key="item.id"
              :label="`${item.planName} - ¥${item.price}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="subscribeVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubscribe">确认变更</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getTenantList, type TenantInfo } from '@/api/billing/tenant'
import { getSubscription, renew, cancel, subscribe } from '@/api/billing/subscription'
import { getPlanList, type PlanInfo } from '@/api/billing/plan'

const loading = ref(false)
const selectedTenantId = ref<number | undefined>(undefined)
const tenantOptions = ref<TenantInfo[]>([])
const planOptions = ref<PlanInfo[]>([])
const subscription = ref<any>(null)
const subscribeVisible = ref(false)
const subscribeFormRef = ref<FormInstance>()

const subscribeForm = reactive({
  planId: undefined as number | undefined,
})

const subscribeRules: FormRules = {
  planId: [{ required: true, message: '请选择套餐', trigger: 'change' }],
}

async function fetchTenantOptions() {
  try {
    const res: any = await getTenantList({ page: 1, pageSize: 999 })
    tenantOptions.value = res.data.list || []
  } catch {
    tenantOptions.value = []
  }
}

async function fetchPlanOptions() {
  try {
    const res: any = await getPlanList()
    planOptions.value = res.data || []
  } catch {
    planOptions.value = []
  }
}

async function handleTenantChange(tenantId: number) {
  if (!tenantId) {
    subscription.value = null
    return
  }
  loading.value = true
  try {
    const res: any = await getSubscription(tenantId)
    subscription.value = res.data || null
  } catch {
    subscription.value = null
  } finally {
    loading.value = false
  }
}

async function handleRenew() {
  if (!selectedTenantId.value) return
  try {
    await ElMessageBox.confirm('确认续费当前套餐？', '续费确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info',
    })
    await renew(selectedTenantId.value)
    ElMessage.success('续费成功')
    handleTenantChange(selectedTenantId.value)
  } catch {
    // 用户取消或请求失败
  }
}

async function handleCancel() {
  if (!selectedTenantId.value) return
  try {
    await ElMessageBox.confirm('确认取消当前订阅？取消后服务将在到期时停止。', '取消订阅', {
      confirmButtonText: '确认取消',
      cancelButtonText: '再想想',
      type: 'warning',
    })
    await cancel(selectedTenantId.value)
    ElMessage.success('订阅已取消')
    handleTenantChange(selectedTenantId.value)
  } catch {
    // 用户取消或请求失败
  }
}

async function handleSubscribe() {
  if (!subscribeFormRef.value) return
  await subscribeFormRef.value.validate()
  try {
    await subscribe({
      tenantId: selectedTenantId.value,
      planId: subscribeForm.planId,
    })
    ElMessage.success('套餐变更成功')
    subscribeVisible.value = false
    handleTenantChange(selectedTenantId.value!)
  } catch {
    // 错误已在拦截器中处理
  }
}

onMounted(() => {
  fetchTenantOptions()
  fetchPlanOptions()
})
</script>

<style scoped lang="scss">
.subscription-list {
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

.info-card {
  margin-bottom: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
}

.action-bar {
  margin-top: 20px;
  display: flex;
  gap: 12px;
}
</style>
