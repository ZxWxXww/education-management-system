<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Calendar, Search } from '@element-plus/icons-vue'
import { fetchTeacherClassPage } from '../../api/teacher/class'

const queryForm = ref({
  keyword: '',
  grade: '全部年级',
  status: '全部状态'
})
const router = useRouter()
const loading = ref(false)
const classList = ref([])

const gradeOptions = ['全部年级', '高一', '高二', '高三']
const statusOptions = ['全部状态', '进行中', '待开课', '已结课']

const classStatistics = computed(() => {
  const total = classList.value.length
  const running = classList.value.filter((item) => item.status === '进行中').length
  const students = classList.value.reduce((sum, item) => sum + Number(item.studentCount || 0), 0)
  const attendanceSource = classList.value.filter((item) => Number(item.attendanceRate) > 0)
  const attendance = attendanceSource.length
    ? (attendanceSource.reduce((sum, item) => sum + Number(item.attendanceRate || 0), 0) / attendanceSource.length).toFixed(1)
    : '0.0'
  return [
    { key: 'total', label: '班级总数', value: total, unit: '个' },
    { key: 'running', label: '进行中', value: running, unit: '个' },
    { key: 'students', label: '在读学员', value: students, unit: '人' },
    { key: 'attendance', label: '本周平均出勤', value: attendance, unit: '%' }
  ]
})

const filteredClassList = computed(() =>
  classList.value.filter((item) => {
    const keyword = queryForm.value.keyword.trim()
    const matchKeyword =
      !keyword ||
      item.className.includes(keyword) ||
      item.classId.includes(keyword) ||
      item.courseName.includes(keyword)
    const matchGrade = queryForm.value.grade === '全部年级' || queryForm.value.grade === item.grade
    const matchStatus = queryForm.value.status === '全部状态' || queryForm.value.status === item.status
    return matchKeyword && matchGrade && matchStatus
  })
)

const classInsights = computed(() => {
  const rows = filteredClassList.value
  const totalStudents = rows.reduce((sum, item) => sum + Number(item.studentCount || 0), 0)
  const withoutSchedule = rows.filter((item) => !item.schedule).length
  const zeroAttendance = rows.filter((item) => Number(item.attendanceRate || 0) <= 0).length
  const completed = rows.filter((item) => item.status !== '进行中').length

  return [
    {
      key: 'visible',
      label: '当前筛选班级',
      value: `${rows.length} 个`,
      desc: '列表和统计均直接来自教师班级接口'
    },
    {
      key: 'students',
      label: '覆盖学员',
      value: `${totalStudents} 人`,
      desc: '按真实班级人数汇总'
    },
    {
      key: 'schedule',
      label: '缺少排课周期',
      value: `${withoutSchedule} 个`,
      desc: '班级开始/结束时间未配置时会出现在这里'
    },
    {
      key: 'attendance',
      label: '未形成出勤记录',
      value: `${zeroAttendance} 个`,
      desc: completed > 0 ? `其中已结课 ${completed} 个` : '当前筛选范围内暂无已结课班级'
    }
  ]
})

function resetQuery() {
  queryForm.value.keyword = ''
  queryForm.value.grade = '全部年级'
  queryForm.value.status = '全部状态'
  loadClassPage()
}

