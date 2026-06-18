<template>
  <div class="knowledge-page">
    <van-nav-bar title="养生知识" left-arrow @click-left="router.back()" />

    <van-tabs v-model:active="activeTab" color="#07C160" @change="onTabChange">
      <van-tab v-for="cat in categories" :key="cat.key" :title="cat.name" />
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="listLoading"
        :finished="finished"
        finished-text="没有更多了"
        @load="loadMore"
      >
        <div class="article-list">
          <div
            v-for="item in articles"
            :key="item.id"
            class="article-card"
          >
            <div class="article-cover" v-if="item.cover">
              <img :src="item.cover" :alt="item.title" />
            </div>
            <div class="article-info">
              <div class="article-title">{{ item.title }}</div>
              <div class="article-summary">{{ item.summary }}</div>
              <div class="article-meta">
                <span class="article-views">📖 {{ item.views }}</span>
                <span class="article-category">{{ item.categoryName }}</span>
              </div>
            </div>
          </div>
        </div>
      </van-list>

      <van-empty v-if="!listLoading && articles.length === 0" description="暂无文章" />
    </van-pull-refresh>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getArticleList } from '@/api/knowledge'

const router = useRouter()

const categories = [
  { key: 'tcm', name: '中医养生' },
  { key: 'acupoint', name: '穴位保健' },
  { key: 'constitution', name: '体质调理' },
  { key: 'seasonal', name: '季节养生' }
]

const activeTab = ref(0)
const articles = ref<any[]>([])
const refreshing = ref(false)
const listLoading = ref(false)
const finished = ref(false)
const page = ref(1)

// 模拟数据（API 失败时回退使用）
const mockArticles: Record<string, any[]> = {
  tcm: [
    { id: 1, title: '中医养生的基本原则', summary: '中医养生讲究天人合一、阴阳平衡，通过调节饮食、起居、情志来达到健康长寿的目的。', cover: '', views: 1234, categoryName: '中医养生' },
    { id: 2, title: '经络养生：打通身体气血通道', summary: '经络是人体气血运行的通道，通过按摩、艾灸等方式疏通经络，可以改善身体各部位的功能。', cover: '', views: 892, categoryName: '中医养生' },
    { id: 3, title: '中药茶饮养生指南', summary: '根据不同体质选择合适的中药茶饮，如枸杞菊花茶、黄芪红枣茶等，日常调理效果显著。', cover: '', views: 756, categoryName: '中医养生' },
    { id: 4, title: '中医四季养生法', summary: '春养肝、夏养心、秋养肺、冬养肾，顺应四时变化调整养生方法，才能事半功倍。', cover: '', views: 634, categoryName: '中医养生' },
    { id: 5, title: '气血不足的调理方法', summary: '气血不足表现为面色苍白、乏力、失眠等，可通过食疗、运动、中药调理来改善。', cover: '', views: 1023, categoryName: '中医养生' }
  ],
  acupoint: [
    { id: 6, title: '足三里穴：长寿第一穴', summary: '足三里是胃经合穴，常按可健脾和胃、扶正培元，是保健要穴之一。', cover: '', views: 1567, categoryName: '穴位保健' },
    { id: 7, title: '合谷穴：止痛万能穴', summary: '合谷穴位于手背虎口处，按摩可缓解头痛、牙痛等多种疼痛症状。', cover: '', views: 934, categoryName: '穴位保健' },
    { id: 8, title: '三阴交：女性养生要穴', summary: '三阴交是脾、肝、肾三经交会穴，对女性月经不调、气血不足有良好调理作用。', cover: '', views: 1123, categoryName: '穴位保健' },
    { id: 9, title: '涌泉穴：补肾强身第一穴', summary: '涌泉穴位于足底，是肾经井穴，常搓涌泉穴可补肾强身、改善睡眠。', cover: '', views: 876, categoryName: '穴位保健' }
  ],
  constitution: [
    { id: 10, title: '九种体质辨识与调理', summary: '中医将人体体质分为九种类型，了解自己的体质才能对症调理，达到最佳养生效果。', cover: '', views: 2045, categoryName: '体质调理' },
    { id: 11, title: '阳虚体质如何温补', summary: '阳虚体质者怕冷、手脚冰凉，应多吃温热食物，如羊肉、生姜，配合艾灸温补阳气。', cover: '', views: 1342, categoryName: '体质调理' },
    { id: 12, title: '阴虚体质的滋阴方法', summary: '阴虚体质者易口干、盗汗，应多食滋阴食物如银耳、百合，避免辛辣燥热食物。', cover: '', views: 987, categoryName: '体质调理' },
    { id: 13, title: '痰湿体质的祛湿攻略', summary: '痰湿体质者体型偏胖、易困倦，应健脾祛湿，多运动，少食甜腻食物。', cover: '', views: 856, categoryName: '体质调理' }
  ],
  seasonal: [
    { id: 14, title: '春季养生：养肝护肝正当时', summary: '春季万物复苏，肝气旺盛，应疏肝理气，多吃绿色蔬菜，保持心情舒畅。', cover: '', views: 1678, categoryName: '季节养生' },
    { id: 15, title: '夏季养生：清热解暑养心气', summary: '夏季炎热，心火旺盛，应清热解暑，适量食用苦瓜、绿豆等清凉食物。', cover: '', views: 1234, categoryName: '季节养生' },
    { id: 16, title: '秋季养生：润肺防燥是关键', summary: '秋季干燥，易伤肺阴，应多食润肺食物如梨、蜂蜜，注意保湿防燥。', cover: '', views: 1456, categoryName: '季节养生' },
    { id: 17, title: '冬季养生：补肾藏精好过冬', summary: '冬季寒冷，肾气当令，应温补肾阳，多食羊肉、核桃等温补食物，早睡晚起。', cover: '', views: 1890, categoryName: '季节养生' }
  ]
}

