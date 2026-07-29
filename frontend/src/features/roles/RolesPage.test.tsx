import { fireEvent, render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { RolesPage } from './RolesPage'

const roles = [
  { code: 'ADMIN', nameTh: 'ผู้ดูแลระบบ', description: 'ดูแลทุกส่วน', active: true, version: 0, permissions: ['USERS:READ', 'USERS:WRITE'] },
  { code: 'DORM_STAFF', nameTh: 'เจ้าหน้าที่หอพัก', description: 'ดูแลผู้เช่า', active: true, version: 0, permissions: ['USERS:READ'] },
]

describe('roles page', () => {
  beforeEach(() => vi.unstubAllGlobals())

  it('shows roles and the permissions assigned to the selected role', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, status: 200, json: async () => roles }))
    render(<RolesPage />)

    expect(await screen.findByRole('button', { name: /ผู้ดูแลระบบ/ })).toBeInTheDocument()
    expect(screen.getByLabelText('USERS:READ')).toBeChecked()
    expect(screen.getByLabelText('USERS:WRITE')).toBeChecked()
  })

  it('replaces permissions with a reason and csrf token', async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => roles })
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => ({ headerName: 'X-XSRF-TOKEN', token: 'csrf-2' }) })
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => ({ ...roles[0], permissions: ['USERS:READ'] }) })
    vi.stubGlobal('fetch', fetchMock)
    render(<RolesPage />)

    await screen.findByText('ดูแลทุกส่วน')
    fireEvent.click(screen.getByLabelText('USERS:WRITE'))
    fireEvent.change(screen.getByLabelText('เหตุผลการเปลี่ยนสิทธิ์'), { target: { value: 'ปรับตามหน้าที่ล่าสุด' } })
    fireEvent.click(screen.getByRole('button', { name: 'บันทึกสิทธิ์' }))

    expect(await screen.findByRole('status')).toHaveTextContent('บันทึกสิทธิ์แล้ว')
    expect(fetchMock).toHaveBeenLastCalledWith('/api/v1/roles/ADMIN/permissions', expect.objectContaining({ method: 'PUT' }))
  })
})
