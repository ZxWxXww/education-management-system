export function buildPageQuery(params = {}) {
  return {
    current: params.pageNum || 1,
    size: params.pageSize || 10,
    ...Object.fromEntries(
      Object.entries(params).filter(([key]) => key !== 'pageNum' && key !== 'pageSize')
    )
  }
}

export function adaptPageData(pageData = {}) {
  return {
    list: Array.isArray(pageData.records) ? pageData.records : [],
    total: Number(pageData.total || 0),
    current: Number(pageData.current || 1),
    size: Number(pageData.size || 10)
  }
}
