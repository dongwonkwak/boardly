import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook } from '@testing-library/react'
import { useOAuth } from './useAuth'

// Mock react-oidc-context
const mockAuth = {
  signinRedirect: vi.fn(),
  removeUser: vi.fn(),
  isAuthenticated: false,
  user: null,
  isLoading: false,
  events: {},
}

vi.mock('react-oidc-context', () => ({
  useAuth: () => mockAuth,
}))

// Mock logger
vi.mock('@/utils/logger', () => ({
  default: {
    error: vi.fn(),
  },
}))

describe('useOAuth Hook', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should return auth state and functions', () => {
    const { result } = renderHook(() => useOAuth())
    
    expect(result.current).toHaveProperty('login')
    expect(result.current).toHaveProperty('logout')
    expect(result.current).toHaveProperty('isAuthenticated')
    expect(result.current).toHaveProperty('user')
    expect(result.current).toHaveProperty('isLoading')
    expect(result.current).toHaveProperty('auth')
    expect(result.current).toHaveProperty('events')
  })

  it('should return correct initial state', () => {
    const { result } = renderHook(() => useOAuth())
    
    expect(result.current.isAuthenticated).toBe(false)
    expect(result.current.user).toBe(null)
    expect(result.current.isLoading).toBe(false)
  })

  it('should call signinRedirect when login is called', async () => {
    const { result } = renderHook(() => useOAuth())
    
    await result.current.login()
    
    expect(mockAuth.signinRedirect).toHaveBeenCalledTimes(1)
  })

  it('should call removeUser when logout is called', async () => {
    const { result } = renderHook(() => useOAuth())
    
    await result.current.logout()
    
    expect(mockAuth.removeUser).toHaveBeenCalledTimes(1)
  })

  it('should handle login error', async () => {
    const error = new Error('Login failed')
    mockAuth.signinRedirect.mockRejectedValueOnce(error)
    
    const { result } = renderHook(() => useOAuth())
    
    await result.current.login()
    
    // Should not throw error
    expect(mockAuth.signinRedirect).toHaveBeenCalledTimes(1)
  })

  it('should handle logout error', async () => {
    const error = new Error('Logout failed')
    mockAuth.removeUser.mockRejectedValueOnce(error)
    
    const { result } = renderHook(() => useOAuth())
    
    await result.current.logout()
    
    // Should not throw error
    expect(mockAuth.removeUser).toHaveBeenCalledTimes(1)
  })

  it('should reflect authentication state changes', () => {
    // Simulate authenticated state
    mockAuth.isAuthenticated = true
    mockAuth.user = { sub: '123', name: 'Test User' } as any
    
    const { result } = renderHook(() => useOAuth())
    
    expect(result.current.isAuthenticated).toBe(true)
    expect(result.current.user).toEqual({ sub: '123', name: 'Test User' })
  })

  it('should reflect loading state', () => {
    mockAuth.isLoading = true
    
    const { result } = renderHook(() => useOAuth())
    
    expect(result.current.isLoading).toBe(true)
  })
}) 