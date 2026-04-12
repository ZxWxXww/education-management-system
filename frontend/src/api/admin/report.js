import { fetchAdminAssignmentPage } from './assignment'
import { fetchAdminAttendanceOverview } from './attendance'
import { fetchAdminBillPage } from './bill'
import { fetchAdminClassPage } from './class'
import { fetchAdminCoursePage } from './course'
import { fetchAdminGradePage } from './grade'
import { fetchAdminTeachingResourceOverview } from './resource'

function round(value, digits = 1) {
  const number = Number(value || 0)
  return Number(number.toFixed(digits))
}

function groupBy(list, keyGetter) {
  return list.reduce((acc, item) => {
    const key = keyGetter(item)
    if (!key) return acc
    acc[key] = acc[key] || []
    acc[key].push(item)
    return acc
  }, {})
}

function getLatestTime(items = [], fields = ['updatedAt', 'createdAt', 'publishTime']) {
  return items
    .flatMap((item) => fields.map((field) => item[field]).filter(Boolean))
    .filter((value) => value && value !== '-')
    .sort()
    .pop() || '-'
}

function getTopRows(rows, key, limit = 5) {
  return [...rows].sort((a, b) => Number(b[key] || 0) - Number(a[key] || 0)).slice(0, limit)
}

export async function fetchAdminReportOverview() {
  const [courses, classes, bills, assignments, grades, attendanceOverview, resourceOverview] = await Promise.all([
    fetchAdminCoursePage({ pageNum: 1, pageSize: 300 }),
    fetchAdminClassPage({ pageNum: 1, pageSize: 300 }),
    fetchAdminBillPage({ pageNum: 1, pageSize: 300 }),
    fetchAdminAssignmentPage({ pageNum: 1, pageSize: 300 }),
    fetchAdminGradePage({ pageNum: 1, pageSize: 300 }),
    fetchAdminAttendanceOverview(),
    fetchAdminTeachingResourceOverview()
  ])

  const reportCards = [
    { label: '课程与班级', value: `${courses.list.length + classes.list.length}`, tip: '真实核心教学数据' },
    { label: '作业与成绩记录', value: `${assignments.list.length + grades.list.length}`, tip: '实时聚合' },
    { label: '最近更新动态', value: `${resourceOverview.recentActivities.length}`, tip: '来自真实资源总览' },
    { label: '对比对象', value: `${attendanceOverview.classOverview.length}`, tip: '班级维度可对比' }
  ]

  const hotReports = [
    {
      name: '班级到课与作业提交总览',
      owner: '教务处',
      createdAt: getLatestTime(assignments.list, ['createdAt', 'updatedAt']),
      updatedAt: getLatestTime(attendanceOverview.classOverview.map((item) => ({ updatedAt: getLatestTime([item], ['updatedAt']) })), ['updatedAt'])
    },
    {
      name: '成绩分布与班级排名概览',
      owner: '教学运营',
      createdAt: getLatestTime(grades.list, ['createdAt', 'publishTime']),
      updatedAt: getLatestTime(grades.list, ['updatedAt', 'publishTime'])
    },
    {
      name: '财务与考勤联动日报',
      owner: '校区运营',
      createdAt: getLatestTime(bills.list, ['createdAt']),
      updatedAt: getLatestTime(bills.list, ['updatedAt'])
    }
  ]

  return { reportCards, hotReports }
}

