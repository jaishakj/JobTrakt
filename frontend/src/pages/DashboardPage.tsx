import { useResource } from '../lib/useResource'
import { request } from '../api/client'
import { AppLink } from '../lib/router'
import type { Dashboard } from '../types'
import { dateLabel } from '../lib/format'
import { EmptyState, ErrorState, Loading, StatusBadge } from '../components/Feedback'

export function DashboardPage() {
  const { data, loading, error, reload } = useResource(() => request<Dashboard>('/dashboard'))
  if (loading) return <div className="page-wrap"><Loading /></div>
  if (error || !data) return <div className="page-wrap"><ErrorState message={error ?? 'Dashboard is unavailable.'} onRetry={() => void reload()} /></div>
  const active = data.totalApplications - data.statusCounts.SAVED - data.statusCounts.WITHDRAWN
  return <div className="page-wrap dashboard-page">
    <section className="dashboard-hero">
      <div><p className="eyebrow">YOUR SEARCH, IN GOOD HANDS</p><h1>Keep every opportunity<br /><em>in motion.</em></h1><p className="hero-support">A clear view of where you’ve been, and what’s coming up next.</p></div>
      <div className="hero-stamp"><span>THIS WEEK</span><strong>{data.applicationsThisWeek}</strong><small>new {data.applicationsThisWeek === 1 ? 'application' : 'applications'}</small><div className="stamp-sun">✳</div></div>
      <div className="hero-caption">PERSONAL SEARCH DESK <span>✳</span> EST. TODAY</div>
    </section>
    <section className="metric-row" aria-label="Search metrics">
      <div className="metric-card metric-large"><span className="metric-label">IN PLAY</span><strong>{data.totalApplications}</strong><small>roles you’re tracking</small><span className="metric-doodle">↗</span></div>
      <div className="metric-card metric-green"><span className="metric-label">INTERVIEWS</span><strong>{data.statusCounts.INTERVIEW}</strong><small>conversations underway</small></div>
      <div className="metric-card metric-clay"><span className="metric-label">OFFERS</span><strong>{data.statusCounts.OFFER}</strong><small>possibilities opened</small></div>
      <div className="metric-card metric-round"><span className="metric-label">RESPONSE</span><strong>{data.responseRate}<i>%</i></strong><small>of active applications</small></div>
    </section>
    <div className="dashboard-columns">
      <section className="surface-panel recent-panel">
        <div className="section-heading"><div><p className="eyebrow">THE LATEST</p><h2>Recent applications</h2></div><AppLink to="/applications" className="text-link">All applications <span>↗</span></AppLink></div>
        {data.recentApplications.length === 0 ? <EmptyState title="A fresh page." children="Add your first application and JobTrakt will keep the details and next steps together." action={<AppLink to="/applications/new" className="button primary-button">Add an application ↗</AppLink>} /> : <div className="application-list compact-list">{data.recentApplications.map(application => <AppLink key={application.id} to={`/applications/${application.id}`} className="application-row"><span className="company-monogram">{application.company.slice(0, 1).toUpperCase()}</span><span className="application-main"><strong>{application.company}</strong><small>{application.position}{application.location ? ` · ${application.location}` : ''}</small></span><StatusBadge status={application.status} /><span className="row-date">{dateLabel(application.appliedDate)}</span><span className="row-arrow">↗</span></AppLink>)}</div>}
      </section>
      <div className="side-panels">
        <section className="interview-panel">
          <div className="section-heading"><div><p className="eyebrow">ON THE CALENDAR</p><h2>Coming up</h2></div><span className="calendar-mark">◷</span></div>
          {data.upcomingInterviews.length ? <div className="upcoming-list">{data.upcomingInterviews.map(item => <AppLink to={`/applications/${item.id}`} className="upcoming-item" key={item.id}><span className="upcoming-date">{dateLabel(item.interviewDate, { month: 'short', day: 'numeric' })}</span><span><strong>{item.company}</strong><small>{item.position} · {dateLabel(item.interviewDate, { hour: 'numeric', minute: '2-digit' })}</small></span></AppLink>)}</div> : <p className="quiet-empty">No interviews on the calendar yet. When you add one, its reminder will appear here.</p>}
        </section>
        <section className="automation-panel"><div className="automation-top"><span className="pulse-dot" /> AUTOMATION IS READY</div><strong>{data.jobQueue.PENDING ?? 0} queued</strong><p>Reminders are handled quietly in the background.</p><AppLink to="/jobs" className="text-link">View the queue ↗</AppLink></section>
      </div>
    </div>
    <div className="lower-strip"><span>{active} active applications</span><span>{data.applicationsThisMonth} added this month</span><span>{data.unreadNotifications} unread notes</span><AppLink to="/notifications">Open notifications ↗</AppLink></div>
  </div>
}
