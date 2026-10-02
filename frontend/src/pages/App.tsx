import { useEffect } from 'react'
import { RouterProvider, useRouter } from '../lib/router'
import { SessionProvider, useSession } from '../lib/session'
import { Shell } from '../components/Shell'
import { Loading } from '../components/Feedback'
import { AuthPage } from './AuthPage'
import { DashboardPage } from './DashboardPage'
import { ApplicationsPage } from './ApplicationsPage'
import { ApplicationFormPage } from './ApplicationFormPage'
import { ApplicationDetailPage } from './ApplicationDetailPage'
import { JobsPage } from './JobsPage'
import { NotificationsPage } from './NotificationsPage'
import { SettingsPage } from './SettingsPage'

function AppRoutes() {
  const { path, navigate } = useRouter()
  const { token, user, loading } = useSession()
  const pathname = path.split('?')[0]
  const publicRoute = pathname === '/login' || pathname === '/register' || pathname === '/'
  useEffect(() => {
    if (!loading && !token && !['/login', '/register'].includes(pathname)) navigate('/login', true)
    if (!loading && token && user && ['/login', '/register', '/'].includes(pathname)) navigate('/dashboard', true)
  }, [loading, token, user, pathname, navigate])
  if (loading) return <div className="session-loading"><Loading label="Opening your workspace…" /></div>
  if (!token || !user) {
    if (!publicRoute && pathname !== '/login' && pathname !== '/register') return <div className="session-loading"><Loading label="Taking you to sign in…" /></div>
    return <AuthPage register={pathname === '/register'} />
  }
  let page
  if (pathname === '/' || pathname === '/dashboard') page = <DashboardPage />
  else if (pathname === '/applications') page = <ApplicationsPage />
  else if (pathname === '/applications/new') page = <ApplicationFormPage />
  else if (pathname === '/jobs') page = <JobsPage />
  else if (pathname === '/notifications') page = <NotificationsPage />
  else if (pathname === '/settings') page = <SettingsPage />
  else if (/^\/applications\/\d+\/edit$/.test(pathname)) page = <ApplicationFormPage id={pathname.split('/')[2]} />
  else if (/^\/applications\/\d+$/.test(pathname)) page = <ApplicationDetailPage id={pathname.split('/')[2]} />
  else page = <div className="page-wrap"><div className="not-found"><p className="eyebrow">NOT IN THIS FILE</p><h1>That page wandered off.</h1><button className="button primary-button" onClick={() => navigate('/dashboard')}>Back to overview ↗</button></div></div>
  return <Shell>{page}</Shell>
}

export default function App() {
  return <RouterProvider><SessionProvider><AppRoutes /></SessionProvider></RouterProvider>
}