function buildClassMetrics(assignments, grades, attendanceOverview, classes) {
  const assignmentGroup = groupBy(assignments, (item) => item.className)
  const gradeGroup = groupBy(grades, (item) => item.className)
  const attendanceMap = new Map(attendanceOverview.classOverview.map((item) => [item.className, item]))
  const classList = classes.list.map((item) => item.className)

  return classList.map((className) => {
    const assignmentRows = assignmentGroup[className] || []
    const gradeRows = gradeGroup[className] || []
    const attendance = attendanceMap.get(className)
    const shouldAttend = Number(attendance?.shouldAttend || 0)
    const abnormalCount = Number(attendance?.abnormalCount || 0)
    const totalAssignment = assignmentRows.reduce((sum, item) => sum + Number(item.total || 0), 0)
    const totalSubmitted = assignmentRows.reduce((sum, item) => sum + Number(item.submitted || 0), 0)
    const avgScore = gradeRows.length
      ? round(gradeRows.reduce((sum, item) => sum + Number(item.score || 0), 0) / gradeRows.length, 1)
      : 0
    return {
      target: className,
      teacherName: attendance?.teacherName || assignmentRows[0]?.teacher || gradeRows[0]?.teacher || '-',
      attendanceRate: Number(attendance?.normalRate || 0),
      homeworkRate: totalAssignment ? round((totalSubmitted / totalAssignment) * 100, 1) : 0,
      averageScore: avgScore,
      abnormalRate: shouldAttend ? round((abnormalCount / shouldAttend) * 100, 1) : 0
    }
  })
}

function buildTeacherMetrics(assignments, grades, attendanceOverview) {
  const teacherNames = [
    ...new Set([
      ...assignments.map((item) => item.teacher),
      ...grades.map((item) => item.teacher),
      ...attendanceOverview.classOverview.map((item) => item.teacherName)
    ].filter(Boolean))
  ]
  return teacherNames.map((teacherName) => {
    const assignmentRows = assignments.filter((item) => item.teacher === teacherName)
    const gradeRows = grades.filter((item) => item.teacher === teacherName)
    const attendanceRows = attendanceOverview.classOverview.filter((item) => item.teacherName === teacherName)
    const totalAssignment = assignmentRows.reduce((sum, item) => sum + Number(item.total || 0), 0)
    const totalSubmitted = assignmentRows.reduce((sum, item) => sum + Number(item.submitted || 0), 0)
    const totalShouldAttend = attendanceRows.reduce((sum, item) => sum + Number(item.shouldAttend || 0), 0)
    const totalAbnormal = attendanceRows.reduce((sum, item) => sum + Number(item.abnormalCount || 0), 0)
    const attendanceRate = attendanceRows.length
      ? round(attendanceRows.reduce((sum, item) => sum + Number(item.normalRate || 0), 0) / attendanceRows.length, 1)
      : 0
    return {
      target: teacherName,
      teacherName,
      attendanceRate,
      homeworkRate: totalAssignment ? round((totalSubmitted / totalAssignment) * 100, 1) : 0,
      averageScore: gradeRows.length ? round(gradeRows.reduce((sum, item) => sum + Number(item.score || 0), 0) / gradeRows.length, 1) : 0,
      abnormalRate: totalShouldAttend ? round((totalAbnormal / totalShouldAttend) * 100, 1) : 0
    }
  })
}

function buildCourseMetrics(assignments, grades, attendanceOverview, classes) {
  const classCourseMap = new Map(classes.list.map((item) => [item.className, item.courseName]))
  const courseNames = [
    ...new Set([
      ...assignments.map((item) => item.course),
      ...grades.map((item) => item.course),
      ...attendanceOverview.classOverview.map((item) => classCourseMap.get(item.className))
    ].filter(Boolean))
  ]
  return courseNames.map((courseName) => {
    const assignmentRows = assignments.filter((item) => item.course === courseName)
    const gradeRows = grades.filter((item) => item.course === courseName)
    const attendanceRows = attendanceOverview.classOverview.filter((item) => classCourseMap.get(item.className) === courseName)
    const totalAssignment = assignmentRows.reduce((sum, item) => sum + Number(item.total || 0), 0)
    const totalSubmitted = assignmentRows.reduce((sum, item) => sum + Number(item.submitted || 0), 0)
    const totalShouldAttend = attendanceRows.reduce((sum, item) => sum + Number(item.shouldAttend || 0), 0)
    const totalAbnormal = attendanceRows.reduce((sum, item) => sum + Number(item.abnormalCount || 0), 0)
    const attendanceRate = attendanceRows.length
      ? round(attendanceRows.reduce((sum, item) => sum + Number(item.normalRate || 0), 0) / attendanceRows.length, 1)
      : 0
    return {
      target: courseName,
      teacherName: '-',
      attendanceRate,
      homeworkRate: totalAssignment ? round((totalSubmitted / totalAssignment) * 100, 1) : 0,
      averageScore: gradeRows.length ? round(gradeRows.reduce((sum, item) => sum + Number(item.score || 0), 0) / gradeRows.length, 1) : 0,
      abnormalRate: totalShouldAttend ? round((totalAbnormal / totalShouldAttend) * 100, 1) : 0
    }
  })
}