async function loadClassPage() {
  loading.value = true
  try {
    const page = await fetchTeacherClassPage({
      keyword: queryForm.value.keyword,
      pageNum: 1,
      pageSize: 100
    })
    classList.value = page.list
  } catch (error) {
    ElMessage.error('班级数据加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function openClassDetail(row) {
  if (!row?.id) {
    ElMessage.warning('当前班级缺少有效 ID，暂时无法查看详情')
    return
  }
  router.push(`/teacher/courses/classes/${row.id}`)
}

function statusType(status) {
  if (status === '进行中') return 'success'
  if (status === '待开课') return 'warning'
  return 'info'
}

onMounted(() => {
  loadClassPage()
})
</script>

<template>
  <div class="class-management">
    <section class="management-hero">
      <div>
        <p class="management-hero__path">我的课程 / 班级管理</p>
        <h1 class="management-hero__title">班级管理</h1>
        <p class="management-hero__desc">集中管理授课班级、课表进度与学员学习状态</p>
      </div>
    </section>

    <section class="statistics-grid">
      <article v-for="item in classStatistics" :key="item.key" class="statistics-card">
        <p class="statistics-card__label">{{ item.label }}</p>
        <p class="statistics-card__value">{{ item.value }}<small>{{ item.unit }}</small></p>
      </article>
    </section>

    <section class="panel">
      <header class="panel__head">
        <h2><el-icon><Search /></el-icon> 班级筛选</h2>
      </header>
      <el-form :inline="true" class="query-form">
        <el-form-item>
          <el-input
            v-model="queryForm.keyword"
            clearable
            placeholder="输入班级名称 / 课程 / 班级ID"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryForm.grade" style="width: 140px">
            <el-option v-for="item in gradeOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryForm.status" style="width: 140px">
            <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadClassPage">查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="content-grid">
      <article v-loading="loading" class="panel">
        <header class="panel__head panel__head--table">
          <h2><el-icon><Calendar /></el-icon> 班级列表</h2>
          <span>共 {{ filteredClassList.length }} 个班级</span>
        </header>
        <el-table :data="filteredClassList" stripe>
          <el-table-column prop="classId" label="班级ID" min-width="120" />
          <el-table-column prop="className" label="班级名称" min-width="180" />
          <el-table-column prop="courseName" label="课程" min-width="140" />
          <el-table-column prop="grade" label="年级" min-width="90" />
          <el-table-column prop="schedule" label="上课时间" min-width="170" />
          <el-table-column prop="classroom" label="教室" min-width="100" />
          <el-table-column label="人数" min-width="110">
            <template #default="{ row }">{{ row.studentCount }}/{{ row.maxStudentCount }}</template>
          </el-table-column>
          <el-table-column label="出勤率" min-width="90">
            <template #default="{ row }">{{ row.attendanceRate }}%</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" min-width="100">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)" effect="light">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="120" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openClassDetail(row)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </article>

      <article class="panel panel--aside">
        <header class="panel__head">
          <h2>真实数据提示</h2>
          <span>{{ classInsights.length }} 项</span>
        </header>
        <div class="insight-list">
          <div v-for="item in classInsights" :key="item.key" class="insight-item">
            <p class="insight-item__label">{{ item.label }}</p>
            <strong class="insight-item__value">{{ item.value }}</strong>
            <p class="insight-item__desc">{{ item.desc }}</p>
          </div>
        </div>
      </article>
    </section>
  </div>
</template>

<style scoped>
.class-management {
  max-width: 1160px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.management-hero {
  border-radius: 16px;
  background: linear-gradient(135deg, #1d4ed8 0%, #1e40af 100%);
  padding: 22px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.management-hero__path,
.management-hero__title,
.management-hero__desc {
  margin: 0;
}

.management-hero__path {
  color: rgba(255, 255, 255, 0.72);
  font-size: 12px;
}

.management-hero__title {
  margin-top: 6px;
  color: #ffffff;
  font-size: 30px;
  line-height: 38px;
  font-weight: 800;
}

.management-hero__desc {
  margin-top: 8px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.statistics-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.statistics-card {
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  padding: 14px;
}

.statistics-card__label {
  margin: 0;
  color: #6b7280;
  font-size: 12px;
}

.statistics-card__value {
  margin: 8px 0 0;
  color: #111827;
  font-size: 28px;
  line-height: 34px;
  font-weight: 800;
}

.statistics-card__value small {
  margin-left: 4px;
  color: #6b7280;
  font-size: 12px;
  font-weight: 500;
}

.panel {
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  padding: 16px;
}

.panel__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.panel__head h2 {
  margin: 0;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #111827;
  font-size: 16px;
}

.panel__head span {
  color: #6b7280;
  font-size: 12px;
}

.panel__head--table {
  margin-bottom: 10px;
}

.query-form {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 12px;
}

.panel--aside {
  align-self: start;
}

.insight-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.insight-item {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.insight-item__label,
.insight-item__desc {
  margin: 0;
  color: #374151;
  font-size: 13px;
  line-height: 20px;
}

.insight-item__value {
  color: #111827;
  font-size: 20px;
  line-height: 28px;
}

@media (max-width: 1080px) {
  .statistics-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .management-hero {
    flex-direction: column;
    padding: 18px;
  }

  .statistics-grid {
    grid-template-columns: 1fr;
  }
}

:global(html.dark) .statistics-card,
:global(html.dark) .panel,
:global(html.dark) .insight-item {
  background: #1a2028;
  border-color: #2b3442;
}

:global(html.dark) .statistics-card__value,
:global(html.dark) .panel__head h2,
:global(html.dark) .insight-item__value {
  color: #e5eaf3;
}

:global(html.dark) .statistics-card__label,
:global(html.dark) .statistics-card__value small,
:global(html.dark) .panel__head span,
:global(html.dark) .insight-item__label,
:global(html.dark) .insight-item__desc {
  color: #9ba7ba;
}
</style>

