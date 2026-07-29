import { useState, type ComponentType } from 'react'
import {
  Bell, BedDouble, Building2, ChevronRight, ClipboardList, Database,
  FileClock, FileSignature, LayoutDashboard, Menu, PackageOpen, ReceiptText,
  Search, Settings, ShieldCheck, Upload, UsersRound, Wrench, X,
} from 'lucide-react'
import { NavLink, Outlet } from 'react-router-dom'
import { useSession } from '../auth/SessionContext'

type NavItem = {
  label: string
  path?: string
  icon: ComponentType<{ size?: number; strokeWidth?: number }>
  roles?: string[]
}

const navigation: NavItem[] = [
  { label: 'ภาพรวม', path: '/', icon: LayoutDashboard },
  { label: 'ห้องพัก', icon: BedDouble },
  { label: 'ผู้เช่า', icon: UsersRound },
  { label: 'สัญญา', icon: FileSignature },
  { label: 'การเงิน', icon: ReceiptText },
  { label: 'แจ้งซ่อม', icon: Wrench },
  { label: 'สต็อก', icon: PackageOpen },
  { label: 'รายงาน', icon: ClipboardList },
]

const adminNavigation: NavItem[] = [
  { label: 'ผู้ใช้ระบบ', path: '/admin/users', icon: UsersRound, roles: ['ADMIN'] },
  { label: 'สิทธิ์การใช้งาน', path: '/admin/roles', icon: ShieldCheck, roles: ['ADMIN'] },
  { label: 'ประวัติการใช้งาน', path: '/admin/audit', icon: FileClock, roles: ['ADMIN'] },
  { label: 'ข้อมูลตั้งต้น', path: '/admin/master-data', icon: Database, roles: ['ADMIN'] },
  { label: 'นำเข้าข้อมูล', path: '/admin/imports', icon: Upload, roles: ['ADMIN'] },
]

export function AppShell() {
  const [menuOpen, setMenuOpen] = useState(false)
  const { profile, hasAnyRole } = useSession()
  const visibleAdmin = adminNavigation.filter((item) => hasAnyRole(item.roles || []))

  return (
    <div className="app-shell">
      <aside className={`sidebar ${menuOpen ? 'sidebar--open' : ''}`}>
        <div className="brand">
          <span className="brand__mark"><Building2 size={22} /></span>
          <span><strong>บ้านสวนดุสิต</strong><small>DORM OPERATIONS</small></span>
        </div>
        <button className="sidebar__close" onClick={() => setMenuOpen(false)} aria-label="ปิดเมนู"><X size={20} /></button>
        <nav aria-label="เมนูหลัก" className="main-nav">
          {navigation.map((item) => <NavigationItem item={item} close={() => setMenuOpen(false)} key={item.label} />)}
          {visibleAdmin.length > 0 && <p className="nav-section">ดูแลระบบ</p>}
          {visibleAdmin.map((item) => <NavigationItem item={item} close={() => setMenuOpen(false)} key={item.label} />)}
        </nav>
        <div className="sidebar__footer">
          <span className="nav-link nav-link--disabled"><Settings size={19} /><span>ตั้งค่าระบบ</span></span>
          <div className="operator"><span className="operator__avatar">{profile.username.slice(0, 2).toUpperCase()}</span><span><strong>{profile.username}</strong><small>{profile.roles.join(', ')}</small></span></div>
        </div>
      </aside>

      {menuOpen && <button className="backdrop" onClick={() => setMenuOpen(false)} aria-label="ปิดเมนู" />}

      <main className="workspace">
        <header className="topbar">
          <button className="icon-button mobile-menu" onClick={() => setMenuOpen(true)} aria-label="เปิดเมนู"><Menu size={21} /></button>
          <label className="search"><Search size={18} /><input aria-label="ค้นหา" placeholder="ค้นหาห้อง ผู้เช่า หรือเลขที่สัญญา" /></label>
          <div className="topbar__date"><span>ศูนย์ปฏิบัติการหอพัก</span><strong>มหาวิทยาลัยสวนดุสิต</strong></div>
          <button className="icon-button notification-button" aria-label="การแจ้งเตือน"><Bell size={20} /><span>4</span></button>
        </header>
        <Outlet />
      </main>
    </div>
  )
}

function NavigationItem({ item, close }: { item: NavItem; close: () => void }) {
  const Icon = item.icon
  if (!item.path) return <span className="nav-link nav-link--disabled"><Icon size={19} strokeWidth={1.8} /><span>{item.label}</span></span>
  return (
    <NavLink to={item.path} end={item.path === '/'} onClick={close}
      className={({ isActive }) => `nav-link ${isActive ? 'nav-link--active' : ''}`}>
      <Icon size={19} strokeWidth={1.8} /><span>{item.label}</span><ChevronRight className="nav-link__chevron" size={14} />
    </NavLink>
  )
}