export async function fetchAdminComparativeAnalysis(compareType = '班级对比') {
  const [assignments, grades, attendanceOverview, classes] = await Promise.all([
    fetchAdminAssignmentPage({ pageNum: 1, pageSize: 300 }),
    fetchAdminGradePage({ pageNum: 1, pageSize: 300 }),
    fetchAdminAttendanceOverview(),
    fetchAdminClassPage({ pageNum: 1, pageSize: 300 })
  ])

  let rows = []
  if (compareType === '教师对比') {
    rows = buildTeacherMetrics(assignments.list, grades.list, attendanceOverview)
  } else if (compareType === '课程对比') {
    rows = buildCourseMetrics(assignments.list, grades.list, attendanceOverview, classes)
  } else {
    rows = buildClassMetrics(assignments.list, grades.list, attendanceOverview, classes)
  }

  const rankedRows = rows
    .map((item) => ({
      ...item,
      compositeScore: round(item.attendanceRate * 0.35 + item.homeworkRate * 0.35 + item.averageScore * 0.2 + (100 - item.abnormalRate) * 0.1, 1)
    }))
    .sort((a, b) => b.compositeScore - a.compositeScore)
    .map((item, index) => ({ ...item, rank: index + 1 }))

  const average = (key) => rankedRows.length ? round(rankedRows.reduce((sum, item) => sum + Number(item[key] || 0), 0) / rankedRows.length, 1) : 0
  const kpiCards = [
    { label: '平均到课率', value: `${average('attendanceRate')}%`, delta: `${rankedRows.length}个对象`, tone: 'blue' },
    { label: '作业提交率', value: `${average('homeworkRate')}%`, delta: '真实作业聚合', tone: 'green' },
    { label: '平均成绩', value: `${average('averageScore')} 分`, delta: '真实成绩聚合', tone: 'purple' },
    { label: '异常考勤占比', value: `${average('abnormalRate')}%`, delta: '真实考勤聚合', tone: 'orange' }
  ]

  const topRows = getTopRows(rankedRows, 'compositeScore', 5)
  const scoreTrendLabels = topRows.map((item) => item.target)
  const scoreSeriesA = topRows.map((item) => item.averageScore)
  const scoreSeriesB = topRows.map(() => average('averageScore'))
  const insightCards = [
    topRows[0]
      ? { title: '高贡献对象', content: `${topRows[0].target} 在到课率、作业提交率和平均成绩综合表现最佳。` }
      : { title: '高贡献对象', content: '当前暂无可分析对象。' },
    topRows[topRows.length - 1]
      ? { title: '需重点跟进', content: `${topRows[topRows.length - 1].target} 当前综合表现偏弱，建议优先排查作业与考勤环节。` }
      : { title: '需重点跟进', content: '当前暂无可分析对象。' },
    {
      title: '风险提醒',
      content: average('abnormalRate') > 5
        ? '整体异常考勤占比偏高，建议优先检查到课波动明显的对象。'
        : '整体异常考勤占比处于可控范围，可继续关注成绩与作业趋势。'
    }
  ]

  return {
    compareType,
    campusOptions: ['全部校区'],
    kpiCards,
    compareLabels: topRows.map((item) => item.target),
    courseSeries: topRows.map((item) => item.attendanceRate),
    homeworkSeries: topRows.map((item) => item.homeworkRate),
    scoreTrendLabels,
    scoreSeriesA,
    scoreSeriesB,
    compareRows: rankedRows,
    insightCards
  }
}
