<template>
  <div class="ai-consultation-page">
    <van-nav-bar title="AI智能问诊" left-arrow @click-left="router.back()" />

    <!-- 绿色渐变卡片 -->
    <div class="banner-card">
      <div class="banner-title">AI智能问诊</div>
      <div class="banner-desc">拍照舌诊 · 体质辨识 · 方案推荐</div>
    </div>

    <!-- 聊天区域 -->
    <div class="chat-area" ref="chatAreaRef">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <div class="chat-list">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            class="chat-msg"
            :class="{ 'chat-msg--user': msg.role === 'user', 'chat-msg--ai': msg.role === 'ai' }"
          >
            <div class="msg-avatar">
              <span v-if="msg.role === 'ai'">🤖</span>
              <span v-else>👤</span>
            </div>
            <div class="msg-bubble">
              <div class="msg-text">{{ msg.content }}</div>
              <div class="msg-time">{{ msg.time }}</div>
            </div>
          </div>
          <div v-if="aiLoading" class="chat-msg chat-msg--ai">
            <div class="msg-avatar"><span>🤖</span></div>
            <div class="msg-bubble">
              <van-loading size="16px" color="#07C160">AI正在思考...</van-loading>
            </div>
          </div>
        </div>
      </van-pull-refresh>
    </div>

    <!-- 底部输入框 -->
    <div class="input-bar">
      <van-field
        v-model="inputText"
        placeholder="请描述您的症状或健康问题"
        :border="false"
        class="input-field"
        @keyup.enter="sendMessage"
      />
      <van-button
        type="success"
        size="small"
        round
        :disabled="!inputText.trim() || aiLoading"
        @click="sendMessage"
      >发送</van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { askAiQuestion } from '@/api/ai'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

interface ChatMessage {
  role: 'user' | 'ai'
  content: string
  time: string
}

const messages = ref<ChatMessage[]>([])
const inputText = ref('')
const aiLoading = ref(false)
const refreshing = ref(false)
const chatAreaRef = ref<HTMLElement>()

onMounted(() => {
  messages.value = [
    {
      role: 'ai',
      content: '您好！我是忠济堂AI智能问诊助手，可以帮您分析症状、辨识体质、推荐调理方案。请描述您的症状或健康问题。',
      time: formatTime(new Date())
    }
  ]
})

function formatTime(date: Date) {
  const h = date.getHours().toString().padStart(2, '0')
  const m = date.getMinutes().toString().padStart(2, '0')
  return `${h}:${m}`
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || aiLoading.value) return

  const userMsg: ChatMessage = {
    role: 'user',
    content: text,
    time: formatTime(new Date())
  }
  messages.value.push(userMsg)
  inputText.value = ''
  scrollToBottom()

  aiLoading.value = true
  try {
    const memberId = userStore.userInfo?.id
    const res: any = await askAiQuestion({ question: text, memberId })
    const aiReply = res.data?.answer || res.data?.content || res.data || '抱歉，暂时无法分析，请稍后再试。'
    messages.value.push({
      role: 'ai',
      content: aiReply,
      time: formatTime(new Date())
    })
  } catch {
    messages.value.push({
      role: 'ai',
      content: '网络异常，请稍后再试。',
      time: formatTime(new Date())
    })
  } finally {
    aiLoading.value = false
    scrollToBottom()
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (chatAreaRef.value) {
      const el = chatAreaRef.value.querySelector('.chat-list')
      if (el) {
        el.scrollTop = el.scrollHeight
      }
    }
  })
}

async function onRefresh() {
  refreshing.value = false
  showToast('已刷新')
}
</script>

<style scoped lang="scss">
.ai-consultation-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}

.banner-card {
  background: linear-gradient(135deg, #07C160, #06ad56);
  padding: 24px 20px;
  color: #fff;

  .banner-title {
    font-size: 20px;
    font-weight: 600;
  }

  .banner-desc {
    font-size: 13px;
    opacity: 0.85;
    margin-top: 6px;
  }
}

.chat-area {
  flex: 1;
  overflow-y: auto;
  padding: 12px;

  .chat-list {
    min-height: 100%;
  }
}

.chat-msg {
  display: flex;
  margin-bottom: 16px;

  &--user {
    flex-direction: row-reverse;

    .msg-bubble {
      background: #07C160;
      color: #fff;
      border-radius: 12px 2px 12px 12px;
    }

    .msg-time {
      text-align: right;
      color: rgba(255, 255, 255, 0.7);
    }
  }

  &--ai {
    .msg-bubble {
      background: #fff;
      color: #303133;
      border-radius: 2px 12px 12px 12px;
    }

    .msg-time {
      color: #909399;
    }
  }

  .msg-avatar {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: #e8f5e9;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    flex-shrink: 0;
  }

  .msg-bubble {
    max-width: 70%;
    padding: 10px 14px;
    margin: 0 8px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);

    .msg-text {
      font-size: 14px;
      line-height: 1.6;
      white-space: pre-wrap;
      word-break: break-word;
    }

    .msg-time {
      font-size: 11px;
      margin-top: 4px;
    }
  }
}

.input-bar {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background: #fff;
  border-top: 1px solid #f0f0f0;
  gap: 8px;

  .input-field {
    flex: 1;
    background: #f5f5f5;
    border-radius: 20px;
    padding: 6px 12px;
  }
}
</style>
