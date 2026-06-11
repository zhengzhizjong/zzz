<template>
  <div class="search-page">
    <van-nav-bar title="搜索" left-arrow @click-left="router.back()">
      <template #title>
        <van-search
          v-model="keyword"
          placeholder="搜索门店、技师、服务项目"
          shape="round"
          autofocus
          @search="onSearch"
          @clear="onClear"
        />
      </template>
    </van-nav-bar>

    <!-- 搜索历史 & 热门搜索 -->
    <div v-if="!hasSearched" class="search-suggest">
      <div class="suggest-section" v-if="searchHistory.length > 0">
        <div class="section-header">
          <span class="section-title">搜索历史</span>
          <span class="section-action" @click="clearHistory">清空</span>
        </div>
        <div class="tag-list">
          <van-tag
            v-for="(item, index) in searchHistory"
            :key="index"
            size="medium"
            type="default"
            class="history-tag"
            @click="onTagClick(item)"
          >{{ item }}</van-tag>
        </div>
      </div>

      <div class="suggest-section">
        <div class="section-header">
          <span class="section-title">热门搜索</span>
        </div>
        <div class="tag-list">
          <van-tag
            v-for="(item, index) in hotKeywords"
            :key="index"
            size="medium"
            type="default"
            class="hot-tag"
            @click="onTagClick(item)"
          >{{ item }}</van-tag>
        </div>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div v-else class="search-result">
      <van-tabs v-model:active="activeTab" color="#07C160" @change="onTabChange">
        <van-tab title="门店">
          <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
            <div v-if="resultLoading" class="loading-wrap">
              <van-loading type="spinner" color="#07C160">搜索中...</van-loading>
            </div>
            <div v-else-if="storeResults.length > 0" class="result-list">
              <van-cell
                v-for="item in storeResults"
                :key="item.id"
                :title="item.storeName || item.name"
                :label="item.address || ''"
                is-link
                @click="router.push(`/store/detail/${item.id}`)"
              />
            </div>
            <van-empty v-else description="暂无门店结果" />
          </van-pull-refresh>
        </van-tab>

        <van-tab title="技师">
          <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
            <div v-if="resultLoading" class="loading-wrap">
              <van-loading type="spinner" color="#07C160">搜索中...</van-loading>
            </div>
            <div v-else-if="technicianResults.length > 0" class="result-list">
              <van-cell
                v-for="item in technicianResults"
                :key="item.id"
                :title="item.name"
                :label="item.levelName || ''"
                is-link
                @click="router.push(`/technician/detail/${item.id}`)"
              />
            </div>
            <van-empty v-else description="暂无技师结果" />
          </van-pull-refresh>
        </van-tab>

        <van-tab title="服务项目">
          <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
            <div v-if="resultLoading" class="loading-wrap">
              <van-loading type="spinner" color="#07C160">搜索中...</van-loading>
            </div>
            <div v-else-if="serviceResults.length > 0" class="result-list">
              <van-cell
                v-for="item in serviceResults"
                :key="item.id"
                :title="item.itemName || item.name"
                :label="item.description || ''"
                is-link
                @click="router.push('/service/list')"
              />
            </div>
            <van-empty v-else description="暂无服务项目结果" />
          </van-pull-refresh>
        </van-tab>
      </van-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { getStoreList } from '@/api/store'
import { getTechnicianList } from '@/api/technician'
import { getServiceItemList } from '@/api/service'

const router = useRouter()

const keyword = ref('')
const hasSearched = ref(false)
const activeTab = ref(0)
const resultLoading = ref(false)
const refreshing = ref(false)

const searchHistory = ref<string[]>([])
const hotKeywords = ref(['推拿', '艾灸', '拔罐', '足疗', '肩颈调理', '中医养生'])

const storeResults = ref<any[]>([])
const technicianResults = ref<any[]>([])
const serviceResults = ref<any[]>([])

const HISTORY_KEY = 'zjt_search_history'

// 初始化搜索历史
try {
  const stored = localStorage.getItem(HISTORY_KEY)
  if (stored) {
    searchHistory.value = JSON.parse(stored)
  }
} catch {}

function saveHistory(word: string) {
  const list = searchHistory.value.filter((item) => item !== word)
  list.unshift(word)
  if (list.length > 10) list.pop()
  searchHistory.value = list
  localStorage.setItem(HISTORY_KEY, JSON.stringify(list))
}

function clearHistory() {
  searchHistory.value = []
  localStorage.removeItem(HISTORY_KEY)
}

function onTagClick(word: string) {
  keyword.value = word
  onSearch()
}

function onClear() {
  hasSearched.value = false
  storeResults.value = []
  technicianResults.value = []
  serviceResults.value = []
}

async function onSearch() {
  const word = keyword.value.trim()
  if (!word) return

  saveHistory(word)
  hasSearched.value = true
  resultLoading.value = true

  try {
    await Promise.all([searchStores(word), searchTechnicians(word), searchServices(word)])
  } finally {
    resultLoading.value = false
  }
}

async function searchStores(word: string) {
  try {
    const res: any = await getStoreList({ keyword: word, page: 1, pageSize: 20 })
    storeResults.value = res.data?.list || res.data?.records || res.data || []
  } catch {
    storeResults.value = []
  }
}

async function searchTechnicians(word: string) {
  try {
    const res: any = await getTechnicianList({ keyword: word, page: 1, pageSize: 20 })
    technicianResults.value = res.data?.list || res.data?.records || res.data || []
  } catch {
    technicianResults.value = []
  }
}

async function searchServices(word: string) {
  try {
    const res: any = await getServiceItemList({ keyword: word, page: 1, pageSize: 20 })
    serviceResults.value = res.data?.list || res.data?.records || res.data || []
  } catch {
    serviceResults.value = []
  }
}

function onTabChange() {
  // Tab切换时无需重新搜索，数据已加载
}

async function onRefresh() {
  try {
    const word = keyword.value.trim()
    if (word) {
      await Promise.all([searchStores(word), searchTechnicians(word), searchServices(word)])
    }
  } finally {
    refreshing.value = false
  }
}
</script>

<style scoped lang="scss">
.search-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.search-suggest {
  padding: 16px;

  .suggest-section {
    margin-bottom: 20px;

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .section-title {
        font-size: 15px;
        font-weight: 600;
        color: #303133;
      }

      .section-action {
        font-size: 13px;
        color: #909399;
      }
    }

    .tag-list {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      .history-tag,
      .hot-tag {
        cursor: pointer;
        padding: 4px 12px;
      }
    }
  }
}

.search-result {
  .loading-wrap {
    display: flex;
    justify-content: center;
    padding: 40px 0;
  }

  .result-list {
    padding: 0 0 12px;
  }
}
</style>
