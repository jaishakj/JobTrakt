import { useState } from 'react'
import { request } from '../api/client'
import { useResource } from '../lib/useResource'
import type { Dashboard, Job, Page } from '../types'
import { dateLabel, relativeTime } from '../lib/format'
import { ErrorState, Loading, PageTitle, StatusBadge } from '../components/Feedback'

const states = ['ALL', 'PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'DEAD']
export function JobsPage() {
  const [status, setStatus] = useState('ALL')
  const [notice, setNotice] = useState('')
  const path = `/jobs?page=0&size=60${status === 'ALL' ? '' : `&status=${status}`}`
  const { data, loading, error, reload } = useResource(() => request<Page<Job>>(path), [path])
  const { data: dashboard } = useResource(() => request<Dashboard>('/dashboard'))
  async function retry(job: Job) {
    setNotice('')
    try { await request<Job>(`/jobs/${job.id}/retry`, { method: 'POST' }); setNotice('This job is back in the queue.'); await reload() }
    catch (cause) { setNotice(cause instanceof Error ? cause.message : 'Could not retry the job.') }
  }
  const counts = states.slice(1).map(state => [state, dashboard?.jobQueue[state] ?? 0] as const)
  return <div className="page-wrap">
    <PageTitle eyebrow="BACKGROUND AUTOMATION" title="The quiet work" detail="A PostgreSQL-backed queue, keeping the next nudge in motion." />
    <div className="queue-stats">{counts.map(([state, count]) => <button key={state} className={`queue-stat ${status === state ? 'queue-stat-active' : ''}`} onClick={() => setStatus(status === state ? 'ALL' : state)}><span>{state}</span><strong>{count}</strong></button>)}</div>
    <section className="surface-panel jobs-panel"><div className="section-heading"><div><p className="eyebrow">THE QUEUE</p><h2>{status === 'ALL' ? 'Recent work' : `${status.toLowerCase()} work`}</h2></div><span className="live-label"><span className="pulse-dot" /> WORKER RUNNING</span></div>
      {notice && <p className="inline-notice">{notice}</p>}{loading ? <Loading label="Checking the queue…" /> : error ? <ErrorState message={error} onRetry={() => void reload()} /> : data?.content.length ? <div className="jobs-list">{data.content.map(job => <article key={job.id} className="job-row"><span className={`job-symbol job-${job.status.toLowerCase()}`}>{job.status === 'COMPLETED' ? '✓' : job.status === 'FAILED' || job.status === 'DEAD' ? '!' : '↻'}</span><div className="job-info"><strong>{job.type.replaceAll('_', ' ').toLowerCase()}</strong><p>{job.company ? `${job.company} · ${job.position}` : job.type === 'WEEKLY_REPORT' ? 'Your weekly search note' : 'JobTrakt system task'}</p>{job.errorMessage && <small className="job-error">{job.errorMessage}</small>}</div><div className="job-meta"><StatusBadge status={job.status} /><small>{job.attempts} / {job.maxAttempts} attempts</small></div><div className="job-meta job-time"><small>SCHEDULED</small><strong>{dateLabel(job.scheduledAt, { dateStyle: 'medium', timeStyle: 'short' })}</strong><small>{relativeTime(job.scheduledAt)}</small></div>{(job.status === 'FAILED' || job.status === 'DEAD') && <button className="button quiet-button retry-button" onClick={() => void retry(job)}>Retry ↗</button>}</article>)}</div> : <div className="quiet-empty padded-empty">Nothing in this part of the queue right now.</div>}
    </section>
    <p className="queue-explainer"><span>↻</span> Jobs are claimed with PostgreSQL row locks. Failed work is retried with a short backoff and moved to dead status after five attempts.</p>
  </div>
}
