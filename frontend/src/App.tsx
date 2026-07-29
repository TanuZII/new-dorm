import { useEffect, useState, type ComponentType } from 'react'
import {
  Bell,
  BedDouble,
  Building2,
  ChevronRight,
  CircleDollarSign,
  ClipboardList,
  FileSignature,
  LayoutDashboard,
  Menu,
  PackageOpen,
  ReceiptText,
  Search,
  Settings,
  UsersRound,
  Wrench,
  X,
} from 'lucide-react'

type DashboardSummary = {
  totalRooms: number
  availableBeds: number
  overdueInvoices: number
  openMaintenanceRequests: number
  outstandingAmount: number
}

type NavItem = {
  label: string
  icon: ComponentType<{ size?: number; strokeWidth?: number }>
}

const fallbackSummary: DashboardSummary = {
  totalRooms: 120,
  availableBeds: 18,
  overdueInvoices: 7,
  openMaintenanceRequests: 3,
  outstandingAmount: 24500,
}

const navItems: NavItem[] = [
  { label: 'ภาพรวม', icon: LayoutDashboard },
  { label: 'ห้องพัก', icon: BedDouble },
  { label: 'ผู้เช่า', icon: UsersRound },
  { label: 'สัญญา', icon: FileSignature },
  { label: 'การเงิน', icon: ReceiptText },
  { label: 'แจ้งซ่อม', icon: Wrench },
  { label: 'สต็อก', icon: PackageOpen },
  { label: 'รายงาน', icon: ClipboardList },
]

const floorRooms = [
  { floor: 'ชั้น 4', rooms: ['full', 'full', 'free', 'full', 'repair', 'free', 'full', 'full'] },
  { floor: 'ชั้น 3', rooms: ['full', 'free', 'free', 'full', 'full', 'full', 'full', 'full'] },
  { floor: 'ชั้น 2', rooms: ['full', 'full', 'full', 'full', 'free', 'full', 'repair', 'full'] },
  { floor: 'ชั้น 1', rooms: ['full', 'full', 'free', 'full', 'full', 'full', 'full', 'free'] },
]

const money = new Intl.NumberFormat('th-TH', {
  style: 'currency',
  currency: 'THB',
  maximumFractionDigits: 0,
})

