<template>
  <div class="gateway-list">
    <!-- 模型列表 -->
    <el-card shadow="never" class="model-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">可用模型列表</span>
          <el-button type="primary" size="small" @click="fetchProviders">刷新</el-button>
        </div>
      </template>
      <el-table :data="providers" v-loading="providersLoading" stripe border>
        <el-table-column prop="providerName" label="供应商" width="130" />
        <el-table-column prop="modelName" label="模型名称" min-width="180" />
        <el-table-column prop="modelCode" label="模型编码" width="160" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '可用' : '不可用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="qpmLimit" label="QPM限制" width="100" align="center" />
        <el-table-column prop="dailyLimit" label="日调用上限" width="110" align="center" />
        <el-table-column label="今日用量" width="120" align="center">
          <template #default="{ row }">
            {{ row.todayUsage || 0 }} / {{ row.dailyLimit || '-' }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 调用统计 -->
    <el-card shadow="never" class="chart-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">调用统计</span>
          <el-date-picker
            v-model="usageDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="fetchUsageData"
          />
        </div>
      </template>
      <div class="usage-summary">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value">{{ usageStats.totalCalls }}</div>
              <div class="stat-label">总调用次数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value">{{ usageStats.successRate }}%</div>
              <div class="stat-label">成功率</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value">{{ usageStats.avgLatency }}ms</div>
              <div class="stat-label">平均延迟</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value">{{ usageStats.totalTokens }}</div>
              <div class="stat-label">总Token消耗</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <el-table :data="usageDetail" v-loading="usageLoading" stripe border style="margin-top: 20px">
        <el-table-column prop="date" label="日期" width="130" />
        <el-table-column prop="modelCode" label="模型" width="160" />
        <el-table-column prop="callCount" label="调用次数" width="110" align="center" />
        <el-table-column prop="successCount" label="成功次数" width="110" align="center" />
        <el-table-column prop="failCount" label="失败次数" width="110" align="center" />
        <el-table-column prop="avgLatency" label="平均延迟(ms)" width="120" align="center" />
        <el-table-column prop="totalTokens" label="Token消耗" width="120" align="center" />
      </el-table>
    </el-card>

    <!-- 限流配置 -->
    <el-card shadow="never" class="config-card">
      <template #header>
        <span class="card-title">限流配置</span>
      </template>
      <el-form ref="configFormRef" :model="rateLimitConfig" label-width="140px">
        <el-form-item label="全局QPM限制">
          <el-input-number v-model="rateLimitConfig.globalQpm" :min="1" :max="10000" />
        </el-form-item>
        <el-form-item label="全局日调用上限">
          <el-input-number v-model="rateLimitConfig.globalDailyLimit" :min="1" :max="1000000" />
        </el-form-item>
        <el-form-item label="单租户QPM限制">
          <el-input-number v-model="rateLimitConfig.tenantQpm" :min="1" :max="1000" />
        </el-form-item>
        <el-form-item label="单租户日调用上限">
          <el-input-number v-model="rateLimitConfig.tenantDailyLimit" :min="1" :max="100000" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSaveConfig">保存配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProviders, getUsage } from '@/api/ai/gateway'
import dayjs from 'dayjs'

const providersLoading = ref(false)
const usageLoading = ref(false)
const providers = ref<any[]>([])
const usageDetail = ref<any[]>([])

const usageDateRange = ref<[string, string]>([
  dayjs().subtract(7, 'day').format('YYYY-MM-DD'),
  dayjs().format('YYYY-MM-DD'),
])

const usageStats = reactive({
  totalCalls: 0,
  successRate: 0,
  avgLatency: 0,
  totalTokens: 0,
})

const rateLimitConfig = reactive({
  globalQpm: 100,
  globalDailyLimit: 50000,
  tenantQpm: 20,
  tenantDailyLimit: 5000,
})

async function fetchProviders() {
  providersLoading.value = true
  try {
    const res: any = await getProviders()
    providers.value = res.data || []
  } catch {
    providers.value = []
  } finally {
    providersLoading.value = false
  }
}

async function fetchUsageData() {
  if (!usageDateRange.value) return
  usageLoading.value = true
  try {
    const res: any = await getUsage({
      startDate: usageDateRange.value[0],
      endDate: usageDateRange.value[1],
    })
    usageDetail.value = res.data?.detail || []
    usageStats.totalCalls = res.data?.totalCalls || 0
    usageStats.successRate = res.data?.successRate || 0
    usageStats.avgLatency = res.data?.avgLatency || 0
    usageStats.totalTokens = res.data?.totalTokens || 0
  } catch {
    usageDetail.value = []
  } finally {
    usageLoading.value = false
  }
}

function handleSaveConfig() {
  ElMessage.success('限流配置保存成功（模拟）')
}

onMounted(() => {
  fetchProviders()
  fetchUsageData()
})
</script>

<style scoped lang="scss">
.gateway-list {
  padding: 20px;
}

.model-card,
.chart-card,
.config-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
}

.usage-summary {
  padding: 10px 0;
}

.stat-item {
  text-align: center;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #07C160;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}
</style>
