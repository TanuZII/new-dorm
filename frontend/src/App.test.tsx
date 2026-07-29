import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import App from './App'

describe('dormitory dashboard', () => {
  it('shows operational navigation and room status summary', () => {
    render(<App />)

    expect(screen.getByRole('heading', { name: 'ภาพรวมหอพัก' })).toBeInTheDocument()
    expect(screen.getByRole('navigation', { name: 'เมนูหลัก' })).toBeInTheDocument()
    expect(screen.getByText('ห้องพัก')).toBeInTheDocument()
    expect(screen.getByText('ผู้เช่า')).toBeInTheDocument()
    expect(screen.getByText('การเงิน')).toBeInTheDocument()
    expect(screen.getByText('เตียงว่าง')).toBeInTheDocument()
    expect(screen.getByText('สถานะห้องแต่ละชั้น')).toBeInTheDocument()
  })
})
