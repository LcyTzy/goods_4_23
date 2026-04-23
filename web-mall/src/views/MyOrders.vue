<template>
  <div class="orders-page">
    <h2 class="page-title">我的订单</h2>
    <div class="order-tabs">
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="全部" :name="null" />
        <el-tab-pane label="待付款" :name="0" />
        <el-tab-pane label="待发货" :name="1" />
        <el-tab-pane label="待收货" :name="2" />
        <el-tab-pane label="已完成" :name="3" />
      </el-tabs>
    </div>
    <div class="order-list" v-loading="loading">
      <div v-for="order in orders" :key="order.id" class="order-card">
        <div class="order-header">
          <span>订单号: {{ order.orderNo }}</span>
          <span class="order-status">{{ statusMap[order.status] }}</span>
        </div>
        <div class="order-body">
          <div class="order-products">
            <p v-if="order.items && order.items.length > 0">
              {{ order.items.map(i => i.productName).join(', ') }}
            </p>
            <p v-else>商品信息</p>
          </div>
          <div class="order-amount">
            <span>实付款: </span>
            <span class="price">¥{{ order.totalAmount }}</span>
          </div>
        </div>
        <div class="order-footer">
          <span class="order-time">{{ order.createTime }}</span>
          <div class="order-actions">
            <el-button v-if="order.status === 0" type="danger" size="small" @click="handlePay(order.id)">去付款</el-button>
            <el-button v-if="order.status === 0" size="small" @click="handleCancel(order.id)">取消订单</el-button>
            <el-button v-if="order.status === 2" type="primary" size="small" @click="handleConfirm(order.id)">确认收货</el-button>
            <el-button size="small" @click="handleViewDetail(order.id)">查看详情</el-button>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && orders.length === 0" description="暂无订单" />
    </div>
    <el-pagination
      v-if="total > 0"
      v-model:current-page="pagination.current"
      v-model:page-size="pagination.size"
      :total="total"
      layout="total, prev, pager, next"
      style="margin-top: 20px; justify-content: center"
      @current-change="loadOrders"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOrderList, cancelOrder, payOrder } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const orders = ref([])
const loading = ref(false)
const total = ref(0)
const activeTab = ref(null)

const statusMap = { 0: '待付款', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消' }

const pagination = reactive({ current: 1, size: 10 })

const handleTabClick = () => {
  pagination.current = 1
  loadOrders()
}

const handlePay = async (id) => {
  try {
    await payOrder(id)
    ElMessage.success('付款成功')
    loadOrders()
  } catch (e) {
    ElMessage.error('付款失败')
  }
}

const handleCancel = async (id) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', { type: 'warning' })
    await cancelOrder(id)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('取消失败')
  }
}

const handleConfirm = async (id) => {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '提示', { type: 'warning' })
    ElMessage.success('确认收货成功')
    loadOrders()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

const handleViewDetail = (id) => {
  ElMessage.info('订单详情功能开发中')
}

const loadOrders = async () => {
  loading.value = true
  try {
    const params = { pageNum: pagination.current, pageSize: pagination.size }
    if (activeTab.value !== null) {
      params.status = activeTab.value
    }
    const res = await getOrderList(params)
    orders.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error('加载订单失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(loadOrders)
</script>

<style scoped>
.orders-page { padding-bottom: 40px; }
.page-title { font-size: 22px; font-weight: bold; margin-bottom: 20px; padding-left: 12px; border-left: 4px solid #ff6600; }
.order-tabs { background: #fff; border-radius: 12px; padding: 0 20px; margin-bottom: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.order-list { display: flex; flex-direction: column; gap: 16px; }
.order-card { background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.order-header { display: flex; justify-content: space-between; padding: 12px 20px; background: #f5f5f5; font-size: 14px; }
.order-status { color: #ff6600; font-weight: bold; }
.order-body { display: flex; justify-content: space-between; padding: 16px 20px; align-items: center; }
.order-products { flex: 1; color: #666; }
.order-amount .price { font-size: 18px; font-weight: bold; color: #ff6600; }
.order-footer { display: flex; justify-content: space-between; padding: 12px 20px; border-top: 1px solid #f0f0f0; align-items: center; }
.order-time { color: #999; font-size: 13px; }
.order-actions { display: flex; gap: 8px; }
</style>
