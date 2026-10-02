import { useCallback, useEffect, useState } from 'react'

export function useResource<T>(load: () => Promise<T>, dependencies: readonly unknown[] = []) {
  const [data, setData] = useState<T | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const reload = useCallback(() => {
    setLoading(true); setError(null)
    return load().then(setData).catch((cause: unknown) => {
      setError(cause instanceof Error ? cause.message : 'Something went wrong.')
    }).finally(() => setLoading(false))
  // The caller supplies stable paths and filters as dependencies.
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, dependencies)
  useEffect(() => { void reload() }, [reload])
  return { data, loading, error, reload, setData }
}
