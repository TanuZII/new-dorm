import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { SessionGate } from './SessionGate'

describe('session gate', () => {
  it('shows a Thai login form when no session exists', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: false }))

    render(<SessionGate><div>private dashboard</div></SessionGate>)

    expect(await screen.findByRole('heading', { name: 'เข้าสู่ระบบหอพัก' })).toBeInTheDocument()
    expect(screen.getByLabelText('ชื่อผู้ใช้')).toBeInTheDocument()
    expect(screen.getByLabelText('รหัสผ่าน')).toBeInTheDocument()
    expect(screen.queryByText('private dashboard')).not.toBeInTheDocument()
  })

  it('submits credentials after obtaining a csrf token', async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce({ ok: false })
      .mockResolvedValueOnce({ ok: true, json: async () => ({ headerName: 'X-XSRF-TOKEN', token: 'csrf-1' }) })
      .mockResolvedValueOnce({ ok: true, json: async () => ({ username: 'admin', roles: ['ADMIN'] }) })
    vi.stubGlobal('fetch', fetchMock)
    render(<SessionGate><div>private dashboard</div></SessionGate>)

    fireEvent.change(await screen.findByLabelText('ชื่อผู้ใช้'), { target: { value: 'admin' } })
    fireEvent.change(screen.getByLabelText('รหัสผ่าน'), { target: { value: 'password123' } })
    fireEvent.click(screen.getByRole('button', { name: 'เข้าสู่ระบบ' }))

    expect(await screen.findByText('private dashboard')).toBeInTheDocument()
    expect(fetchMock).toHaveBeenLastCalledWith('/api/v1/auth/login', expect.objectContaining({
      method: 'POST',
      headers: expect.objectContaining({ 'X-XSRF-TOKEN': 'csrf-1' }),
    }))
  })
})
