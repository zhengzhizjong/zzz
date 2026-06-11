<template>
  <el-container class="admin-layout">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
      <div class="logo" @click="$router.push('/')">
        <span v-if="!isCollapse" class="logo-text">忠济堂</span>
        <span v-else class="logo-text-short">忠</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :router="true"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        class="aside-menu"
      >
        <el-menu-item index="/">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>

        <el-sub-menu index="/store">
          <template #title>
            <el-icon><OfficeBuilding /></el-icon>
            <span>门店管理</span>
          </template>
          <el-menu-item index="/store/list">门店列表</el-menu-item>
          <el-menu-item index="/store/rooms">房间管理</el-menu-item>
          <el-menu-item index="/store/schedules">排班管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/technician">
          <template #title>
            <el-icon><User /></el-icon>
            <span>技师管理</span>
          </template>
          <el-menu-item index="/technician/list">技师列表</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/service-item">
          <template #title>
            <el-icon><List /></el-icon>
            <span>服务项目管理</span>
          </template>
          <el-menu-item index="/service-item/list">服务项目列表</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/timeslot">
          <template #title>
            <el-icon><Clock /></el-icon>
            <span>时段配置</span>
          </template>
          <el-menu-item index="/timeslot/config">时段配置</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/appointment">
          <template #title>
            <el-icon><Calendar /></el-icon>
            <span>预约管理</span>
          </template>
          <el-menu-item index="/appointment/list">预约列表</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/trade">
          <template #title>
            <el-icon><ShoppingCart /></el-icon>
            <span>交易管理</span>
          </template>
          <el-menu-item index="/trade/orders">订单管理</el-menu-item>
          <el-menu-item index="/trade/refunds">退款管理</el-menu-item>
          <el-menu-item index="/trade/coupons">优惠券管理</el-menu-item>
          <el-menu-item index="/trade/treatment-cards">疗程卡管理</el-menu-item>
          <el-menu-item index="/trade/activities">活动管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/user">
          <template #title>
            <el-icon><Avatar /></el-icon>
            <span>用户管理</span>
          </template>
          <el-menu-item index="/user/employees">员工管理</el-menu-item>
          <el-menu-item index="/user/roles">角色权限</el-menu-item>
          <el-menu-item index="/user/members">会员管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/integration">
          <template #title>
            <el-icon><Connection /></el-icon>
            <span>集成管理</span>
          </template>
          <el-menu-item index="/integration/leads">线索管理</el-menu-item>
          <el-menu-item index="/integration/configs">平台对接配置</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/ai">
          <template #title>
            <el-icon><MagicStick /></el-icon>
            <span>AI管理</span>
          </template>
          <el-menu-item index="/ai/gateway">AI网关</el-menu-item>
          <el-menu-item index="/ai/daily-report">AI日报</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/billing">
          <template #title>
            <el-icon><Wallet /></el-icon>
            <span>计费管理</span>
          </template>
          <el-menu-item index="/billing/tenants">租户管理</el-menu-item>
          <el-menu-item index="/billing/plans">套餐管理</el-menu-item>
          <el-menu-item index="/billing/subscriptions">订阅管理</el-menu-item>
          <el-menu-item index="/billing/usage">用量统计</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/dashboard">
          <template #title>
            <el-icon><DataAnalysis /></el-icon>
            <span>数据看板</span>
          </template>
          <el-menu-item index="/dashboard/appointment-funnel">预约漏斗</el-menu-item>
          <el-menu-item index="/dashboard/promotion-funnel">推广漏斗</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/ranking">
          <template #title>
            <el-icon><Trophy /></el-icon>
            <span>排行榜</span>
          </template>
          <el-menu-item index="/ranking/store">门店排行</el-menu-item>
          <el-menu-item index="/ranking/technician">技师排行</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/dict">字典管理</el-menu-item>
          <el-menu-item index="/system/config">配置管理</el-menu-item>
          <el-menu-item index="/system/feature-flag">功能开关</el-menu-item>
          <el-menu-item index="/system/audit-logs">审计日志</el-menu-item>
          <el-menu-item index="/system/sms-logs">短信日志</el-menu-item>
          <el-menu-item index="/system/files">文件管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container class="main-container">
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <span class="page-title">{{ currentTitle }}</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              {{ userStore.userInfo?.name || '管理员' }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { removeToken } from '@/utils/auth'
import {
  HomeFilled, OfficeBuilding, User, List, Clock, Calendar,
  ShoppingCart, Avatar, Connection, MagicStick, Wallet,
  DataAnalysis, Trophy, Setting, Fold, Expand, ArrowDown
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => (route.meta.title as string) || '忠济堂')

function handleCommand(command: string) {
  if (command === 'logout') {
    removeToken()
    router.push('/login')
  }
}
</script>

<style scoped lang="scss">
.admin-layout {
  height: 100vh;
}

.aside {
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;
}

.logo {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #263445;
  cursor: pointer;
}

.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  letter-spacing: 2px;
}

.logo-text-short {
  color: #fff;
  font-size: 20px;
  font-weight: bold;
}

.aside-menu {
  border-right: none;
  height: calc(100vh - 50px);
  overflow-y: auto;
}

.aside-menu:not(.el-menu--collapse) {
  width: 220px;
}

.main-container {
  overflow: hidden;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 20px;
  height: 50px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #606266;

  &:hover {
    color: #409eff;
  }
}

.page-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #606266;
  font-size: 14px;

  &:hover {
    color: #409eff;
  }
}

.main-content {
  background: #f0f2f5;
  overflow-y: auto;
  padding: 0;
}
</style>
