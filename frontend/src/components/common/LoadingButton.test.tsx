import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import LoadingButton from './LoadingButton'

describe('LoadingButton Component', () => {
  it('should render normally when not loading', () => {
    render(<LoadingButton loading={false}>Submit</LoadingButton>)
    const button = screen.getByRole('button', { name: 'Submit' })
    expect(button).toBeInTheDocument()
    expect(button).not.toBeDisabled()
    expect(screen.queryByRole('img', { hidden: true })).not.toBeInTheDocument()
  })

  it('should show loading spinner when loading is true', () => {
    render(<LoadingButton loading={true}>Submit</LoadingButton>)
    const button = screen.getByRole('button')
    expect(button).toBeDisabled()
    
    // Check for the loader icon using CSS selector
    const loader = button.querySelector('.animate-spin')
    expect(loader).toBeInTheDocument()
  })

  it('should be disabled when loading is true', () => {
    render(<LoadingButton loading={true}>Submit</LoadingButton>)
    const button = screen.getByRole('button')
    expect(button).toBeDisabled()
  })

  it('should be disabled when disabled prop is true even if not loading', () => {
    render(<LoadingButton loading={false} disabled={true}>Submit</LoadingButton>)
    const button = screen.getByRole('button')
    expect(button).toBeDisabled()
  })

  it('should handle click events when not loading', async () => {
    const user = userEvent.setup()
    const handleClick = vi.fn()
    
    render(<LoadingButton loading={false} onClick={handleClick}>Submit</LoadingButton>)
    const button = screen.getByRole('button')
    
    await user.click(button)
    expect(handleClick).toHaveBeenCalledTimes(1)
  })

  it('should not handle click events when loading', async () => {
    const user = userEvent.setup()
    const handleClick = vi.fn()
    
    render(<LoadingButton loading={true} onClick={handleClick}>Submit</LoadingButton>)
    const button = screen.getByRole('button')
    
    await user.click(button)
    expect(handleClick).not.toHaveBeenCalled()
  })

  it('should pass through button props', () => {
    render(
      <LoadingButton 
        loading={false}
        variant="destructive"
        size="lg"
        className="custom-class"
      >
        Delete
      </LoadingButton>
    )
    const button = screen.getByRole('button')
    expect(button).toHaveClass('bg-destructive')
    expect(button).toHaveClass('h-10')
    expect(button).toHaveClass('custom-class')
  })

  it('should show both loader and text when loading', () => {
    render(<LoadingButton loading={true}>Submitting</LoadingButton>)
    expect(screen.getByText('Submitting')).toBeInTheDocument()
    
    const button = screen.getByRole('button')
    const loader = button.querySelector('.animate-spin')
    expect(loader).toBeInTheDocument()
  })
}) 