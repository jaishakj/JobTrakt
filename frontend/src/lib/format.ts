import type { ApplicationStatus, EmploymentType } from '../types'

export const statusLabels: Record<ApplicationStatus, string> = {
  SAVED: 'Saved', APPLIED: 'Applied', SCREENING: 'Screening', INTERVIEW: 'Interview', OFFER: 'Offer', REJECTED: 'Rejected', WITHDRAWN: 'Withdrawn',
}
export const employmentLabels: Record<EmploymentType, string> = {
  FULL_TIME: 'Full time', PART_TIME: 'Part time', CONTRACT: 'Contract', INTERNSHIP: 'Internship', FREELANCE: 'Freelance',
}
export function dateLabel(value?: string, options: Intl.DateTimeFormatOptions = { dateStyle: 'medium' }) {
  if (!value) return '—'
  const date = new Date(value)
  return Number.isNaN(date.valueOf()) ? value : new Intl.DateTimeFormat(undefined, options).format(date)
}
export function relativeTime(value: string) {
  const seconds = Math.round((new Date(value).getTime() - Date.now()) / 1000)
  const absolute = Math.abs(seconds)
  if (absolute < 60) return 'just now'
  if (absolute < 3600) return `${Math.round(absolute / 60)} min ${seconds < 0 ? 'ago' : 'from now'}`
  if (absolute < 86400) return `${Math.round(absolute / 3600)} hr ${seconds < 0 ? 'ago' : 'from now'}`
  const days = Math.round(absolute / 86400)
  return `${days} day${days === 1 ? '' : 's'} ${seconds < 0 ? 'ago' : 'from now'}`
}
export function money(value?: number) {
  if (value === undefined || value === null) return null
  return new Intl.NumberFormat(undefined, { style: 'currency', currency: 'USD', maximumFractionDigits: 0 }).format(value)
}
export function toLocalInput(value?: string) {
  if (!value) return ''
  const date = new Date(value)
  const local = new Date(date.getTime() - date.getTimezoneOffset() * 60000)
  return local.toISOString().slice(0, 16)
}