export default function App() {
  const [summary, setSummary] = useState(fallbackSummary)
  const [menuOpen, setMenuOpen] = useState(false)

  useEffect(() => {
    const controller = new AbortController()
    fetch('/api/v1/dashboard', { credentials: 'include', signal: controller.signal })
      .then((response) => (response.ok ? response.json() : Promise.reject()))
      .then((data: DashboardSummary) => setSummary(data))
      .catch(() => undefined)
    return () => controller.abort()
  }, [])

  return (
    <div className="app-shell">
      <aside className={`sidebar ${menuOpen ? 'sidebar--open' : ''}`}>
        <div className="brand">
          <span className="brand__mark"><Building2 size={22} /></span>
          <span><strong>บ้านสวนดุสิต</strong><small>DORM OPERATIONS</small></span>
        </div>
        <button className="sidebar__close" onClick={() => setMenuOpen(false)} aria-label="ปิดเมนู">
          <X size={20} />
        </button>
        <nav aria-label="เมนูหลัก" className="main-nav">
          {navItems.map(({ label, icon: Icon }, index) => (
            <button className={index === 0 ? 'nav-link nav-link--active' : 'nav-link'} key={label}>
              <Icon size={19} strokeWidth={1.8} />
              <span>{label}</span>
              {index === 0 && <span className="nav-link__signal" />}
            </button>
          ))}
        </nav>
        <div className="sidebar__footer">
          <button className="nav-link"><Settings size={19} /><span>ตั้งค่าระบบ</span></button>
          <div className="operator"><span className="operator__avatar">กอ</span><span><strong>กัญญา อารีย์</strong><small>เจ้าหน้าที่หอพัก</small></span></div>
        </div>
      </aside>

      {menuOpen && <button className="backdrop" onClick={() => setMenuOpen(false)} aria-label="ปิดเมนู" />}

      <main className="workspace">
        <header className="topbar">
          <button className="icon-button mobile-menu" onClick={() => setMenuOpen(true)} aria-label="เปิดเมนู"><Menu size={21} /></button>
          <label className="search"><Search size={18} /><input aria-label="ค้นหา" placeholder="ค้นหาห้อง ผู้เช่า หรือเลขที่สัญญา" /></label>
          <div className="topbar__date"><span>วันพุธ</span><strong>29 ก.ค. 2569</strong></div>
          <button className="icon-button notification-button" aria-label="การแจ้งเตือน"><Bell size={20} /><span>4</span></button>
        </header>

        <div className="content">
          <section className="page-intro">
            <div><p className="eyebrow">อาคารปราโมทย์ 1–3 · ภาคเรียน 1/2569</p><h1>ภาพรวมหอพัก</h1><p>ติดตามห้อง ผู้เช่า และงานที่ต้องจัดการวันนี้</p></div>
            <button className="primary-action"><span>+</span> รับผู้เช่าเข้าพัก</button>
          </section>

          <section className="metrics" aria-label="สรุปสถานะหอพัก">
            <Metric label="ห้องทั้งหมด" value={summary.totalRooms.toLocaleString('th-TH')} note="3 อาคาร" icon={Building2} tone="navy" />
            <Metric label="เตียงว่าง" value={summary.availableBeds.toLocaleString('th-TH')} note="พร้อมรับผู้เช่า" icon={BedDouble} tone="mint" />
            <Metric label="ใบแจ้งหนี้ค้าง" value={summary.overdueInvoices.toLocaleString('th-TH')} note={money.format(summary.outstandingAmount)} icon={CircleDollarSign} tone="amber" />
            <Metric label="งานซ่อมที่เปิดอยู่" value={summary.openMaintenanceRequests.toLocaleString('th-TH')} note="1 งานเร่งด่วน" icon={Wrench} tone="coral" />
          </section>

          <section className="dashboard-grid">
            <article className="panel building-panel">
              <div className="panel__heading"><div><p className="eyebrow">อาคารปราโมทย์ 1</p><h2>สถานะห้องแต่ละชั้น</h2></div><button className="text-action">ดูผังทั้งหมด <ChevronRight size={16} /></button></div>
              <div className="legend"><span><i className="room-dot room-dot--free" />ว่าง</span><span><i className="room-dot room-dot--full" />เต็ม</span><span><i className="room-dot room-dot--repair" />ซ่อม</span></div>
              <div className="building-map">
                {floorRooms.map(({ floor, rooms }) => (
                  <div className="floor" key={floor}><strong>{floor}</strong><div className="floor__rooms">
                    {rooms.map((status, roomIndex) => <span className={`room-cell room-cell--${status}`} title={`${floor} ห้อง ${roomIndex + 1}`} key={roomIndex}>{roomIndex + 1}</span>)}
                  </div></div>
                ))}
              </div>
            </article>

            <article className="panel task-panel">
              <div className="panel__heading"><div><p className="eyebrow">คิวงานวันนี้</p><h2>สิ่งที่ต้องดำเนินการ</h2></div><span className="task-count">6</span></div>
              <div className="task-list">
                <Task icon={FileSignature} title="สัญญารอยืนยัน" detail="3 รายการ · ใกล้ครบกำหนด 1 รายการ" tone="blue" />
                <Task icon={ReceiptText} title="ตรวจหลักฐานชำระเงิน" detail="2 รายการ · ส่งมาเช้านี้" tone="amber" />
                <Task icon={Wrench} title="งานซ่อมเร่งด่วน" detail="ห้อง P2-304 · ระบบไฟฟ้า" tone="coral" />
              </div>
              <button className="task-panel__all">เปิดศูนย์งานทั้งหมด <ChevronRight size={16} /></button>
            </article>
          </section>
        </div>
      </main>
    </div>
  )
}

function Metric({ label, value, note, icon: Icon, tone }: { label: string; value: string; note: string; icon: NavItem['icon']; tone: string }) {
  return <article className="metric"><span className={`metric__icon metric__icon--${tone}`}><Icon size={21} /></span><div><p>{label}</p><strong>{value}</strong><small>{note}</small></div></article>
}

function Task({ icon: Icon, title, detail, tone }: { icon: NavItem['icon']; title: string; detail: string; tone: string }) {
  return <button className="task"><span className={`task__icon task__icon--${tone}`}><Icon size={18} /></span><span><strong>{title}</strong><small>{detail}</small></span><ChevronRight size={17} /></button>
}

