<template>
  <div class="consultation-page">
    <el-row :gutter="12" class="full-height">
      <!-- 左侧：待跟进线索列表 -->
      <el-col :span="6">
        <el-card shadow="never" class="full-card">
          <template #header>
            <span>待跟进线索</span>
            <el-badge :value="leads.length" class="badge" />
          </template>
          <div v-loading="leadsLoading" class="lead-list">
            <div
              v-for="item in leads"
              :key="item.id"
              :class="['lead-item', { active: selectedLead?.id === item.id }]"
              @click="selectLead(item)"
            >
              <div class="lead-top">
                <span class="lead-name">{{ item.customerName || '未知' }}</span>
                <el-tag size="small" type="warning">{{ sourceLabel(item.source) }}</el-tag>
              </div>
              <div class="lead-phone">{{ item.customerPhone }}</div>
              <div class="lead-content">{{ item.content || '暂无咨询内容' }}</div>
            </div>
            <div v-if="leads.length === 0 && !leadsLoading" class="empty-text">暂无待跟进线索</div>
          </div>
        </el-card>
      </el-col>

      <!-- 中间：咨询对话 -->
      <el-col :span="12">
        <el-card shadow="never" class="full-card">
          <template #header>
            <span>咨询对话</span>
            <span v-if="selectedLead" class="dialog-title-extra">- {{ selectedLead.customerName }}</span>
          </template>
          <div v-if="!selectedLead" class="empty-text">请从左侧选择一个线索开始对话</div>
          <div v-else class="chat-area">
            <div class="chat-messages" ref="messagesRef">
              <div
                v-for="(msg, idx) in messages"
                :key="idx"
                :class="['chat-msg', msg.from === 'staff' ? 'msg-right' : 'msg-left']"
              >
                <div class="msg-bubble">{{ msg.content }}</div>
                <div class="msg-time">{{ msg.time }}</div>
              </div>
              <div v-if="messages.length === 0" class="empty-text">暂无对话记录，发送消息开始沟通</div>
            </div>
            <div class="chat-input-area">
              <div class="quick-replies">
                <el-tag
                  v-for="(reply, idx) in quickReplies"
                  :key="idx"
                  class="quick-tag"
                  effect="plain"
                  @click="sendQuickReply(reply)"
                >{{ reply }}</el-tag>
              </div>
              <div class="input-row">
                <el-input
                  v-model="inputMsg"
                  placeholder="输入消息..."
                  @keyup.enter="sendMessage"
                  style="flex: 1"
                />
                <el-button type="primary" @click="sendMessage" :disabled="!inputMsg.trim()">发送</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：门店状态 -->
      <el-col :span="6">
        <el-card shadow="never" class="full-card">
          <template #header>
            <span>门店状态</span>
          </template>

          <el-divider content-position="left">技师状态</el-divider>
          <div v-loading="storeLoading" class="technician-list">
            <div v-for="tech in technicians" :key="tech.id" class="tech-row">
              <el-avatar :size="32" :style="{ background: tech.onDuty ? '#67c23a' : '#909399' }">
                {{ tech.name?.charAt(0) || '?' }}
              </el-avatar>
              <div class="tech-info">
                <span class="tech-name">{{ tech.name }}</span>
                <el-tag :type="tech.onDuty ? 'success' : 'info'" size="small">
                  {{ tech.onDuty ? '在岗' : '离岗' }}
                </el-tag>
              </div>
            </div>
            <div v-if="technicians.length === 0 && !storeLoading" class="empty-text-sm">暂无技师信息</div>
          </div>

          <el-divider content-position="left">房间状态</el-divider>
          <div class="room-list">
            <div v-for="room in rooms" :key="room.id" class="room-item">
              <div :class="['room-dot', room.status === 'available' ? 'dot-green' : 'dot-red']"></div>
              <span class="room-name">{{ room.name }}</span>
              <el-tag :type="room.status === 'available' ? 'success' : 'danger'" size="small">
                {{ room.status === 'available' ? '空闲' : '使用中' }}
              </el-tag>
            </div>
            <div v-if="rooms.length === 0" class="empty-text-sm">暂无房间信息</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { getLeadList, updateLead } from '@/api/lead'
import { getTechnicianList } from '@/api/technician'
import { get } from '@/utils/request'
import { getStoreId } from '@/utils/auth'

// 线索列表
const leadsLoading = ref(false)
const leads = ref<any[]>([])
const selectedLead = ref<any>(null)

