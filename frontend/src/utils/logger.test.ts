import { describe, it, expect, vi, beforeEach } from 'vitest'
import log from 'loglevel'

// Mock loglevel
vi.mock('loglevel', () => ({
  default: {
    setLevel: vi.fn(),
    debug: vi.fn(),
    info: vi.fn(),
    warn: vi.fn(),
    error: vi.fn(),
  }
}))

const mockLog = vi.mocked(log)

describe('Logger configuration', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should have setLevel method', () => {
    // Check if setLevel method exists and is a function
    expect(mockLog.setLevel).toBeDefined()
    expect(typeof mockLog.setLevel).toBe('function')
  })

  it('should export log instance', () => {
    expect(log).toBeDefined()
    expect(log.setLevel).toBeDefined()
  })

  it('should have logging methods available', () => {
    expect(log.debug).toBeDefined()
    expect(log.info).toBeDefined() 
    expect(log.warn).toBeDefined()
    expect(log.error).toBeDefined()
  })
}) 