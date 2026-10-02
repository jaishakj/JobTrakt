import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'

type RouterValue = { path: string; navigate: (path: string, replace?: boolean) => void }
const RouterContext = createContext<RouterValue | null>(null)

export function RouterProvider({ children }: { children: ReactNode }) {
  const [path, setPath] = useState(`${location.pathname}${location.search}`)
  useEffect(() => {
    const update = () => setPath(`${location.pathname}${location.search}`)
    window.addEventListener('popstate', update)
    return () => window.removeEventListener('popstate', update)
  }, [])
  const value = useMemo<RouterValue>(() => ({
    path,
    navigate(next, replace = false) {
      if (replace) history.replaceState({}, '', next)
      else history.pushState({}, '', next)
      setPath(`${location.pathname}${location.search}`)
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },
  }), [path])
  return <RouterContext.Provider value={value}>{children}</RouterContext.Provider>
}

export function useRouter(): RouterValue {
  const value = useContext(RouterContext)
  if (!value) throw new Error('RouterProvider is missing')
  return value
}

export function AppLink({ to, children, className, onClick }: { to: string; children: ReactNode; className?: string; onClick?: () => void }) {
  const { navigate } = useRouter()
  return <a href={to} className={className} onClick={(event) => {
    if (event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return
    event.preventDefault(); onClick?.(); navigate(to)
  }}>{children}</a>
}
