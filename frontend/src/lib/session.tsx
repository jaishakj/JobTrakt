import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from 'react'
import { request } from '../api/client'
import type { AuthResponse, User } from '../types'

type Session = { user: User | null; token: string | null; loading: boolean; signIn: (response: AuthResponse) => void; signOut: () => void; setUser: (user: User) => void }
const SessionContext = createContext<Session | null>(null)

export function SessionProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => sessionStorage.getItem('jobtrakt.token'))
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(Boolean(token))
  const signOut = useCallback(() => {
    sessionStorage.removeItem('jobtrakt.token'); setToken(null); setUser(null); setLoading(false)
  }, [])
  useEffect(() => {
    const unauthorized = () => signOut()
    window.addEventListener('jobtrakt:unauthorized', unauthorized)
    return () => window.removeEventListener('jobtrakt:unauthorized', unauthorized)
  }, [signOut])
  useEffect(() => {
    if (!token) { setUser(null); setLoading(false); return }
    setLoading(true)
    request<User>('/auth/me').then(setUser).catch(signOut).finally(() => setLoading(false))
  }, [token, signOut])
  const signIn = useCallback((response: AuthResponse) => {
    sessionStorage.setItem('jobtrakt.token', response.accessToken)
    setToken(response.accessToken); setUser(response.user)
  }, [])
  return <SessionContext.Provider value={{ user, token, loading, signIn, signOut, setUser }}>{children}</SessionContext.Provider>
}

export function useSession(): Session {
  const value = useContext(SessionContext)
  if (!value) throw new Error('SessionProvider is missing')
  return value
}
