<template>
  <div class="technician-ranking">
    <!-- 筛选栏 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="时间范围">
          <el-radio-group v-model="filterForm.period" @change="handleSearch">
            <el-radio-button label="week">本周</el-radio-button>
            <el-radio-button label="month">本月</el-radio-button>
            <el-radio-button label="quarter">本季</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排行维度">
          <el-radio-group v-model="filterForm.dimension" @change="handleSearch">
            <el-radio-button label="revenue">业绩</el-radio-button>
            <el-radio-button label="service">服务数</el-radio-button>
            <el-radio-button label="rating">评分</el-radio-button>
            <el-radio-button label="promotion">推广</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 排行表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column label="排名" width="80" align="center">
          <template #default="{ $index }">
            <span :class="['rank-badge', { 'rank-top': $index < 3 }]">
              {{ $index + 1 }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="technicianNo" label="技师编号" width="110" />
        <el-table-column prop="technicianName" label="姓名" width="100" />
        <el-table-column prop="storeName" label="门店" min-width="140" show-overflow-tooltip />
        <el-table-column label="业绩" width="130" align="center" v-if="filterForm.dimension === 'revenue'">
          <template #default="{ row }">
            ¥{{ row.revenue?.toFixed(2) ?? '0.00' }}
          </template>
        </el-table-column>
        <el-table-column label="服务数" width="100" align="center" v-if="filterForm.dimension === 'service'">
          <template #default="{ row }">
            {{ row.serviceCount ?? 0 }}
          </template>
        </el-table-column>
        <el-table-column label="评分" width="100" align="center" v-if="filterForm.dimension === 'rating'">
          <template #default="{ row }">
            {{ row.rating?.toFixed(1) ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="推广数" width="100" align="center" v-if="filterForm.dimension === 'promotion'">
          <template #default="{ row }">
            {{ row.promotionCount ?? 0 }}
          </template>
        </el-table-column>
        <el-table-column label="环比变化" width="130" align="center">
          <template #default="{ row }">
            <span :class="getChangeClass(getChangeValue(row))">
              {{ formatChange(getChangeValue(row)) }}
            </span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getTechnicianRanking, type TechnicianRankingItem } from '@/api/data/ranking'

const loading = ref(false)
const tableData = ref<TechnicianRankingItem[]>([])

const filterForm = reactive({
  period: 'month',
  dimension: 'revenue',
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

function getChangeValue(row: TechnicianRankingItem): number {
  if (filterForm.dimension === 'revenue') return row.revenueChange ?? 0
  if (filterForm.dimension === 'service') return row.serviceChange ?? 0
  if (filterForm.dimension === 'rating') return row.ratingChange ?? 0
  return row.promotionChange ?? 0
}

function getChangeClass(value: number): string {
  if (value > 0) return 'change-up'
  if (value < 0) return 'change-down'
  return 'change-flat'
}

function formatChange(value: number): string {
  if (value > 0) return `+${value.toFixed(1)}%`
  if (value < 0) return `${value.toFixed(1)}%`
  return '0.0%'
}

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getTechnicianRanking({
      period: filterForm.period,
      dimension: filterForm.dimension,
      page: pagination.page,
      pageSize: pagination.pageSize,
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

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.technician-ranking {
  padding: 20px;
}

.filter-card {
  margin-bottom: 16px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  background: #F5F7FA;
}

.rank-top {
  color: #fff;
  background: #07C160;
}

.change-up {
  color: #07C160;
  font-weight: 600;
}

.change-down {
  color: #FA5151;
  font-weight: 600;
}

.change-flat {
  color: #909399;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
