import { Navigate, Route, Routes } from 'react-router-dom'
import { RoleRoute } from './auth/RoleRoute'
import { DashboardPage } from './features/dashboard/DashboardPage'
import { AppShell } from './layout/AppShell'
import { UsersPage } from './features/users/UsersPage'
import { RolesPage } from './features/roles/RolesPage'
import { AuditPage } from './features/audit/AuditPage'
import { MasterDataPage } from './features/master-data/MasterDataPage'

const adminPages = [
  { path: 'imports', title: 'นำเข้าข้อมูล', detail: 'ตรวจสอบไฟล์ Excel ก่อนยืนยันบันทึกข้อมูล' },
]

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
          {adminPages.map((page) => <Route key={page.path} path={`/admin/${page.path}`} element={<AdminPlaceholder {...page} />} />)}
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

function AdminPlaceholder({ title, detail }: { title: string; detail: string }) {
  return <div className="content"><section className="page-intro"><div><p className="eyebrow">ADMINISTRATION</p><h1>{title}</h1><p>{detail}</p></div></section><section className="panel empty-state"><strong>กำลังเตรียมพื้นที่ทำงาน</strong><p>หน้าจอนี้จะเชื่อมต่อข้อมูลจริงในขั้นถัดไป</p></section></div>
}
