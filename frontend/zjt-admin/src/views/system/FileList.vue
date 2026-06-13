<template>
  <div class="file-list">
    <!-- 操作栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="文件名">
          <el-input v-model="searchForm.name" placeholder="请输入文件名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="全部类型" clearable>
            <el-option label="图片" value="image" />
            <el-option label="文档" value="document" />
            <el-option label="视频" value="video" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="primary" @click="handleUpload">上传文件</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="fileType" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getFileTypeTag(row.fileType)" size="small">
              {{ getFileTypeLabel(row.fileType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="大小" width="120" align="right">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdBy" label="上传人" width="120" />
        <el-table-column prop="createdAt" label="上传时间" width="170" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 上传弹窗 -->
    <el-dialog
      v-model="uploadVisible"
      title="上传文件"
      width="500px"
      destroy-on-close
    >
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :limit="5"
        :on-change="handleFileChange"
        :file-list="fileList"
        multiple
        drag
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">支持图片/文档/视频，单次最多5个文件</div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="uploadVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploadLoading" @click="handleSubmitUpload">确认上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadFile } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { getFileList, uploadFile, deleteFile, type FileInfo } from '@/api/system/file'

const loading = ref(false)
const uploadLoading = ref(false)
const tableData = ref<FileInfo[]>([])
const uploadVisible = ref(false)
const fileList = ref<UploadFile[]>([])

const searchForm = reactive({
  name: '',
  type: undefined as string | undefined,
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

function getFileTypeLabel(type: string): string {
  if (type === 'image') return '图片'
  if (type === 'document') return '文档'
  if (type === 'video') return '视频'
  return '其他'
}

function getFileTypeTag(type: string): string {
  if (type === 'image') return 'success'
  if (type === 'document') return ''
  if (type === 'video') return 'warning'
  return 'info'
}

function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      name: searchForm.name || undefined,
      type: searchForm.type || undefined,
    }
    const res: any = await getFileList(params)
    tableData.value = res.data.list || []
    pagination.total = res.data.pagination.total
  } catch {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.name = ''
  searchForm.type = undefined
  pagination.page = 1
  fetchData()
}

function handleUpload() {
  fileList.value = []
  uploadVisible.value = true
}

function handleFileChange(_file: UploadFile, uploadFiles: UploadFile[]) {
  fileList.value = uploadFiles
}

async function handleSubmitUpload() {
  if (fileList.value.length === 0) {
    ElMessage.warning('请选择要上传的文件')
    return
  }

  uploadLoading.value = true
  try {
    for (const file of fileList.value) {
      if (!file.raw) continue
      const formData = new FormData()
      formData.append('file', file.raw)
      await uploadFile(formData)
    }
    ElMessage.success('上传成功')
    uploadVisible.value = false
    fetchData()
  } catch {
    // 错误已在拦截器中处理
  } finally {
    uploadLoading.value = false
  }
}

async function handleDelete(row: FileInfo) {
  try {
    await ElMessageBox.confirm('确认删除该文件？删除后不可恢复', '提示', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消',
    })
    await deleteFile(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // 用户取消或错误已在拦截器中处理
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.file-list {
  padding: 20px;
}

.search-card {
  margin-bottom: 16px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
