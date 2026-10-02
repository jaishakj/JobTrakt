import { useEffect, useState, type FormEvent } from 'react'
import { request, jsonBody } from '../api/client'
import { useSession } from '../lib/session'
import { ErrorState, PageTitle } from '../components/Feedback'

export function SettingsPage() {
  const { user, setUser } = useSession()
  const [name, setName] = useState(user?.name ?? '')
  const [theme, setTheme] = useState(localStorage.getItem('jobtrakt.theme') ?? 'light')
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)
  useEffect(() => { document.documentElement.dataset.theme = theme; localStorage.setItem('jobtrakt.theme', theme) }, [theme])
  useEffect(() => { setName(user?.name ?? '') }, [user?.name])

  async function saveProfile(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setSaving(true); setMessage(''); setError('')
    try { const updated = await request<typeof user>('/auth/me', { method: 'PATCH', body: jsonBody({ name }) }); if (updated) setUser(updated); setMessage('Your profile is up to date.') }
    catch (cause) { setError(cause instanceof Error ? cause.message : 'Could not update your profile.') }
    finally { setSaving(false) }
  }
  async function savePassword(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setSaving(true); setMessage(''); setError('')
    try { await request<void>('/auth/me/password', { method: 'PATCH', body: jsonBody({ currentPassword, newPassword }) }); setCurrentPassword(''); setNewPassword(''); setMessage('Your password has been changed.') }
    catch (cause) { setError(cause instanceof Error ? cause.message : 'Could not change your password.') }
    finally { setSaving(false) }
  }
  return <div className="page-wrap settings-page">
    <PageTitle eyebrow="MAKE IT YOURS" title="Settings" detail="A few small choices for your search desk." />
    {error && <ErrorState message={error} />}{message && <p className="success-message">{message}</p>}
    <section className="surface-panel settings-section"><div className="settings-section-head"><span className="section-number">01</span><div><h2>Your profile</h2><p>How your personal workspace is addressed.</p></div></div><form className="settings-form" onSubmit={saveProfile}><label>Name<input required maxLength={100} value={name} onChange={event => setName(event.target.value)} /></label><label>Email address<input value={user?.email ?? ''} disabled /><small>Email is tied to your sign-in and can’t be edited here.</small></label><button className="button primary-button" disabled={saving}>Save profile ↗</button></form></section>
    <section className="surface-panel settings-section"><div className="settings-section-head"><span className="section-number">02</span><div><h2>Sign-in security</h2><p>Choose a password that’s yours alone.</p></div></div><form className="settings-form" onSubmit={savePassword}><label>Current password<input type="password" required autoComplete="current-password" value={currentPassword} onChange={event => setCurrentPassword(event.target.value)} /></label><label>New password<input type="password" required minLength={10} maxLength={72} autoComplete="new-password" value={newPassword} onChange={event => setNewPassword(event.target.value)} /><small>At least 10 characters.</small></label><button className="button quiet-button" disabled={saving}>Update password</button></form></section>
    <section className="surface-panel settings-section theme-section"><div className="settings-section-head"><span className="section-number">03</span><div><h2>Appearance</h2><p>Keep the palette warm, or settle into evening.</p></div></div><div className="theme-choice"><button onClick={() => setTheme('light')} className={theme === 'light' ? 'theme-selected' : ''}><span className="theme-swatch light-swatch" />Light<small>Cream & forest</small></button><button onClick={() => setTheme('dark')} className={theme === 'dark' ? 'theme-selected' : ''}><span className="theme-swatch dark-swatch" />Dark<small>Deep green & oat</small></button></div></section>
  </div>
}
