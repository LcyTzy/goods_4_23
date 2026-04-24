<template>
  <div class="mall-layout">
    <header class="mall-header">
      <div class="header-top">
        <div class="container header-content">
          <div class="logo" @click="$router.push('/home')">
            <span class="logo-icon">🚗</span>
            <span class="logo-text">战途汽配商城</span>
          </div>
          <div class="search-box">
            <el-input v-model="searchKeyword" placeholder="搜索商品/VIN码/OEM号" @keyup.enter="handleSearch" class="search-input">
              <template #append>
                <el-button @click="handleSearch"><el-icon><Search /></el-icon></el-button>
              </template>
            </el-input>
            <div class="search-tips">
              <span class="tip-label">热门搜索:</span>
              <el-tag size="small" class="search-tag" @click="searchKeyword='机油'; handleSearch()">机油</el-tag>
              <el-tag size="small" class="search-tag" @click="searchKeyword='刹车片'; handleSearch()">刹车片</el-tag>
              <el-tag size="small" class="search-tag" @click="searchKeyword='滤清器'; handleSearch()">滤清器</el-tag>
              <el-button size="small" type="primary" link @click="openVinQuery">VIN查配件</el-button>
            </div>
          </div>
          <div class="header-actions">
            <template v-if="userStore.token">
              <el-dropdown>
                <span class="user-info">
                  <el-icon><User /></el-icon>
              {{ userStore.userInfo?.nickname || userStore.userInfo?.phone || '用户' }}
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="$router.push('/profile')">个人中心</el-dropdown-item>
                    <el-dropdown-item @click="$router.push('/orders')">我的订单</el-dropdown-item>
                    <el-dropdown-item @click="$router.push('/coupons')">优惠券</el-dropdown-item>
                    <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <template v-else>
              <el-button type="primary" link @click="$router.push('/login')">登录</el-button>
              <el-button type="primary" link @click="$router.push('/register')">注册</el-button>
            </template>
            <el-button type="primary" link @click="$router.push('/cart')">
              <el-icon><ShoppingCart /></el-icon>
              购物车
            </el-button>
          </div>
        </div>
      </div>
      <nav class="header-nav">
        <div class="container nav-content">
          <el-menu :default-active="activeMenu" mode="horizontal" :ellipsis="false" @select="handleNavSelect">
            <el-menu-item index="/home">首页</el-menu-item>
            <el-menu-item index="/products">全部商品</el-menu-item>
            <el-menu-item index="/service">预约修车</el-menu-item>
          </el-menu>
        </div>
      </nav>
    </header>
    <main class="mall-main">
      <div class="container">
        <router-view />
      </div>
    </main>
    <footer class="mall-footer">
      <div class="container">
        <p>战途汽配商城 - 品质配件 极速配送</p>
        <p>Copyright © 2024 Zhantu Auto Parts Mall. All rights reserved.</p>
      </div>
    </footer>
    <VinQueryDialog ref="vinQueryDialog" />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import VinQueryDialog from '@/components/VinQueryDialog.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const searchKeyword = ref('')
const vinQueryDialog = ref(null)

const openVinQuery = () => {
  vinQueryDialog.value?.open()
}

const activeMenu = computed(() => {
  if (route.path.startsWith('/products')) return '/products'
  return route.path
})

const handleSearch = () => {
  router.push({ path: '/products', query: { keyword: searchKeyword.value } })
}

const handleNavSelect = (index) => {
  router.push(index)
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/home')
}
</script>

<style scoped>
.mall-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.mall-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-top {
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  flex-shrink: 0;
}

.logo-icon {
  font-size: 28px;
}

.logo-text {
  font-size: 20px;
  font-weight: bold;
  color: #ff6600;
}

.search-box {
  flex: 1;
  max-width: 500px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 20px;
}

.search-tips {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
  font-size: 12px;
}

.tip-label {
  color: #999;
}

.search-tag {
  cursor: pointer;
  transition: all 0.2s;
}

.search-tag:hover {
  background-color: #ff6600;
  color: #fff;
  border-color: #ff6600;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #666;
}

.header-nav {
  padding: 0;
}

.nav-content :deep(.el-menu) {
  border-bottom: none;
  justify-content: center;
}

.mall-main {
  flex: 1;
  padding: 20px 0;
}

.mall-footer {
  background: #333;
  color: #999;
  text-align: center;
  padding: 24px 0;
  margin-top: auto;
}

.mall-footer p {
  margin: 4px 0;
  font-size: 14px;
}
</style>