// 对话
const inputMsg = ref('')
const messagesRef = ref<HTMLElement | null>(null)
const messages = ref<{ from: string; content: string; time: string }[]>([])
const quickReplies = [
  '您好，请问有什么可以帮您？',
  '我们门店位于XX路XX号，欢迎到店体验',
  '目前有优惠活动，详情可以为您介绍',
  '好的，我帮您预约一下',
  '稍后会有专业技师为您服务'
]

// 门店状态
const storeLoading = ref(false)
const technicians = ref<any[]>([])
const rooms = ref<any[]>([])

function sourceLabel(source: string) {
  const map: Record<string, string> = { online: '线上', phone: '电话', walk_in: '到店', referral: '转介绍', other: '其他' }
  return map[source] || source
}

async function loadLeads() {
  leadsLoading.value = true
  try {
    const res: any = await getLeadList({ status: 1, page: 1, pageSize: 50 })
    leads.value = res.data?.list || res.data?.records || []
  } catch {
    // handled by interceptor
  } finally {
    leadsLoading.value = false
  }
}

function selectLead(item: any) {
  selectedLead.value = item
  messages.value = []
  inputMsg.value = ''
}

function sendMessage() {
  if (!inputMsg.value.trim() || !selectedLead.value) return
  const now = new Date()
  const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
  messages.value.push({ from: 'staff', content: inputMsg.value.trim(), time })
  inputMsg.value = ''
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

function sendQuickReply(text: string) {
  if (!selectedLead.value) {
    ElMessage.warning('请先选择一个线索')
    return
  }
  const now = new Date()
  const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
  messages.value.push({ from: 'staff', content: text, time })
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

async function loadStoreStatus() {
  storeLoading.value = true
  const storeId = getStoreId() || ''
  try {
    const [techRes, roomRes] = await Promise.all([
      getTechnicianList({ storeId, status: 1, page: 1, pageSize: 50 }),
      get('/api/v1/store/rooms', { params: { storeId, page: 1, pageSize: 50 } }).catch(() => ({ data: { list: [], records: [] } }))
    ])
    technicians.value = (techRes as any).data?.list || (techRes as any).data?.records || []
    rooms.value = (roomRes as any).data?.list || (roomRes as any).data?.records || []
  } catch {
    // handled by interceptor
  } finally {
    storeLoading.value = false
  }
}

onMounted(() => {
  loadLeads()
  loadStoreStatus()
})
</script>

<style scoped>
.consultation-page {
  padding: 4px;
  height: calc(100vh - 120px);
}
.full-height {
  height: 100%;
}
.full-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.full-card :deep(.el-card__body) {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.badge {
  margin-left: 8px;
}
.lead-list {
  flex: 1;
  overflow-y: auto;
}
.lead-item {
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.lead-item:hover {
  border-color: #409eff;
}
.lead-item.active {
  border-color: #409eff;
  background: #ecf5ff;
}
.lead-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}
.lead-name {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}
.lead-phone {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}
.lead-content {
  font-size: 12px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.dialog-title-extra {
  color: #909399;
  font-weight: normal;
  font-size: 14px;
}
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}
.chat-msg {
  margin-bottom: 12px;
  display: flex;
  flex-direction: column;
}
.msg-left {
  align-items: flex-start;
}
.msg-right {
  align-items: flex-end;
}
.msg-bubble {
  max-width: 70%;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-all;
}
.msg-left .msg-bubble {
  background: #f4f4f5;
  color: #303133;
}
.msg-right .msg-bubble {
  background: #409eff;
  color: #fff;
}
.msg-time {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 2px;
}
.chat-input-area {
  border-top: 1px solid #ebeef5;
  padding-top: 8px;
}
.quick-replies {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}
.quick-tag {
  cursor: pointer;
  font-size: 12px;
}
.quick-tag:hover {
  color: #409eff;
  border-color: #409eff;
}
.input-row {
  display: flex;
  gap: 8px;
}
.technician-list {
  margin-bottom: 8px;
}
.tech-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.tech-info {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.tech-name {
  font-size: 14px;
  color: #303133;
}
.room-list {
  margin-bottom: 8px;
}
.room-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.room-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.dot-green {
  background: #67c23a;
}
.dot-red {
  background: #f56c6c;
}
.room-name {
  flex: 1;
  font-size: 14px;
  color: #303133;
}
.empty-text {
  color: #909399;
  text-align: center;
  padding: 40px 0;
}
.empty-text-sm {
  color: #909399;
  text-align: center;
  padding: 12px 0;
  font-size: 13px;
}
</style>
