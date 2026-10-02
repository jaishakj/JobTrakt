import { useResource } from '../lib/useResource'
import { request } from '../api/client'
import { AppLink, useRouter } from '../lib/router'
import type { ApplicationDetail } from '../types'
import { dateLabel, employmentLabels, money, relativeTime } from '../lib/format'
import { ErrorState, Loading, StatusBadge } from '../components/Feedback'

export function ApplicationDetailPage({ id }: { id: string }) {
  const { navigate } = useRouter()
  const { data, loading, error, reload } = useResource(() => request<ApplicationDetail>(`/applications/${id}`), [id])
  async function remove() {
    if (!window.confirm('Remove this application from your search?')) return
    try { await request<void>(`/applications/${id}`, { method: 'DELETE' }); navigate('/applications') }
    catch (cause) { window.alert(cause instanceof Error ? cause.message : 'Could not remove this application.') }
  }
  if (loading) return <div className="page-wrap"><Loading /></div>
  if (error || !data) return <div className="page-wrap"><ErrorState message={error ?? 'Application not found.'} onRetry={() => void reload()} /></div>
  const app = data.application
  return <div className="page-wrap detail-page">
    <AppLink to="/applications" className="back-link">← All applications</AppLink>
    <section className="detail-hero"><span className="detail-monogram">{app.company.slice(0, 1).toUpperCase()}</span><div className="detail-heading"><p className="eyebrow">APPLICATION FILE · JT-{String(app.id).padStart(4, '0')}</p><h1>{app.company}</h1><p>{app.position}{app.location ? ` · ${app.location}` : ''}</p><StatusBadge status={app.status} /></div><div className="detail-actions"><AppLink to={`/applications/${id}/edit`} className="button quiet-button">Edit details</AppLink><button className="button danger-button" onClick={() => void remove()}>Remove</button></div></section>
    <div className="detail-grid"><section className="surface-panel detail-facts"><div className="section-heading"><div><p className="eyebrow">THE DETAILS</p><h2>Role notes</h2></div></div><dl className="facts-grid"><div><dt>Location</dt><dd>{app.location || 'Not specified'}</dd></div><div><dt>Employment</dt><dd>{employmentLabels[app.employmentType]}</dd></div><div><dt>Compensation</dt><dd>{money(app.salary) ?? 'Not listed'}</dd></div><div><dt>Applied</dt><dd>{dateLabel(app.appliedDate)}</dd></div><div><dt>Interview</dt><dd>{dateLabel(app.interviewDate, { dateStyle: 'medium', timeStyle: 'short' })}</dd></div><div><dt>Follow up</dt><dd>{dateLabel(app.followUpDate, { dateStyle: 'medium', timeStyle: 'short' })}</dd></div></dl>{app.jobUrl && <a className="text-link posting-link" href={app.jobUrl} target="_blank" rel="noreferrer">Open original posting ↗</a>}{app.notes && <div className="notes-block"><p className="eyebrow">YOUR NOTES</p><p>{app.notes}</p></div>}</section>
      <section className="surface-panel timeline-panel"><div className="section-heading"><div><p className="eyebrow">THE PAPER TRAIL</p><h2>Timeline</h2></div></div>{data.timeline.length ? <div className="timeline">{[...data.timeline].reverse().map((event, index) => <div className="timeline-item" key={event.id}><span className={`timeline-dot ${index === 0 ? 'current' : ''}`} /><div><strong>{event.action.replaceAll('_', ' ').toLowerCase()}</strong><small>{event.details}</small><time>{relativeTime(event.createdAt)}</time></div></div>)}</div> : <p className="quiet-empty">Updates will gather here as this opportunity moves along.</p>}</section>
    </div>
    <section className="surface-panel related-panel"><div className="section-heading"><div><p className="eyebrow">QUIETLY IN MOTION</p><h2>Automation & notes</h2></div><AppLink to="/jobs" className="text-link">Open queue ↗</AppLink></div><div className="related-columns"><div><h3>Reminder jobs <span>{data.jobs.length}</span></h3>{data.jobs.length ? data.jobs.map(job => <div className="mini-record" key={job.id}><span className="mini-dot" /><div><strong>{job.type.replaceAll('_', ' ').toLowerCase()}</strong><small>{job.status} · {job.attempts}/{job.maxAttempts} attempts</small></div><time>{relativeTime(job.scheduledAt)}</time></div>) : <p className="quiet-empty">No reminder jobs yet.</p>}</div><div><h3>Notifications <span>{data.notifications.length}</span></h3>{data.notifications.length ? data.notifications.map(note => <div className="mini-record" key={note.id}><span className="mini-dot clay-dot" /><div><strong>{note.title}</strong><small>{note.message}</small></div><time>{relativeTime(note.createdAt)}</time></div>) : <p className="quiet-empty">No notes for this role yet.</p>}</div></div></section>
  </div>
}
