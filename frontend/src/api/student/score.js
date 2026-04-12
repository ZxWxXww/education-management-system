import request from '../request'
import { adaptPageData, buildPageQuery } from '../_adapters/page'
import { getStatusMeta, SCORE_PUBLISH_STATUS_OPTIONS } from '../../constants/status'

function formatDateTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

function adaptStudentScoreItem(item = {}) {
  const score = Number(item.score || 0)
  const fullScore = Number(item.fullScore || 100)
  const rankInClass = item.rankInClass || null
  const publishStatus = getStatusMeta(SCORE_PUBLISH_STATUS_OPTIONS, item.examStatus, '未知')
  return {
    id: item.id,
    examId: item.examId || null,
    examName: item.examName || '-',
    courseName: item.courseName || '-',
    examStatusCode: publishStatus.value || '',
    examStatusLabel: publishStatus.label,
    examStatusTagType: publishStatus.tagType,
    score,
    fullScore,
    scoreRate: fullScore > 0 ? Number(((score / fullScore) * 100).toFixed(1)) : 0,
    rankInClass,
    teacherComment: item.teacherComment || '',
    publishTime: formatDateTime(item.publishTime),
    examTime: formatDateTime(item.examTime),
    createdAt: formatDateTime(item.createdAt),
    updatedAt: formatDateTime(item.updatedAt)
  }
}

export async function fetchStudentScorePage(params = {}) {
  const data = await request({
    url: '/student/scores/page',
    method: 'post',
    data: buildPageQuery({
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 200,
      examId: params.examId
    })
  })
  const page = adaptPageData(data)
  return {
    ...page,
    list: page.list.map(adaptStudentScoreItem)
  }
}
