import { type FormEvent, useCallback, useEffect, useState } from 'react'
import { Plus, RefreshCw, Search, UserRound, X } from 'lucide-react'
import { api } from '../../api/client'

type UserRole = 'ADMIN' | 'DORM_STAFF' | 'FINANCE' | 'APPROVER' | 'MAINTENANCE' | 'TENANT'
type User = { id: number; username: string; displayName: string; email: string | null; role: UserRole; active: boolean }
type UserPage = { content: User[]; number: number; size: number; totalElements: number; totalPages: number }
type Dialog = { kind: 'create' } | { kind: 'status' | 'password'; user: User } | null
const roles: UserRole[] = ['ADMIN', 'DORM_STAFF', 'FINANCE', 'APPROVER', 'MAINTENANCE', 'TENANT']

export function UsersPage() {
  const [data, setData] = useState<UserPage | null>(null)
  const [query, setQuery] = useState('')
  const [draftQuery, setDraftQuery] = useState('')
  const [page, setPage] = useState(0)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [dialog, setDialog] = useState<Dialog>(null)

  const load = useCallback(async () => {
    setLoading(true); setError('')
    try {
      const params = new URLSearchParams({ page: String(page), size: '20', sort: 'username,asc' })
      if (query.trim()) params.set('query', query.trim())
      setData(await api.get<UserPage>(`/api/v1/users?${params}`))
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'โหลดรายชื่อผู้ใช้ไม่สำเร็จ')
    } finally { setLoading(false) }
  }, [page, query])

  useEffect(() => { void load() }, [load])

  function updateUser(updated: User) {
    setData((current) => current ? { ...current, content: current.content.map((user) => user.id === updated.id ? updated : user) } : current)
  }

  return <div className="content admin-page">
    <section className="page-intro"><div><p className="eyebrow">IDENTITY · USERS</p><h1>จัดการผู้ใช้ระบบ</h1><p>ค้นหา เพิ่ม และควบคุมสถานะบัญชีจากศูนย์กลาง</p></div><button className="primary-action" onClick={() => setDialog({ kind: 'create' })}><Plus size={17} /> เพิ่มผู้ใช้</button></section>
    <section className="operations-rail" aria-label="ตัวกรองผู้ใช้"><form className="operations-rail__search" onSubmit={(event) => { event.preventDefault(); setPage(0); setQuery(draftQuery) }}><label><Search size={17} /><input value={draftQuery} onChange={(event) => setDraftQuery(event.target.value)} aria-label="ค้นหาผู้ใช้" placeholder="ชื่อผู้ใช้ ชื่อ หรืออีเมล" /></label><button>ค้นหา</button></form><span>{data?.totalElements.toLocaleString('th-TH') || 0} บัญชี</span></section>
    {loading ? <Loading /> : error ? <ErrorState message={error} retry={load} /> : data?.content.length === 0 ? <Empty /> : <section className="data-panel"><div className="data-table" role="table" aria-label="รายชื่อผู้ใช้"><div className="data-row data-row--header" role="row"><span>ผู้ใช้</span><span>บทบาท</span><span>สถานะ</span><span>การจัดการ</span></div>{data?.content.map((user) => <div className="data-row" role="row" key={user.id}><span data-label="ผู้ใช้"><b>{user.displayName}</b><small>{user.username}{user.email ? ` · ${user.email}` : ''}</small></span><span data-label="บทบาท"><code>{user.role}</code></span><span data-label="สถานะ"><i className={`status-chip ${user.active ? 'status-chip--active' : 'status-chip--inactive'}`}>{user.active ? 'ใช้งาน' : 'ปิดใช้งาน'}</i></span><span className="row-actions" data-label="การจัดการ"><button onClick={() => setDialog({ kind: 'password', user })}>ตั้งรหัสผ่าน</button><button onClick={() => setDialog({ kind: 'status', user })}>{user.active ? 'ปิดบัญชี' : 'เปิดบัญชี'}</button></span></div>)}</div><div className="pagination"><button disabled={page === 0} onClick={() => setPage((value) => value - 1)}>ก่อนหน้า</button><span>หน้า {page + 1} / {Math.max(data?.totalPages || 1, 1)}</span><button disabled={!data || page + 1 >= data.totalPages} onClick={() => setPage((value) => value + 1)}>ถัดไป</button></div></section>}
    {dialog?.kind === 'create' && <CreateUserDialog close={() => setDialog(null)} created={(user) => { setData((current) => current ? { ...current, totalElements: current.totalElements + 1, content: [user, ...current.content] } : current); setDialog(null) }} />}
    {dialog && dialog.kind !== 'create' && <UserActionDialog dialog={dialog} close={() => setDialog(null)} updated={updateUser} />}
  </div>
}

