import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import App from './App'
import { SessionProvider } from './auth/SessionContext'

function renderApp(roles: string[], route = '/') {
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: false }))
  return render(
    <SessionProvider profile={{ username: 'operator', roles }}>
      <MemoryRouter initialEntries={[route]}>
        <App />
      </MemoryRouter>
    </SessionProvider>,
  )
}

describe('role-aware dormitory workspace', () => {
  it('shows administration navigation to an administrator', () => {
    renderApp(['ADMIN'])

    expect(screen.getByRole('link', { name: 'ผู้ใช้ระบบ' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'สิทธิ์การใช้งาน' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'ประวัติการใช้งาน' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'นำเข้าข้อมูล' })).toBeInTheDocument()
  })

  it('does not render administration navigation for dormitory staff', () => {
    renderApp(['DORM_STAFF'])

    expect(screen.queryByRole('link', { name: 'ผู้ใช้ระบบ' })).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'นำเข้าข้อมูล' })).not.toBeInTheDocument()
  })

  it('redirects a non-admin away from a direct administration URL', async () => {
    renderApp(['DORM_STAFF'], '/admin/users')

    expect(await screen.findByRole('heading', { name: 'ภาพรวมหอพัก' })).toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: 'จัดการผู้ใช้ระบบ' })).not.toBeInTheDocument()
  })
})
