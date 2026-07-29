import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { describe, expect, it } from 'vitest'
import { SessionProvider } from './SessionContext'
import { RoleRoute } from './RoleRoute'

describe('role route', () => {
  it('renders content when the current profile has an allowed role', () => {
    render(
      <SessionProvider profile={{ username: 'admin', roles: ['ADMIN'] }}>
        <MemoryRouter initialEntries={['/admin']}>
          <Routes>
            <Route element={<RoleRoute roles={['ADMIN']} />}>
              <Route path="/admin" element={<h1>Admin content</h1>} />
            </Route>
          </Routes>
        </MemoryRouter>
      </SessionProvider>,
    )

    expect(screen.getByRole('heading', { name: 'Admin content' })).toBeInTheDocument()
  })

  it('redirects authenticated users without an allowed role', () => {
    render(
      <SessionProvider profile={{ username: 'staff', roles: ['DORM_STAFF'] }}>
        <MemoryRouter initialEntries={['/admin']}>
          <Routes>
            <Route path="/" element={<h1>Dashboard</h1>} />
            <Route element={<RoleRoute roles={['ADMIN']} />}>
              <Route path="/admin" element={<h1>Admin content</h1>} />
            </Route>
          </Routes>
        </MemoryRouter>
      </SessionProvider>,
    )

    expect(screen.getByRole('heading', { name: 'Dashboard' })).toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: 'Admin content' })).not.toBeInTheDocument()
  })
})
