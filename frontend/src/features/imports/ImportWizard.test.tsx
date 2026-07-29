import { fireEvent, render, screen } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { ImportWizard } from './ImportWizard'

const validPreview = { token: 'token-1', sha256: 'a'.repeat(64), totalRows: 2, validRows: 2, invalidRows: 0, errors: [], expiresAt: '2026-07-29T14:00:00Z' }
const invalidPreview = { ...validPreview, token: 'token-2', validRows: 1, invalidRows: 1, errors: [{ rowNumber: 3, field: 'code', rejectedValue: '', code: 'REQUIRED', message: 'code is required' }] }

describe('master data import wizard', () => {
  beforeEach(() => {
    vi.unstubAllGlobals()
    vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => undefined)
  })
  afterEach(() => vi.restoreAllMocks())

  it('previews and confirms a valid xlsx exactly once', async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => ({ headerName: 'X-XSRF-TOKEN', token: 'csrf-1' }) })
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => validPreview })
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => ({ headerName: 'X-XSRF-TOKEN', token: 'csrf-2' }) })
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => ({ token: 'token-1', importedRows: 2 }) })
    vi.stubGlobal('fetch', fetchMock)
    render(<ImportWizard />)
    const file = new File(['xlsx'], 'master-data.xlsx', { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    fireEvent.change(screen.getByLabelText('ไฟล์ Excel'), { target: { files: [file] } })
    fireEvent.click(screen.getByRole('button', { name: 'ตรวจสอบไฟล์' }))

    expect(await screen.findByLabelText('ผ่าน 2 แถว')).toBeInTheDocument()
    expect(screen.getByText('a'.repeat(64))).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'ยืนยันนำเข้าข้อมูล' }))
    expect(await screen.findByRole('status')).toHaveTextContent('นำเข้าสำเร็จ 2 แถว')
    expect(screen.queryByRole('button', { name: 'ยืนยันนำเข้าข้อมูล' })).not.toBeInTheDocument()
  })

  it('shows row errors, offers an error workbook, and hides confirm', async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => ({ headerName: 'X-XSRF-TOKEN', token: 'csrf' }) })
      .mockResolvedValueOnce({ ok: true, status: 200, json: async () => invalidPreview })
      .mockResolvedValueOnce({ ok: true, status: 200, blob: async () => new Blob(['error workbook']) })
    vi.stubGlobal('fetch', fetchMock)
    vi.stubGlobal('URL', { createObjectURL: vi.fn(() => 'blob:error'), revokeObjectURL: vi.fn() })
    render(<ImportWizard />)
    fireEvent.change(screen.getByLabelText('ไฟล์ Excel'), { target: { files: [new File(['xlsx'], 'bad.xlsx', { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })] } })
    fireEvent.click(screen.getByRole('button', { name: 'ตรวจสอบไฟล์' }))

    expect(await screen.findByLabelText('ผิดพลาด 1 แถว')).toBeInTheDocument()
    expect(screen.getByText('code is required')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'ยืนยันนำเข้าข้อมูล' })).not.toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'ดาวน์โหลดรายการข้อผิดพลาด' }))
    expect(fetchMock).toHaveBeenLastCalledWith('/api/v1/imports/token-2/errors.xlsx', { credentials: 'include' })
  })
})
