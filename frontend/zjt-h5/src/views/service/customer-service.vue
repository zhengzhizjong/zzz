<template>
  <div class="customer-service-page">
    <van-nav-bar title="在线客服" left-arrow @click-left="router.back()" />

    <!-- 聊天区域 -->
    <div class="chat-area" ref="chatAreaRef">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <div class="chat-list">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            class="chat-msg"
            :class="{ 'chat-msg--user': msg.role === 'user', 'chat-msg--bot': msg.role === 'bot' }"
          >
            <div class="msg-avatar">
              <span v-if="msg.role === 'bot'">🧑‍💼</span>
              <span v-else>👤</span>
            </div>
            <div class="msg-bubble">
              <div class="msg-text">{{ msg.content }}</div>
              <div class="msg-time">{{ msg.time }}</div>
            </div>
          </div>
          <div v-if="botLoading" class="chat-msg chat-msg--bot">
            <div class="msg-avatar"><span>🧑‍💼</span></div>
            <div class="msg-bubble">
              <van-loading size="16px" color="#07C160">正在回复...</van-loading>
            </div>
          </div>
        </div>
      </van-pull-refresh>
    </div>

    <!-- 常见问题快捷按钮 -->
    <div class="quick-questions">
      <div class="quick-title">常见问题</div>
      <div class="quick-list">
        <van-button
          v-for="(q, index) in quickQuestions"
          :key="index"
          size="small"
          round
          plain
          type="success"
          class="quick-btn"
          @click="onQuickQuestion(q)"
        >{{ q.question }}</van-button>
      </div>
    </div>

    <!-- 底部输入框 -->
    <div class="input-bar">
      <van-field
        v-model="inputText"
        placeholder="请输入您的问题"
        :border="false"
        class="input-field"
        @keyup.enter="sendMessage"
      />
      <van-button
        type="success"
        size="small"
        round
        :disabled="!inputText.trim() || botLoading"
        @click="sendMessage"
      >发送</van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

interface ChatMessage {
  role: 'user' | 'bot'
  content: string
  time: string
}

const messages = ref<ChatMessage[]>([])
const inputText = ref('')
const botLoading = ref(false)
const refreshing = ref(false)
const chatAreaRef = ref<HTMLElement>()

const quickQuestions = [
  { question: '预约如何取消？', answer: '您可以在"我的预约"中找到对应预约，点击取消按钮即可。请注意，预约开始前2小时可免费取消，2小时内取消可能会扣除部分费用。' },
  { question: '如何修改预约？', answer: '您可以在"我的预约"中取消当前预约后重新预约，或联系门店前台帮您修改预约信息。' },
  { question: '营业时间？', answer: '忠济堂门店营业时间一般为 09:00 - 22:00，部分门店可能有所不同，建议您查看具体门店详情页确认营业时间。' },
  { question: '如何退款？', answer: '如需退款，请在"我的订单"中找到对应订单申请退款。未使用的服务可全额退款，已使用的服务不支持退款。退款将在3-5个工作日内原路返回。' }
]

onMounted(() => {
  messages.value = [
    {
      role: 'bot',
      content: '您好！我是忠济堂在线客服，有什么可以帮您的？您可以直接输入问题，或点击下方常见问题快捷按钮。',
      time: formatTime(new Date())
    }
  ]
})

function formatTime(date: Date) {
  const h = date.getHours().toString().padStart(2, '0')
  const m = date.getMinutes().toString().padStart(2, '0')
  return `${h}:${m}`
}

function onQuickQuestion(q: { question: string; answer: string }) {
  messages.value.push({
    role: 'user',
    content: q.question,
    time: formatTime(new Date())
  })
  scrollToBottom()

  botLoading.value = true
  setTimeout(() => {
    messages.value.push({
      role: 'bot',
      content: q.answer,
      time: formatTime(new Date())
    })
    botLoading.value = false
    scrollToBottom()
  }, 600)
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || botLoading.value) return

  messages.value.push({
    role: 'user',
    content: text,
    time: formatTime(new Date())
  })
  inputText.value = ''
  scrollToBottom()

  botLoading.value = true
  setTimeout(() => {
    messages.value.push({
      role: 'bot',
      content: '感谢您的咨询！您的问题已记录，我们的客服人员会尽快为您解答。如需紧急帮助，请拨打门店电话。',
      time: formatTime(new Date())
    })
    botLoading.value = false
    scrollToBottom()
  }, 800)
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
}
</script>

<style scoped lang="scss">
.customer-service-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
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

  &--bot {
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

.quick-questions {
  padding: 10px 12px;
  background: #fff;
  border-top: 1px solid #f0f0f0;

  .quick-title {
    font-size: 12px;
    color: #909399;
    margin-bottom: 8px;
  }

  .quick-list {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;

    .quick-btn {
      font-size: 12px;
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
