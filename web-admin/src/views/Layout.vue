<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <span class="logo-icon">🚗</span>
        <span class="logo-text">战途汽配商城</span>
      </div>
      <el-menu 
        :default-active="$route.path" 
        router 
        class="sidebar-menu"
        background-color="#1d1e4c" 
        text-color="#a3a6c7" 
        active-text-color="#ffffff"
        active-background-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>数据概览</span>
        </el-menu-item>
        <el-menu-item index="/products">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/categories">
          <el-icon><Menu /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/orders">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-sub-menu index="inventory-menu">
          <template #title>
            <el-icon><Box /></el-icon>
            <span>进销存管理</span>
          </template>
          <el-menu-item index="/purchase">采购管理</el-menu-item>
          <el-menu-item index="/inventory">库存流水</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="epc-menu">
          <template #title>
            <el-icon><Connection /></el-icon>
            <span>EPC管理</span>
          </template>
          <el-menu-item index="/epc/vehicle">车型管理</el-menu-item>
          <el-menu-item index="/epc/part-relation">配件关联</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/service-appointment">
          <el-icon><Calendar /></el-icon>
          <span>服务预约</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container class="main-container">
      <el-header class="header">
        <div class="header-left">
          <span class="breadcrumb">{{ $route.meta.title || '管理后台' }}</span>
        </div>
        <div class="header-right">
          <el-button type="danger" size="small" @click="handleLogout" class="logout-btn">
            <el-icon><SwitchButton /></el-icon>
            退出登录
          </el-button>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const handleLogout = () => {
  localStorage.removeItem('adminToken')
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  background-color: #f0f2f5;
}

.sidebar {
  background-color: #1d1e4c;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
  overflow-y: auto;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background: linear-gradient(135deg, #1d1e4c 0%, #2d2f6b 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  color: #ffffff;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
}

.sidebar-menu {
  border-right: none;
}

.sidebar-menu .el-menu-item {
  height: 50px;
  line-height: 50px;
  margin: 4px 8px;
  border-radius: 8px;
  transition: all 0.3s;
}

.sidebar-menu .el-menu-item:hover {
  background-color: rgba(64, 158, 255, 0.2) !important;
}

.sidebar-menu .el-menu-item.is-active {
  background-color: #409EFF !important;
  color: #ffffff !important;
}

.main-container {
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #ffffff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  padding: 0 24px;
  height: 64px;
}

.header-left {
  display: flex;
  align-items: center;
}

.breadcrumb {
  font-size: 18px;
  font-weight: 600;
  color: #1d1e4c;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 6px;
}

.main-content {
  background-color: #f0f2f5;
  padding: 24px;
  overflow-y: auto;
}
</style>
