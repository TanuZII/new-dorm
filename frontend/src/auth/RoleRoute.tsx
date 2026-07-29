import { Navigate, Outlet } from 'react-router-dom'
import { useSession } from './SessionContext'

export function RoleRoute({ roles }: { roles: string[] }) {
  const { hasAnyRole } = useSession()
  return hasAnyRole(roles) ? <Outlet /> : <Navigate to="/" replace />
}
