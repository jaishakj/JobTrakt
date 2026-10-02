import { request } from '../api/client'
import { useResource } from '../lib/useResource'
import { AppLink } from '../lib/router'
import type { NotificationPage as NotificationData } from '../types'
import { dateLabel, relativeTime } from '../lib/format'
import { EmptyState, ErrorState, Loading, PageTitle } from '../components/Feedback'

export function NotificationsPage() {
  const { data, loading, error, reload, setData } = useResource(() => request<NotificationData>('/notifications?page=0&size=50'))
  async function markOne(id: number) {
    await request(`/notifications/${id}/read`, { method: 'PATCH' })
    setData(current => current ? { ...current, unreadCount: Math.max(0, current.unreadCount - 1), notifications: { ...current.notifications, content: current.notifications.content.map(item => item.id === id ? { ...item, read: true } : item) } } : current)
  }
  async function markAll() {
    const result = await request<{ updated: number }>('/notifications/read-all', { method: 'PATCH' })
    setData(current => current ? { ...current, unreadCount: 0, notifications: { ...current.notifications, content: current.notifications.content.map(item => ({ ...item, read: true })) } } : current)
    return result.updated
  }
  return <div className="page-wrap">
    <PageTitle eyebrow="A NOTE WHEN IT MATTERS" title="Notifications" detail="Reminders and updates created by real work in your queue." action={Boolean(data?.unreadCount) ? <button className="button quiet-button" onClick={() => void markAll()}>Mark all read</button> : undefined} />
    {loading ? <Loading label="Gathering your notes…" /> : error ? <ErrorState message={error} onRetry={() => void reload()} /> : !data?.notifications.content.length ? <EmptyState title="All quiet here." children="When a reminder is processed, it will find its way here." /> : <div className="notification-list">{data.notifications.content.map(note => <article key={note.id} className={`notification-row ${note.read ? 'is-read' : 'is-unread'}`}><span className={`notification-symbol ${note.type === 'INTERVIEW' ? 'symbol-green' : note.type === 'FOLLOW_UP' ? 'symbol-clay' : 'symbol-olive'}`}>{note.type === 'INTERVIEW' ? '◷' : note.type === 'FOLLOW_UP' ? '↗' : '✳'}</span><div className="notification-copy"><div className="notification-kicker">{note.type.replaceAll('_', ' ')}{!note.read && <span className="unread-pill">NEW</span>}</div><h3>{note.title}</h3><p>{note.message}</p>{note.applicationId && <AppLink to={`/applications/${note.applicationId}`} className="text-link">Open application ↗</AppLink>}</div><div className="notification-time"><time>{relativeTime(note.createdAt)}</time><small>{dateLabel(note.createdAt, { dateStyle: 'medium', timeStyle: 'short' })}</small>{!note.read && <button className="text-button" onClick={() => void markOne(note.id)}>Mark read</button>}</div></article>)}</div>}
  </div>
}
