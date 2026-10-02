export function Loading({ label = 'Gathering your details…' }: { label?: string }) {
  return <div className="loading"><span className="spinner" />{label}</div>
}
export function ErrorState({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return <div className="notice error-notice"><strong>That didn’t load.</strong><span>{message}</span>{onRetry && <button className="text-button" onClick={onRetry}>Try again</button>}</div>
}
export function EmptyState({ title, children, action }: { title: string; children: string; action?: React.ReactNode }) {
  return <div className="empty-state"><span className="empty-orbit">✳</span><h3>{title}</h3><p>{children}</p>{action}</div>
}
export function StatusBadge({ status }: { status: string }) {
  return <span className={`badge badge-${status.toLowerCase()}`}>{status.replaceAll('_', ' ')}</span>
}
export function PageTitle({ eyebrow, title, detail, action }: { eyebrow: string; title: string; detail?: string; action?: React.ReactNode }) {
  return <div className="page-title"><div><p className="eyebrow">{eyebrow}</p><h1>{title}</h1>{detail && <p className="page-detail">{detail}</p>}</div>{action}</div>
}
