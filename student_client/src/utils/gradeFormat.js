/**
 * 成绩与状态统一格式化工具。
 * 对接后端 score.status、grade_change_request.status，以及成绩、日期展示。
 */

/** score.status：UPLOADED 已上传 / PUBLISHED 已发布 / LOCKED 锁定 */
export const SCORE_STATUS = {
  UPLOADED: 'UPLOADED',
  PUBLISHED: 'PUBLISHED',
  LOCKED: 'LOCKED'
}

export const SCORE_STATUS_LABELS = {
  [SCORE_STATUS.UPLOADED]: '已上传',
  [SCORE_STATUS.PUBLISHED]: '已发布',
  [SCORE_STATUS.LOCKED]: '锁定'
}

/** grade_change_request.status */
export const REQUEST_STATUS = {
  PENDING: 'PENDING',
  DEAN_APPROVED: 'DEAN_APPROVED',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED'
}

export const REQUEST_STATUS_LABELS = {
  [REQUEST_STATUS.PENDING]: '待审',
  [REQUEST_STATUS.DEAN_APPROVED]: '院长已通过',
  [REQUEST_STATUS.APPROVED]: '已通过',
  [REQUEST_STATUS.REJECTED]: '已拒绝'
}

/**
 * 申请状态中文描述（用于审批页面提示）
 */
export const REQUEST_STATUS_DESCRIPTIONS = {
  [REQUEST_STATUS.PENDING]: '待审',
  [REQUEST_STATUS.DEAN_APPROVED]: '院长已通过',
  [REQUEST_STATUS.APPROVED]: '已通过',
  [REQUEST_STATUS.REJECTED]: '已拒绝'
}

/** grade_change_log.operation */
export const LOG_OPERATION_LABELS = {
  IMPORT: '导入',
  PUBLISH: '发布',
  CHANGE: '修改'
}

/**
 * 成绩保留两位小数显示（总分除外）
 * @param {number|string|null} v
 * @param {boolean} isTotal 是否为总分（总分显示为整数）
 * @returns {string}
 */
export function formatScore (v, isTotal = false) {
  if (v === null || v === undefined || v === '') return '—'
  const n = Number(v)
  if (Number.isNaN(n)) return '—'
  return isTotal ? Math.round(n).toString() : n.toFixed(2)
}

/**
 * 总分显示为整数
 * @param {number|string|null} v
 * @returns {string}
 */
export function formatTotalScore (v) {
  return formatScore(v, true)
}

/**
 * 日期时间统一格式 YYYY-MM-DD HH:mm
 * @param {string|Date|null} d
 * @returns {string}
 */
export function formatDateTime (d) {
  if (!d) return '—'
  const date = typeof d === 'string' ? new Date(d) : d
  if (Number.isNaN(date.getTime())) return '—'
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${min}`
}

/**
 * 成绩状态文案
 * @param {string} s
 * @returns {string}
 */
export function scoreStatusLabel (s) {
  return SCORE_STATUS_LABELS[s] || s || '—'
}

/**
 * 申请状态文案
 * @param {string} s
 * @returns {string}
 */
export function requestStatusLabel (s) {
  return REQUEST_STATUS_LABELS[s] || s || '—'
}
