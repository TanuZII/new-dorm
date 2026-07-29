import { Navigate, Route, Routes } from 'react-router-dom'
import { RoleRoute } from './auth/RoleRoute'
import { DashboardPage } from './features/dashboard/DashboardPage'
import { AppShell } from './layout/AppShell'
import { UsersPage } from './features/users/UsersPage'
import { RolesPage } from './features/roles/RolesPage'
import { AuditPage } from './features/audit/AuditPage'
import { MasterDataPage } from './features/master-data/MasterDataPage'
import { ImportWizard } from './features/imports/ImportWizard'

export default function App() {
  return (
    <Routes>
      <Route element={<AppShell />}>
        <Route index element={<DashboardPage />} />
        <Route element={<RoleRoute roles={['ADMIN']} />}>
          <Route path="/admin/users" element={<UsersPage />} />
          <Route path="/admin/roles" element={<RolesPage />} />
          <Route path="/admin/audit" element={<AuditPage />} />
          <Route path="/admin/master-data" element={<MasterDataPage />} />
          <Route path="/admin/imports" element={<ImportWizard />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
