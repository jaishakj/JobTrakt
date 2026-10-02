import { useState, type FormEvent } from 'react'
import { request, jsonBody } from '../api/client'
import { useRouter } from '../lib/router'
import { useSession } from '../lib/session'
import type { AuthResponse } from '../types'

export function AuthPage({ register }: { register: boolean }) {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const { signIn } = useSession()
  const { navigate } = useRouter()

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setBusy(true); setError('')
    try {
      const response = await request<AuthResponse>(register ? '/auth/register' : '/auth/login', {
        method: 'POST', body: jsonBody(register ? { name, email, password } : { email, password }),
      })
      signIn(response); navigate('/dashboard', true)
    } catch (cause) { setError(cause instanceof Error ? cause.message : 'Please try again.') }
    finally { setBusy(false) }
  }

  return <main className="auth-screen">
    <section className="auth-art">
      <a href="/" className="brand" onClick={e => { e.preventDefault(); navigate('/login') }}><span className="brand-mark">J</span>JobTrakt</a>
      <div className="auth-copy"><p className="eyebrow">A LITTLE MORE CLARITY</p><h1>Keep every opportunity<br /><em>in motion.</em></h1><p>One calm place for the roles, conversations, and next steps that make up your search.</p></div>
      <div className="auth-note"><span className="note-seal">JT</span><span>YOUR SEARCH, HELD TOGETHER<small>Applications · interviews · follow-ups</small></span></div>
      <div className="auth-sun" />
    </section>
    <section className="auth-form-wrap">
      <div className="auth-mobile-brand"><span className="brand-mark">J</span>JobTrakt</div>
      <div className="auth-form-card">
        <p className="eyebrow">{register ? 'BEGIN WITH THE BASICS' : 'WELCOME BACK'}</p>
        <h2>{register ? 'Make room for your next move.' : 'Good to have you back.'}</h2>
        <p className="form-intro">{register ? 'Your search deserves a place of its own.' : 'Pick up where your search left off.'}</p>
        <form onSubmit={submit} className="form-stack">
          {register && <label>Your name<input autoComplete="name" value={name} onChange={e => setName(e.target.value)} required maxLength={100} placeholder="Sam Rivera" /></label>}
          <label>Email address<input type="email" autoComplete="email" value={email} onChange={e => setEmail(e.target.value)} required maxLength={254} placeholder="you@example.com" /></label>
          <label>Password<input type="password" autoComplete={register ? 'new-password' : 'current-password'} value={password} onChange={e => setPassword(e.target.value)} required minLength={register ? 10 : 1} maxLength={72} placeholder={register ? 'At least 10 characters' : 'Your password'} /></label>
          {error && <p className="form-error" role="alert">{error}</p>}
          <button className="button primary-button full-button" disabled={busy}>{busy ? 'One moment…' : register ? 'Create your workspace' : 'Sign in'}<span>↗</span></button>
        </form>
        <p className="switch-auth">{register ? 'Already have a workspace?' : 'New to JobTrakt?'} <a href={register ? '/login' : '/register'} onClick={e => { e.preventDefault(); navigate(register ? '/login' : '/register') }}>{register ? 'Sign in' : 'Create an account'}</a></p>
      </div>
      <span className="auth-footnote">PRIVATE BY DESIGN · YOUR SEARCH IS YOURS</span>
    </section>
  </main>
}
