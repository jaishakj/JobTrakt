import { useSession } from '../lib/session'
import { AppLink, useRouter } from '../lib/router'

const navigation = [
  { to: '/dashboard', label: 'Overview', mark: '◒' },
  { to: '/applications', label: 'Applications', mark: '▤' },
  { to: '/jobs', label: 'Automation', mark: '↗' },
  { to: '/notifications', label: 'Notifications', mark: '◌' },
]

export function Shell({ children }: { children: React.ReactNode }) {
  const { user, signOut } = useSession()
  const { path, navigate } = useRouter()
  const activePath = path.split('?')[0]
  return <div className="app-shell">
    <aside className="sidebar">
      <AppLink to="/dashboard" className="brand"><span className="brand-mark">J</span><span>JobTrakt</span></AppLink>
      <div className="side-label">YOUR DESK</div>
      <nav className="side-nav">
        {navigation.map(item => <AppLink key={item.to} to={item.to} className={`nav-link ${activePath === item.to || (item.to === '/applications' && activePath.startsWith('/applications/')) ? 'selected' : ''}`}>
          <span className="nav-mark">{item.mark}</span>{item.label}
        </AppLink>)}
      </nav>
      <div className="sidebar-bottom">
        <AppLink to="/settings" className={`nav-link ${activePath === '/settings' ? 'selected' : ''}`}><span className="nav-mark">⚙</span>Settings</AppLink>
        <div className="profile-chip"><span className="avatar">{user?.name.slice(0, 1).toUpperCase() ?? 'J'}</span><span className="profile-name">{user?.name}<small>Personal workspace</small></span>
          <button className="icon-button signout" title="Sign out" onClick={() => { signOut(); navigate('/login') }}>↗</button>
        </div>
      </div>
    </aside>
    <main className="main-area">
      <header className="mobile-header"><AppLink to="/dashboard" className="brand"><span className="brand-mark">J</span>JobTrakt</AppLink><button className="button small-button" onClick={() => { signOut(); navigate('/login') }}>Sign out</button></header>
      {children}
      <footer className="mobile-nav">{navigation.map(item => <AppLink key={item.to} to={item.to} className={activePath === item.to ? 'mobile-active' : ''}><span>{item.mark}</span><small>{item.label === 'Applications' ? 'Roles' : item.label === 'Notifications' ? 'Notes' : item.label}</small></AppLink>)}</footer>
    </main>
  </div>
}
