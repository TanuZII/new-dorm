import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { UsersPage } from './UsersPage'

const page = {
  content: [{ id: 1, username: 'finance01', displayName: 'เจ้าหน้าที่การเงิน', email: 'finance@example.com', role: 'FINANCE', active: true }],
  number: 0, size: 20, totalElements: 1, totalPages: 1,
}

describe('users page', () => {
  beforeEach(() => vi.unstubAllGlobals())

  it('loads users and exposes their operational status', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, status: 200, json: async () => page }))
    render(<UsersPage />)

    expect(await screen.findByText('เจ้าหน้าที่การเงิน')).toBeInTheDocument()
    expect(screen.getByText('FINANCE')).toBeInTheDocument()
    expect(screen.getByText('ใช้งาน')).toBeInTheDocument()
  })

  it('creates a user through a csrf-backed request', async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => page })
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => ({ headerName: 'X-XSRF-TOKEN', token: 'csrf-1' }) })
      .mockResolvedValueOnce({ ok: true, status: 201, json: async () => ({ id: 2, username: 'staff02', displayName: 'เจ้าหน้าที่หอพัก', email: '', role: 'DORM_STAFF', active: true }) })
    vi.stubGlobal('fetch', fetchMock)
    render(<UsersPage />)

    await screen.findByText('เจ้าหน้าที่การเงิน')
    fireEvent.click(screen.getByRole('button', { name: 'เพิ่มผู้ใช้' }))
    fireEvent.change(screen.getByLabelText('ชื่อผู้ใช้'), { target: { value: 'staff02' } })
    fireEvent.change(screen.getByLabelText('ชื่อที่แสดง'), { target: { value: 'เจ้าหน้าที่หอพัก' } })
    fireEvent.change(screen.getByLabelText('รหัสผ่านเริ่มต้น'), { target: { value: 'Staff@1234' } })
    fireEvent.change(screen.getByLabelText('บทบาท'), { target: { value: 'DORM_STAFF' } })
    fireEvent.click(screen.getByRole('button', { name: 'บันทึกผู้ใช้' }))

    expect(await screen.findByText('staff02')).toBeInTheDocument()
    expect(fetchMock).toHaveBeenCalledWith('/api/v1/users', expect.objectContaining({
      method: 'POST', headers: expect.any(Headers), credentials: 'include',
    }))
  })

  it('shows a useful retry state when loading fails', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: false, status: 500, json: async () => ({ message: 'Database unavailable' }) }))
    render(<UsersPage />)
    expect(await screen.findByRole('alert')).toHaveTextContent('Database unavailable')
    expect(screen.getByRole('button', { name: 'ลองอีกครั้ง' })).toBeInTheDocument()
  })

  it('uses the responsive search layout contract', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, status: 200, json: async () => page }))
    render(<UsersPage />)

    await screen.findByText('FINANCE')
    expect(screen.getByRole('region', { name: 'ตัวกรองผู้ใช้' }).querySelector('form')).toHaveClass('operations-rail__search')
  })
})