function CreateUserDialog({ close, created }: { close: () => void; created: (user: User) => void }) {
  const [error, setError] = useState(''); const [busy, setBusy] = useState(false)
  async function submit(event: FormEvent<HTMLFormElement>) { event.preventDefault(); setBusy(true); setError(''); const form = new FormData(event.currentTarget); try { created(await api.post<User>('/api/v1/users', { username: form.get('username'), displayName: form.get('displayName'), email: form.get('email') || null, password: form.get('password'), role: form.get('role') })) } catch (reason) { setError(reason instanceof Error ? reason.message : 'บันทึกผู้ใช้ไม่สำเร็จ') } finally { setBusy(false) } }
  return <div className="dialog-backdrop"><section className="admin-dialog" role="dialog" aria-modal="true" aria-labelledby="create-user-title"><button className="dialog-close" aria-label="ปิด" onClick={close}><X size={18} /></button><p className="eyebrow">NEW ACCOUNT</p><h2 id="create-user-title">เพิ่มผู้ใช้</h2><form onSubmit={submit} className="admin-form"><label>ชื่อผู้ใช้<input name="username" required /></label><label>ชื่อที่แสดง<input name="displayName" required /></label><label>อีเมล<input name="email" type="email" /></label><label>รหัสผ่านเริ่มต้น<input name="password" type="password" minLength={8} required /></label><label>บทบาท<select name="role" defaultValue="DORM_STAFF">{roles.map((role) => <option key={role}>{role}</option>)}</select></label>{error && <p role="alert" className="form-error">{error}</p>}<button className="primary-action" disabled={busy}>บันทึกผู้ใช้</button></form></section></div>
}

function UserActionDialog({ dialog, close, updated }: { dialog: Exclude<Dialog, null | { kind: 'create' }>; close: () => void; updated: (user: User) => void }) {
  const [error, setError] = useState(''); const [busy, setBusy] = useState(false)
  async function submit(event: FormEvent<HTMLFormElement>) { event.preventDefault(); setBusy(true); const form = new FormData(event.currentTarget); try { if (dialog.kind === 'status') { const result = await api.patch<User>(`/api/v1/users/${dialog.user.id}/status`, { active: !dialog.user.active, reason: form.get('reason') }); updated(result) } else { await api.post<void>(`/api/v1/users/${dialog.user.id}/reset-password`, { password: form.get('password'), reason: form.get('reason') }) } close() } catch (reason) { setError(reason instanceof Error ? reason.message : 'ทำรายการไม่สำเร็จ') } finally { setBusy(false) } }
  return <div className="dialog-backdrop"><section className="admin-dialog" role="dialog" aria-modal="true"><button className="dialog-close" aria-label="ปิด" onClick={close}><X size={18} /></button><h2>{dialog.kind === 'status' ? `${dialog.user.active ? 'ปิด' : 'เปิด'}บัญชี ${dialog.user.username}` : `ตั้งรหัสผ่าน ${dialog.user.username}`}</h2><form onSubmit={submit} className="admin-form">{dialog.kind === 'password' && <label>รหัสผ่านใหม่<input name="password" type="password" minLength={8} required /></label>}<label>เหตุผล<textarea name="reason" required /></label>{error && <p role="alert" className="form-error">{error}</p>}<button className="primary-action" disabled={busy}>ยืนยันรายการ</button></form></section></div>
}
function Loading() { return <section className="panel state-panel" role="status">กำลังโหลดรายชื่อผู้ใช้…</section> }
function ErrorState({ message, retry }: { message: string; retry: () => void }) { return <section className="panel state-panel" role="alert"><b>โหลดข้อมูลไม่สำเร็จ</b><p>{message}</p><button onClick={retry}><RefreshCw size={15} /> ลองอีกครั้ง</button></section> }
function Empty() { return <section className="panel state-panel"><UserRound size={25} /><b>ยังไม่พบผู้ใช้</b><p>ลองเปลี่ยนคำค้น หรือเพิ่มบัญชีใหม่</p></section> }
