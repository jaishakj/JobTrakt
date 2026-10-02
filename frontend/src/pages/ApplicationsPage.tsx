import { useEffect, useState } from 'react'
import { request } from '../api/client'
import { useResource } from '../lib/useResource'
import { AppLink } from '../lib/router'
import type { Application, ApplicationStatus, Page } from '../types'
import { dateLabel, money, statusLabels } from '../lib/format'
import { EmptyState, ErrorState, Loading, PageTitle, StatusBadge } from '../components/Feedback'

const statuses: ApplicationStatus[] = ['SAVED', 'APPLIED', 'SCREENING', 'INTERVIEW', 'OFFER', 'REJECTED', 'WITHDRAWN']

export function ApplicationsPage() {
  const [search, setSearch] = useState('')
  const [term, setTerm] = useState('')
  const [status, setStatus] = useState('')
  const [page, setPage] = useState(0)
  const [sort, setSort] = useState('createdAt,desc')
  useEffect(() => { const timer = window.setTimeout(() => { setTerm(search); setPage(0) }, 250); return () => window.clearTimeout(timer) }, [search])
  const params = new URLSearchParams({ page: String(page), size: '12', sort })
  if (term) params.set('search', term)
  if (status) params.set('status', status)
  const url = `/applications?${params}`
  const { data, loading, error, reload } = useResource(() => request<Page<Application>>(url), [url])

  return <div className="page-wrap">
    <PageTitle eyebrow="THE WHOLE FIELD" title="Applications" detail="Every open door, kept in one place." action={<AppLink to="/applications/new" className="button primary-button">＋ New application</AppLink>} />
    <div className="filter-bar"><label className="search-box"><span>⌕</span><input value={search} onChange={event => setSearch(event.target.value)} placeholder="Search company, role, place…" aria-label="Search applications" /></label>
      <select aria-label="Filter by status" value={status} onChange={event => { setStatus(event.target.value); setPage(0) }}><option value="">All statuses</option>{statuses.map(value => <option key={value} value={value}>{statusLabels[value]}</option>)}</select>
      <select aria-label="Sort applications" value={sort} onChange={event => setSort(event.target.value)}><option value="createdAt,desc">Recently added</option><option value="appliedDate,desc">Applied date · newest</option><option value="appliedDate,asc">Applied date · oldest</option><option value="company,asc">Company · A to Z</option><option value="status,asc">Status</option></select>
      <span className="result-count">{data?.totalElements ?? 0} {data?.totalElements === 1 ? 'role' : 'roles'}</span>
    </div>
    {loading ? <Loading label="Finding your applications…" /> : error ? <ErrorState message={error} onRetry={() => void reload()} /> : !data?.content.length ? <EmptyState title={term || status ? 'No matches this time.' : 'A fresh page.'} children={term || status ? 'Try a different search or clear the status filter.' : 'Add your first application and make your search easier to follow.'} action={!term && !status ? <AppLink to="/applications/new" className="button primary-button">Add your first application ↗</AppLink> : undefined} /> : <>
      <div className="application-list full-list">{data.content.map(application => <AppLink key={application.id} to={`/applications/${application.id}`} className="application-row"><span className="company-monogram">{application.company.slice(0, 1).toUpperCase()}</span><span className="application-main"><strong>{application.company}</strong><small>{application.position}{application.location ? ` · ${application.location}` : ''}</small></span><StatusBadge status={application.status} /><span className="application-meta"><small>APPLIED</small>{dateLabel(application.appliedDate)}{application.interviewDate && <small className="row-interview">Interview · {dateLabel(application.interviewDate, { dateStyle: 'medium', timeStyle: 'short' })}</small>}</span><span className="application-meta salary-meta"><small>COMPENSATION</small>{money(application.salary) ?? 'Not listed'}</span><span className="row-arrow">↗</span></AppLink>)}</div>
      <div className="pagination"><span>Page {data.page + 1} of {Math.max(1, data.totalPages)}</span><div><button className="button quiet-button" disabled={page === 0} onClick={() => setPage(page - 1)}>← Previous</button><button className="button quiet-button" disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)}>Next →</button></div></div>
    </>}
  </div>
}
