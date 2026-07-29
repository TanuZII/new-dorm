import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { AuditPage } from './AuditPage'

const response = { content: [{ id: 8, actor: 'admin', action: 'USER_CREATED', entityType: 'USER', entityId: '12', reason: null, ipAddress: '127.0.0.1', traceId: 'trace-8', details: { username: 'staff01' }, createdAt: '2026-07-29T10:00:00Z' }], number: 0, totalElements: 1, totalPages: 1, size: 20 }

describe('audit page', () => {
  it('renders audit records and applies actor filters', async () => {
    const fetchMock = vi.fn().mockResolvedValue({ ok: true, status: 200, json: async () => response })
    vi.stubGlobal('fetch', fetchMock)
    render(<AuditPage />)
    expect(await screen.findByText('USER_CREATED')).toBeInTheDocument()
    expect(screen.getByText('trace-8')).toBeInTheDocument()

    fireEvent.change(screen.getByLabelText('ผู้ดำเนินการ'), { target: { value: 'admin' } })
    fireEvent.click(screen.getByRole('button', { name: 'กรองข้อมูล' }))
    await waitFor(() => expect(fetchMock).toHaveBeenLastCalledWith(expect.stringContaining('actor=admin'), expect.anything()))
  })
})
