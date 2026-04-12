import request from '../request'
import { adaptPageData, buildPageQuery } from '../_adapters/page'

function formatDateTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

function adaptTeacherScoreItem(item = {}) {
  const score = Number(item.score || 0)
  const fullScore = Number(item.fullScore || 100)
  return {
    id: item.id,
    scoreId: item.id ? `S-${String(item.id).padStart(8, '0')}` : '-',
    examId: item.examId || null,
    examName: item.examName || '未命名考试',
    className: item.className || '未关联班级',
    courseName: item.courseName || '未关联课程',
    studentId: item.studentId || null,
    studentName: item.studentName || '未命名学员',
    studentNo: item.studentNo || '',
    score,
    fullScore,
    scoreRate: fullScore > 0 ? Number(((score / fullScore) * 100).toFixed(1)) : 0,
    rankInClass: item.rankInClass || null,
    teacherComment: item.teacherComment || '',
    publishTime: formatDateTime(item.publishTime),
    createdAt: formatDateTime(item.createdAt),
    updatedAt: formatDateTime(item.updatedAt)
  }
}

export async function fetchTeacherScorePage(params = {}) {
  const data = await request({
    url: '/teacher/scores/page',
    method: 'post',
    data: buildPageQuery({
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 200,
      examId: params.examId,
      studentId: params.studentId
    })
  })
  const page = adaptPageData(data)
  return {
    ...page,
    list: page.list.map(adaptTeacherScoreItem)
  }
}
