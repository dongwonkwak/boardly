import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import { I18nextProvider } from 'react-i18next'
import i18n from 'i18next'
import Navbar from './Navbar'

// Mock translations
i18n.init({
  lng: 'ko',
  resources: {
    ko: {
      common: {
        'nav.logo': 'Boardly',
        'nav.login': '로그인',
        'nav.register': '회원가입',
      }
    }
  }
})

const renderWithProviders = (component: React.ReactElement) => {
  return render(
    <BrowserRouter>
      <I18nextProvider i18n={i18n}>
        {component}
      </I18nextProvider>
    </BrowserRouter>
  )
}

describe('Navbar Component', () => {
  it('should render logo with correct link', () => {
    renderWithProviders(<Navbar />)
    
    const logoLink = screen.getByRole('link', { name: 'Boardly' })
    expect(logoLink).toBeInTheDocument()
    expect(logoLink).toHaveAttribute('href', '/')
  })

  it('should render login button', () => {
    renderWithProviders(<Navbar />)
    
    const loginButton = screen.getByRole('button', { name: '로그인' })
    expect(loginButton).toBeInTheDocument()
  })

  it('should render register link', () => {
    renderWithProviders(<Navbar />)
    
    const registerLink = screen.getByRole('link', { name: '회원가입' })
    expect(registerLink).toBeInTheDocument()
    expect(registerLink).toHaveAttribute('href', '/register')
  })

  it('should call onLogin when login button is clicked', async () => {
    const user = userEvent.setup()
    const onLogin = vi.fn()
    
    renderWithProviders(<Navbar onLogin={onLogin} />)
    
    const loginButton = screen.getByRole('button', { name: '로그인' })
    await user.click(loginButton)
    
    expect(onLogin).toHaveBeenCalledTimes(1)
  })

  it('should not call onLogin when onLogin prop is not provided', async () => {
    const user = userEvent.setup()
    
    renderWithProviders(<Navbar />)
    
    const loginButton = screen.getByRole('button', { name: '로그인' })
    await user.click(loginButton)
    
    // Should not throw error even without onLogin prop
    expect(loginButton).toBeInTheDocument()
  })

  it('should have proper navigation structure', () => {
    renderWithProviders(<Navbar />)
    
    const nav = screen.getByRole('navigation')
    expect(nav).toBeInTheDocument()
    expect(nav).toHaveClass('border-b')
  })

  it('should display logo text correctly', () => {
    renderWithProviders(<Navbar />)
    
    const logoHeading = screen.getByRole('heading', { level: 1 })
    expect(logoHeading).toHaveTextContent('Boardly')
    expect(logoHeading).toHaveClass('text-2xl', 'font-bold', 'text-primary')
  })

  it('should have responsive layout classes', () => {
    renderWithProviders(<Navbar />)
    
    const container = screen.getByRole('navigation').firstChild
    expect(container).toHaveClass('container', 'mx-auto', 'px-4', 'h-16')
  })

  it('should apply hover effects on logo', () => {
    renderWithProviders(<Navbar />)
    
    const logoLink = screen.getByRole('link', { name: 'Boardly' })
    expect(logoLink).toHaveClass('hover:opacity-80', 'transition-opacity')
  })

  it('should have proper button variants', () => {
    renderWithProviders(<Navbar />)
    
    const loginButton = screen.getByRole('button', { name: '로그인' })
    const registerLink = screen.getByRole('link', { name: '회원가입' })
    
    // Login button should have ghost variant
    expect(loginButton.closest('button')).toHaveAttribute('data-slot', 'button')
    
    // Register should be a regular button inside a link
    expect(registerLink.querySelector('button')).toHaveAttribute('data-slot', 'button')
  })
}) 