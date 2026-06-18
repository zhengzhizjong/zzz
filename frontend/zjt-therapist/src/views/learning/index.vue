<template>
  <div class="learning-page">
    <div class="page-title">学习中心</div>

    <!-- 分类Tab -->
    <van-tabs v-model:active="activeTab" color="#07C160" title-active-color="#07C160">
      <van-tab title="中医基础">
        <div class="course-list">
          <div v-for="item in courses[0]" :key="item.id" class="course-card" @click="goCourse(item)">
            <div class="course-cover">{{ item.icon }}</div>
            <div class="course-info">
              <div class="course-title">{{ item.title }}</div>
              <div class="course-meta">{{ item.type }} · {{ item.duration }}</div>
            </div>
          </div>
        </div>
      </van-tab>
      <van-tab title="推拿手法">
        <div class="course-list">
          <div v-for="item in courses[1]" :key="item.id" class="course-card" @click="goCourse(item)">
            <div class="course-cover">{{ item.icon }}</div>
            <div class="course-info">
              <div class="course-title">{{ item.title }}</div>
              <div class="course-meta">{{ item.type }} · {{ item.duration }}</div>
            </div>
          </div>
        </div>
      </van-tab>
      <van-tab title="艾灸技术">
        <div class="course-list">
          <div v-for="item in courses[2]" :key="item.id" class="course-card" @click="goCourse(item)">
            <div class="course-cover">{{ item.icon }}</div>
            <div class="course-info">
              <div class="course-title">{{ item.title }}</div>
              <div class="course-meta">{{ item.type }} · {{ item.duration }}</div>
            </div>
          </div>
        </div>
      </van-tab>
      <van-tab title="沟通技巧">
        <div class="course-list">
          <div v-for="item in courses[3]" :key="item.id" class="course-card" @click="goCourse(item)">
            <div class="course-cover">{{ item.icon }}</div>
            <div class="course-info">
              <div class="course-title">{{ item.title }}</div>
              <div class="course-meta">{{ item.type }} · {{ item.duration }}</div>
            </div>
          </div>
        </div>
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCourseList } from '@/api/learning'
import { showToast } from 'vant'

const router = useRouter()
const activeTab = ref(0)
const loading = ref(false)

// 分类名称与索引映射
const categoryNames = ['中医基础', '推拿手法', '艾灸技术', '沟通技巧']

// fallback mock 数据
const mockCourses = [
  // 中医基础
  [
    { id: 1, title: '中医经络基础理论', type: '文章', duration: '15分钟', icon: '📖' },
    { id: 2, title: '阴阳五行学说入门', type: '视频', duration: '25分钟', icon: '🎬' },
    { id: 3, title: '脏腑功能与调理', type: '文章', duration: '20分钟', icon: '📖' }
  ],
  // 推拿手法
  [
    { id: 4, title: '基本推拿手法详解', type: '视频', duration: '30分钟', icon: '🎬' },
    { id: 5, title: '颈肩部推拿实操', type: '视频', duration: '35分钟', icon: '🎬' },
    { id: 6, title: '腰部推拿技巧', type: '文章', duration: '18分钟', icon: '📖' }
  ],
  // 艾灸技术
  [
    { id: 7, title: '艾灸基础知识', type: '文章', duration: '12分钟', icon: '📖' },
    { id: 8, title: '常用艾灸穴位定位', type: '视频', duration: '28分钟', icon: '🎬' },
    { id: 9, title: '艾灸注意事项与禁忌', type: '文章', duration: '10分钟', icon: '📖' }
  ],
  // 沟通技巧
  [
    { id: 10, title: '客户沟通入门技巧', type: '文章', duration: '15分钟', icon: '📖' },
    { id: 11, title: '如何处理客户投诉', type: '视频', duration: '20分钟', icon: '🎬' },
    { id: 12, title: '服务礼仪规范', type: '文章', duration: '12分钟', icon: '📖' }
  ]
]

const courses = ref<any[][]>([[], [], [], []])

function normalizeCourse(item: any) {
  return {
    id: item.id,
    title: item.title || item.name || '',
    type: item.type || (item.media_type === 'video' ? '视频' : '文章'),
    duration: item.duration || item.read_time || '',
    icon: item.icon || (item.type === '视频' || item.media_type === 'video' ? '🎬' : '📖'),
    category: item.category ?? item.category_name ?? ''
  }
}

function groupByCategory(list: any[]) {
  const grouped: any[][] = [[], [], [], []]
  for (const item of list) {
    const normalized = normalizeCourse(item)
    const catIdx = categoryNames.indexOf(normalized.category)
    if (catIdx >= 0) {
      grouped[catIdx].push(normalized)
    } else {
      // 无法匹配分类时归入第一个 tab
      grouped[0].push(normalized)
    }
  }
  return grouped
}

async function fetchCourses() {
  loading.value = true
  try {
    const res: any = await getCourseList({ page: 1, pageSize: 20 })
    const list = res?.data?.list || res?.data || res?.list || []
    if (Array.isArray(list) && list.length > 0) {
      courses.value = groupByCategory(list)
    } else {
      // API 返回空数据，使用 mock
      courses.value = mockCourses
    }
  } catch (e) {
    // API 失败，fallback 到 mock 数据
    console.warn('获取课程列表失败，使用本地数据', e)
    courses.value = mockCourses
    showToast('课程数据加载失败，显示本地数据')
  } finally {
    loading.value = false
  }
}

function goCourse(course: any) {
  router.push(`/learning/${course.id}`)
}

onMounted(() => {
  fetchCourses()
})
</script>

<style scoped>
.learning-page {
  padding: 12px;
  background: #fff;
  min-height: 100vh;
}

.page-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin-bottom: 12px;
}

.course-list {
  padding: 12px 0;
}

.course-card {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 10px;
  margin-bottom: 10px;
}

.course-cover {
  width: 50px;
  height: 50px;
  background: #e8f7ef;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.course-info {
  flex: 1;
  margin-left: 12px;
}

.course-title {
  font-size: 15px;
  color: #333;
  font-weight: 500;
}

.course-meta {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>
