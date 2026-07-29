import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { MasterDataPage } from './MasterDataPage'

const feePage = { content: [{ id: 1, type: 'FEE_TYPE', code: 'WATER', nameTh: 'ค่าน้ำ', nameEn: 'Water', parentId: null, effectiveFrom: '2026-01-01', effectiveTo: null, active: true, deactivationReason: null, version: 0 }], number: 0, totalElements: 1, totalPages: 1, size: 20 }

describe('master data page', () => {
  it('switches master-data types and shows effective dates', async () => {
    const fetchMock = vi.fn().mockResolvedValue({ ok: true, status: 200, json: async () => feePage })
    vi.stubGlobal('fetch', fetchMock)
    render(<MasterDataPage />)
    expect(await screen.findByText('WATER')).toBeInTheDocument()
    expect(screen.getByText('1 ม.ค. 2569')).toBeInTheDocument()

    fireEvent.change(screen.getByLabelText('ประเภทข้อมูล'), { target: { value: 'COUNTRY' } })
    expect(await screen.findByText('WATER')).toBeInTheDocument()
    expect(fetchMock).toHaveBeenLastCalledWith(expect.stringContaining('/master-data/COUNTRY'), expect.anything())
  })

  it('deactivates an item only after receiving a reason', async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => feePage })
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => ({ headerName: 'X-XSRF-TOKEN', token: 'csrf' }) })
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => ({ ...feePage.content[0], active: false, deactivationReason: 'ยกเลิกรายการเดิม', version: 1 }) })
    vi.stubGlobal('fetch', fetchMock)
    render(<MasterDataPage />)
    await screen.findByText('WATER')
    fireEvent.click(screen.getByRole('button', { name: 'ปิดใช้งาน WATER' }))
    fireEvent.change(screen.getByLabelText('เหตุผล'), { target: { value: 'ยกเลิกรายการเดิม' } })
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยันเปลี่ยนสถานะ' }))
    expect(await screen.findByText('ปิดใช้งาน')).toBeInTheDocument()
  })

  it('explains how to recover from an optimistic locking conflict', async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => feePage })
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => ({ headerName: 'X-XSRF-TOKEN', token: 'csrf' }) })
      .mockResolvedValueOnce({ ok: false, status: 409, json: async () => ({ code: 'CONCURRENT_MODIFICATION', message: 'changed' }) })
    vi.stubGlobal('fetch', fetchMock)
    render(<MasterDataPage />)
    await screen.findByText('WATER')
    fireEvent.click(screen.getByRole('button', { name: 'แก้ไข' }))
    fireEvent.click(screen.getByRole('button', { name: 'บันทึกรายการ' }))
    expect(await screen.findByRole('alert')).toHaveTextContent('ข้อมูลถูกแก้ไขโดยผู้ใช้อื่น')
  })
})
