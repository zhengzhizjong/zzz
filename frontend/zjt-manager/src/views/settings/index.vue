<template>
  <div class="settings-page">
    <el-card shadow="hover">
      <template #header>
        <span>门店信息</span>
      </template>
      <el-descriptions :column="2" border v-if="storeInfo">
        <el-descriptions-item label="门店名称">{{ storeInfo.storeName }}</el-descriptions-item>
        <el-descriptions-item label="门店编号">{{ storeInfo.storeNo }}</el-descriptions-item>
        <el-descriptions-item label="门店类型">{{ storeInfo.storeType === 1 ? '直营店' : '加盟店' }}</el-descriptions-item>
        <el-descriptions-item label="营业状态">
          <el-tag :type="storeInfo.status === 1 ? 'success' : 'danger'">
            {{ storeInfo.status === 1 ? '营业中' : '已打烊' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ storeInfo.address || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ storeInfo.contactPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="营业时间">{{ storeInfo.businessStartTime || '09:00' }} - {{ storeInfo.businessEndTime || '21:00' }}</el-descriptions-item>
        <el-descriptions-item label="门店简介" :span="2">{{ storeInfo.description || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="暂无门店信息" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getStoreDetail } from '@/api/store'

const userStore = useUserStore()
const storeInfo = ref<any>(null)

onMounted(() => {
  loadStoreInfo()
})

async function loadStoreInfo() {
  if (!userStore.storeId) return
  try {
    const res: any = await getStoreDetail(userStore.storeId)
    storeInfo.value = res.data
  } catch {}
}
</script>

<style scoped>
.settings-page {
  padding: 20px;
}
</style>
