<template>
  <div class="login-page">
    <div class="login-header">
      <div class="logo-wrap">
        <span class="logo-text">忠济堂</span>
      </div>
      <p class="slogan">传承中医精髓 守护健康人生</p>
    </div>

    <div class="login-form">
      <van-cell-group inset>
        <van-field
          v-model="phone"
          type="tel"
          maxlength="11"
          label="手机号"
          placeholder="请输入手机号"
          clearable
        />
        <van-field
          v-model="verifyCode"
          type="digit"
          maxlength="6"
          label="验证码"
          placeholder="请输入验证码"
        >
          <template #button>
            <van-button
              size="small"
              type="primary"
              :disabled="countdown > 0"
              @click="sendCode"
            >
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </van-button>
          </template>
        </van-field>
      </van-cell-group>

      <div class="login-btn-wrap">
        <van-button
          type="primary"
          block
          round
          :loading="loading"
          loading-text="登录中..."
          @click="handleLogin"
        >
          登录
        </van-button>
      </div>
    </div>

    <div class="divider">
      <van-divider>其他登录方式</van-divider>
    </div>

    <div class="wechat-login">
      <van-button
        block
        round
        plain
        type="success"
        :loading="wxLoading"
        loading-text="登录中..."
        @click="handleWechatLogin"
      >
        微信一键登录
      </van-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { sendVerifyCode } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { checkLogin } from '@/utils/auth'

const router = useRouter()
const userStore = useUserStore()

const phone = ref('')
const verifyCode = ref('')
const countdown = ref(0)
const loading = ref(false)
const wxLoading = ref(false)

let timer: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  if (checkLogin()) {
    router.replace('/home')
  }
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

function startCountdown() {
  countdown.value = 60
  timer = setInterval(() => {
    if (countdown.value <= 1) {
      if (timer) clearInterval(timer)
      timer = null
      countdown.value = 0
    } else {
      countdown.value--
    }
  }, 1000)
}

async function sendCode() {
  if (countdown.value > 0) return
  if (!phone.value || !/^1[3-9]\d{9}$/.test(phone.value)) {
    showToast('请输入正确的手机号')
    return
  }

  try {
    await sendVerifyCode(phone.value)
    showToast('验证码已发送')
    startCountdown()
  } catch {}
}

async function handleLogin() {
  if (!phone.value || !/^1[3-9]\d{9}$/.test(phone.value)) {
    showToast('请输入正确的手机号')
    return
  }
  if (!verifyCode.value) {
    showToast('请输入验证码')
    return
  }

  loading.value = true
  try {
    await userStore.loginBySms(phone.value, verifyCode.value)
    showToast('登录成功')
    const redirect = (router.currentRoute.value.query.redirect as string) || '/home'
    setTimeout(() => router.replace(redirect), 500)
  } catch {} finally {
    loading.value = false
  }
}

async function handleWechatLogin() {
  wxLoading.value = true
  try {
    // H5环境微信登录 - 跳转微信授权页
    const appId = import.meta.env.VITE_WECHAT_APP_ID || ''
    if (appId) {
      const redirectUri = encodeURIComponent(window.location.origin + '/login')
      const state = Math.random().toString(36).substring(2)
      window.location.href = `https://open.weixin.qq.com/connect/oauth2/authorize?appid=${appId}&redirect_uri=${redirectUri}&response_type=code&scope=snsapi_userinfo&state=${state}#wechat_redirect`
    } else {
      showToast('微信登录未配置')
    }
  } finally {
    wxLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  background: #fff;
  padding: 0 20px;
}

.login-header {
  text-align: center;
  padding: 60px 0 40px;

  .logo-wrap {
    margin-bottom: 12px;
  }

  .logo-text {
    font-size: 32px;
    font-weight: 700;
    color: #07C160;
  }

  .slogan {
    font-size: 14px;
    color: #909399;
    margin: 8px 0 0;
  }
}

.login-form {
  .login-btn-wrap {
    margin: 30px 16px 0;
  }
}

.divider {
  margin: 30px 0 20px;
}

.wechat-login {
  padding: 0 16px;
}
</style>