onMounted(() => {
  loadArticles()
})

function getCurrentCategory() {
  return categories[activeTab.value].key
}

async function loadArticles() {
  const cat = getCurrentCategory()
  try {
    const res: any = await getArticleList({ page: page.value, pageSize: 20, category: cat })
    const list = res.data?.list || res.data || []
    if (page.value === 1) {
      articles.value = list
    } else {
      articles.value = [...articles.value, ...list]
    }
    finished.value = !res.data?.hasMore && list.length < 20
  } catch {
    // API 失败时回退到 mock 数据
    const data = mockArticles[cat] || []
    articles.value = page.value === 1 ? data : [...articles.value, ...data]
    finished.value = true
  }
}

function onTabChange() {
  page.value = 1
  finished.value = false
  articles.value = []
  loadArticles()
}

async function onRefresh() {
  page.value = 1
  finished.value = false
  await loadArticles()
  refreshing.value = false
}

async function loadMore() {
  page.value++
  await loadArticles()
  listLoading.value = false
}
</script>

<style scoped lang="scss">
.knowledge-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.article-list {
  padding: 12px;
}

.article-card {
  display: flex;
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);

  .article-cover {
    width: 120px;
    height: 100px;
    flex-shrink: 0;
    background: linear-gradient(135deg, #e8f5e9, #c8e6c9);
    display: flex;
    align-items: center;
    justify-content: center;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .article-info {
    flex: 1;
    padding: 10px 12px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;

    .article-title {
      font-size: 15px;
      font-weight: 600;
      color: #303133;
      line-height: 1.4;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .article-summary {
      font-size: 12px;
      color: #909399;
      line-height: 1.5;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      margin-top: 4px;
    }

    .article-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 6px;

      .article-views {
        font-size: 11px;
        color: #909399;
      }

      .article-category {
        font-size: 11px;
        color: #07C160;
      }
    }
  }
}
</style>
