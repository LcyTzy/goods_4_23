<template>
  <div class="product-manage">
    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="搜索商品名称/编号" style="width: 300px" clearable @keyup.enter="handleSearch" />
      <el-select v-model="searchForm.categoryId" placeholder="选择分类" clearable style="width: 200px; margin-left: 10px">
        <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <el-button type="primary" style="margin-left: 10px" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button type="success" style="margin-left: auto" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增商品
      </el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe style="width: 100%; margin-top: 20px">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="code" label="商品编号" width="120" />
      <el-table-column prop="name" label="商品名称" width="150" />
      <el-table-column prop="subName" label="副标题" width="200" show-overflow-tooltip />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="brand" label="品牌" width="100" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button :type="row.status === 1 ? 'warning' : 'success'" size="small" @click="handleToggleStatus(row)">
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pagination.current"
      v-model:page-size="pagination.size"
      :total="pagination.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next"
      style="margin-top: 20px; justify-content: flex-end"
      @current-change="getData"
      @size-change="getData"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="100px">
        <el-form-item label="商品编号" prop="code">
          <el-input v-model="editForm.code" />
        </el-form-item>
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="editForm.subName" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="editForm.price" :precision="2" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="editForm.stock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="品牌">
          <el-input v-model="editForm.brand" />
        </el-form-item>
        <el-form-item label="规格">
          <el-input v-model="editForm.spec" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="editForm.unit" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="editForm.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="editForm.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getProductList, addProduct, updateProduct, deleteProduct, updateProductStatus } from '@/api/product'
import { getCategoryList } from '@/api/category'

const loading = ref(false)
const tableData = ref([])
const categories = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('编辑商品')
const editFormRef = ref(null)

const searchForm = reactive({
  keyword: '',
  categoryId: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const editForm = reactive({
  id: null,
  code: '',
  name: '',
  subName: '',
  price: 0,
  stock: 0,
  brand: '',
  spec: '',
  unit: '',
  categoryId: null,
  status: 1
})

const editRules = {
  code: [{ required: true, message: '请输入商品编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

const getData = async () => {
  loading.value = true
  try {
    const res = await getProductList({
      pageNum: pagination.current,
      pageSize: pagination.size,
      keyword: searchForm.keyword,
      categoryId: searchForm.categoryId
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error('获取商品列表失败', error)
  } finally {
    loading.value = false
  }
}

const getCategories = async () => {
  try {
    const res = await getCategoryList()
    categories.value = res.data
  } catch (error) {
    console.error('获取分类列表失败', error)
  }
}

const handleSearch = () => {
  pagination.current = 1
  getData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.categoryId = null
  handleSearch()
}

const handleEdit = (row) => {
  Object.assign(editForm, row)
  dialogTitle.value = '编辑商品'
  dialogVisible.value = true
}

const handleAdd = () => {
  Object.assign(editForm, {
    id: null,
    code: '',
    name: '',
    subName: '',
    price: 0,
    stock: 0,
    brand: '',
    spec: '',
    unit: '',
    categoryId: null,
    status: 1
  })
  dialogTitle.value = '新增商品'
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await editFormRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    if (editForm.id) {
      await updateProduct(editForm)
    } else {
      await addProduct(editForm)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    getData()
  } catch (error) {
    console.error('保存失败', error)
  }
}

const handleToggleStatus = async (row) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await updateProductStatus(row.id, newStatus)
    ElMessage.success('操作成功')
    getData()
  } catch (error) {
    console.error('操作失败', error)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', { type: 'warning' })
    await deleteProduct(row.id)
    ElMessage.success('删除成功')
    getData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
    }
  }
}

onMounted(() => {
  getData()
  getCategories()
})
</script>

<style scoped>
.product-manage {
  padding: 20px;
}

.search-bar {
  display: flex;
  align-items: center;
}
</style>
