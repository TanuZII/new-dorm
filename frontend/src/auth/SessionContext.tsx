import { createContext, type ReactNode, useContext } from 'react'

export type SessionProfile = {
  username: string
  roles: string[]
}

type SessionContextValue = {
  profile: SessionProfile
  hasAnyRole: (roles: string[]) => boolean
}

const SessionContext = createContext<SessionContextValue | null>(null)

export function SessionProvider({ profile, children }: { profile: SessionProfile; children: ReactNode }) {
  const value: SessionContextValue = {
    profile,
    hasAnyRole: (roles) => roles.length === 0 || roles.some((role) => profile.roles.includes(role)),
  }
  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>
}

export function useSession() {
  const value = useContext(SessionContext)
  if (!value) throw new Error('useSession must be used inside SessionProvider')
  return value
}
