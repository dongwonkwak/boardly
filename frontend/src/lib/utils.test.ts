import { describe, it, expect } from 'vitest'
import { cn, getAvatarBackgroundColor, getInitials, getBoardThemeColor, boardGradientThemes } from './utils'

describe('cn utility function', () => {
  it('should merge class names correctly', () => {
    const result = cn('text-red-500', 'bg-blue-500')
    expect(result).toBe('text-red-500 bg-blue-500')
  })

  it('should handle conflicting tailwind classes', () => {
    const result = cn('text-red-500', 'text-blue-500')
    expect(result).toBe('text-blue-500')
  })

  it('should handle conditional classes', () => {
    const result = cn('base-class', true && 'conditional-class', false && 'hidden-class')
    expect(result).toBe('base-class conditional-class')
  })

  it('should handle undefined and null values', () => {
    const result = cn('base-class', undefined, null, 'another-class')
    expect(result).toBe('base-class another-class')
  })

  it('should handle empty input', () => {
    const result = cn()
    expect(result).toBe('')
  })

  it('should merge responsive classes correctly', () => {
    const result = cn('w-full', 'md:w-1/2', 'lg:w-1/3')
    expect(result).toBe('w-full md:w-1/2 lg:w-1/3')
  })
})

describe('getAvatarBackgroundColor utility function', () => {
  it('should return a valid color class for a given userId', () => {
    const result = getAvatarBackgroundColor('user123')
    expect(result).toMatch(/^bg-(blue|green|purple|pink|indigo|yellow|red|teal|orange|cyan)-500$/)
  })

  it('should return consistent color for the same userId', () => {
    const userId = 'user123'
    const result1 = getAvatarBackgroundColor(userId)
    const result2 = getAvatarBackgroundColor(userId)
    expect(result1).toBe(result2)
  })

  it('should return different colors for different userIds', () => {
    const result1 = getAvatarBackgroundColor('user123')
    const result2 = getAvatarBackgroundColor('user456')
    expect(result1).not.toBe(result2)
  })

  it('should handle empty userId', () => {
    const result = getAvatarBackgroundColor('')
    expect(result).toMatch(/^bg-(blue|green|purple|pink|indigo|yellow|red|teal|orange|cyan)-500$/)
  })
})

describe('getInitials utility function', () => {
  it('should return first character of firstName in uppercase', () => {
    const result = getInitials('John')
    expect(result).toBe('J')
  })

  it('should handle Korean names', () => {
    const result = getInitials('김철수')
    expect(result).toBe('김')
  })

  it('should handle empty firstName', () => {
    const result = getInitials('')
    expect(result).toBe('')
  })

  it('should handle undefined firstName', () => {
    const result = getInitials(undefined)
    expect(result).toBe('')
  })

  it('should handle single character firstName', () => {
    const result = getInitials('A')
    expect(result).toBe('A')
  })
})

describe('getBoardThemeColor utility function', () => {
  it('should return the specified board color when valid', () => {
    const result = getBoardThemeColor('board123', 'blue-purple')
    expect(result).toBe(boardGradientThemes['blue-purple'])
  })

  it('should return a theme color based on boardId when no color specified', () => {
    const result = getBoardThemeColor('board123')
    expect(Object.values(boardGradientThemes)).toContain(result)
  })

  it('should return consistent color for the same boardId', () => {
    const boardId = 'board123'
    const result1 = getBoardThemeColor(boardId)
    const result2 = getBoardThemeColor(boardId)
    expect(result1).toBe(result2)
  })

  it('should return different colors for different boardIds', () => {
    const result1 = getBoardThemeColor('board123')
    const result2 = getBoardThemeColor('board456')
    expect(result1).not.toBe(result2)
  })

  it('should handle invalid board color gracefully', () => {
    const result = getBoardThemeColor('board123', 'invalid-color')
    expect(Object.values(boardGradientThemes)).toContain(result)
  })

  it('should handle empty boardId', () => {
    const result = getBoardThemeColor('')
    expect(Object.values(boardGradientThemes)).toContain(result)
  })

  it('should handle non-numeric boardId', () => {
    const result = getBoardThemeColor('abc')
    expect(Object.values(boardGradientThemes)).toContain(result)
  })
}) 