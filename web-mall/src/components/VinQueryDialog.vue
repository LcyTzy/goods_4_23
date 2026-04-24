<template>
  <el-dialog v-model="visible" title="VIN查配件" width="600px">
    <el-input
      v-model="vin"
      placeholder="请输入17位VIN码"
      maxlength="17"
      @keyup.enter="handleQuery"
    >
      <template #append>
        <el-button @click="handleQuery">查询</el-button>
      </template>
    </el-input>

    <div v-if="vinResult" class="vin-result" v-loading="loading">
      <el-descriptions :column="2" border class="vin-info">
        <el-descriptions-item label="产地">{{ vinResult.origin }}</el-descriptions-item>
        <el-descriptions-item label="品牌">{{ vinResult.brand }}</el-descriptions-item>
        <el-descriptions-item label="年款">{{ vinResult.year }}</el-descriptions-item>
        <el-descriptions-item label="匹配车型数">{{ vinResult.modelCount }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="vinResult.models && vinResult.models.length > 0" class="model-list">
        <h4>匹配车型</h4>
        <el-table :data="vinResult.models" @row-click="selectModel" style="cursor: pointer">
          <el-table-column prop="name" label="车型名称" />
          <el-table-column prop="year" label="年款" width="80" />
          <el-table-column prop="displacement" label="排量" width="80" />
        </el-table>
      </div>

      <div v-if="parts.length > 0" class="parts-list">
        <h4>适用配件</h4>
        <el-table :data="parts">
          <el-table-column prop="code" label="编号" width="120" />
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="oem" label="OE号" width="120" />
          <el-table-column prop="price" label="价格" width="100">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ row }">
              <el-button size="small" @click.stop="goToDetail(row.id)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const visible = ref(false)
const vin = ref('')
const vinResult = ref(null)
const parts = ref([])
const loading = ref(false)
const router = useRouter()

const open = () => {
  visible.value = true
  vin.value = ''
  vinResult.value = null
  parts.value = []
}

const handleQuery = async () => {
  if (vin.value.length !== 17) {
    ElMessage.warning('请输入17位VIN码')
    return
  }
  loading.value = true
  try {
    const res = await request.get('/api/epc/query-by-vin', {
      params: { vin: vin.value }
    })
    vinResult.value = res.data
    parts.value = []
  } catch (e) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const selectModel = async (row) => {
  loading.value = true
  try {
    const res = await request.get('/api/epc/parts', {
      params: { modelId: row.id }
    })
    parts.value = res.data
  } catch (e) {
    ElMessage.error('加载配件失败')
  } finally {
    loading.value = false
  }
}

const goToDetail = (id) => {
  router.push(`/product/${id}`)
  visible.value = false
}

defineExpose({ open })
</script>

<style scoped>
.vin-result {
  margin-top: 20px;
}

.vin-info {
  margin-bottom: 20px;
}

.model-list, .parts-list {
  margin-top: 20px;
}

.model-list h4, .parts-list h4 {
  margin-bottom: 10px;
  color: #333;
}
</style>
