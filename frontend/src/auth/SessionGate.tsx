import { type FormEvent, type ReactNode, useEffect, useState } from 'react'
import { Building2, KeyRound, UserRound } from 'lucide-react'
import { SessionProvider, type SessionProfile } from './SessionContext'

type SessionState = 'checking' | 'anonymous' | 'authenticated'

export function SessionGate({ children }: { children: ReactNode }) {
  const [state, setState] = useState<SessionState>('checking')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [profile, setProfile] = useState<SessionProfile | null>(null)

  useEffect(() => {
    fetch('/api/v1/auth/me', { credentials: 'include' })
      .then(async (response) => {
        if (!response.ok) {
          setState('anonymous')
          return
        }
        setProfile(await response.json() as SessionProfile)
        setState('authenticated')
      })
      .catch(() => setState('anonymous'))
  }, [])

  async function login(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setBusy(true)
    setError('')
    const form = new FormData(event.currentTarget)
    try {
      const csrfResponse = await fetch('/api/v1/auth/csrf', { credentials: 'include' })
      if (!csrfResponse.ok) throw new Error('ไม่สามารถเริ่มการเข้าสู่ระบบได้')
      const csrf = await csrfResponse.json() as { headerName: string; token: string }
      const response = await fetch('/api/v1/auth/login', {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json', [csrf.headerName]: csrf.token },
        body: JSON.stringify({ username: form.get('username'), password: form.get('password') }),
      })
      if (!response.ok) throw new Error('ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง')
      setProfile(await response.json() as SessionProfile)
      setState('authenticated')
    } catch (loginError) {
      setError(loginError instanceof Error ? loginError.message : 'เข้าสู่ระบบไม่สำเร็จ')
    } finally {
      setBusy(false)
    }
  }

  if (state === 'checking') {
    return <div className="session-loading"><Building2 size={30} /><span>กำลังตรวจสอบสิทธิ์…</span></div>
  }
  if (state === 'authenticated' && profile) {
    return <SessionProvider profile={profile}>{children}</SessionProvider>
  }

  return (
    <main className="login-shell">
      <section className="login-building" aria-hidden="true">
        <div className="login-building__copy"><span>มหาวิทยาลัยสวนดุสิต</span><strong>ทุกห้อง<br />ทุกสัญญา<br />ตรวจสอบได้</strong><p>ศูนย์กลางการดูแลผู้พักอาศัย อาคาร และการเงินหอพัก</p></div>
        <div className="login-building__floors">{[4, 3, 2, 1].map((floor) => <div key={floor}><b>0{floor}</b>{Array.from({ length: 6 }, (_, index) => <i key={index} className={index === 2 && floor === 3 ? 'is-lit' : ''} />)}</div>)}</div>
      </section>
      <section className="login-panel">
        <form className="login-form" onSubmit={login}>
          <div className="login-brand"><span><Building2 size={23} /></span><div><strong>บ้านสวนดุสิต</strong><small>DORM OPERATIONS</small></div></div>
          <div className="login-copy"><p className="eyebrow">ระบบสำหรับเจ้าหน้าที่และผู้พักอาศัย</p><h1>เข้าสู่ระบบหอพัก</h1><p>ใช้บัญชีที่ได้รับจากผู้ดูแลระบบ</p></div>
          <label className="login-field"><span>ชื่อผู้ใช้</span><div><UserRound size={18} /><input name="username" autoComplete="username" required /></div></label>
          <label className="login-field"><span>รหัสผ่าน</span><div><KeyRound size={18} /><input name="password" type="password" autoComplete="current-password" minLength={8} required /></div></label>
          {error && <p className="login-error" role="alert">{error}</p>}
          <button className="login-submit" disabled={busy}>{busy ? 'กำลังตรวจสอบ…' : 'เข้าสู่ระบบ'}</button>
          <small className="login-help">หากบัญชีถูกล็อก กรุณาติดต่อเจ้าหน้าที่ผู้ดูแลระบบ</small>
        </form>
      </section>
    </main>
  )
}
