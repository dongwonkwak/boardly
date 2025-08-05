import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { Avatar } from './avatar';
import * as languageStore from '@/store/languageStore';
import * as utils from '@/lib/utils';

// useCurrentLanguage 모킹
vi.mock('@/store/languageStore', () => ({
  useCurrentLanguage: vi.fn(),
}));

// getUserInitials, getAvatarBackgroundColor만 모킹, cn은 실제 구현 사용
vi.mock('@/lib/utils', async () => {
  const actual = await vi.importActual<typeof import('@/lib/utils')>('@/lib/utils');
  return {
    ...actual,
    getUserInitials: vi.fn(),
    getAvatarBackgroundColor: vi.fn(),
  };
});

describe('Avatar', () => {
  const mockUseCurrentLanguage = vi.mocked(languageStore.useCurrentLanguage);
  const mockGetUserInitials = vi.mocked(utils.getUserInitials);
  const mockGetAvatarBackgroundColor = vi.mocked(utils.getAvatarBackgroundColor);

  beforeEach(() => {
    vi.clearAllMocks();
    mockGetUserInitials.mockReturnValue('김홍');
    mockGetAvatarBackgroundColor.mockReturnValue('bg-blue-500');
    mockUseCurrentLanguage.mockReturnValue('ko');
  });

  it('기본 props로 렌더링되어야 한다', () => {
    
    render(<Avatar firstName="홍길동" lastName="김" />);
    
    const avatar = screen.getByText('김홍');
    expect(avatar).toBeInTheDocument();
    expect(avatar).toHaveClass('rounded-full', 'flex', 'items-center', 'justify-center', 'text-white', 'font-medium', 'w-8', 'h-8', 'text-sm', 'bg-blue-500');
  });

  it('한국어일 때 성+이름 순서로 이니셜을 표시해야 한다', () => {
    mockUseCurrentLanguage.mockReturnValue('ko');
    mockGetUserInitials.mockReturnValue('김홍');
    
    render(<Avatar firstName="홍길동" lastName="김" />);
    
    expect(mockGetUserInitials).toHaveBeenCalledWith('홍길동', '김', 'ko');
    expect(screen.getByText('김홍')).toBeInTheDocument();
  });

  it('영어일 때 이름+성 순서로 이니셜을 표시해야 한다', () => {
    mockUseCurrentLanguage.mockReturnValue('en');
    mockGetUserInitials.mockReturnValue('HK');
    
    render(<Avatar firstName="Hong" lastName="Kim" />);
    
    expect(mockGetUserInitials).toHaveBeenCalledWith('Hong', 'Kim', 'en');
    expect(screen.getByText('HK')).toBeInTheDocument();
  });

  it('다양한 크기 옵션을 지원해야 한다', () => {
    mockUseCurrentLanguage.mockReturnValue('ko');
    
    const { rerender } = render(<Avatar firstName="홍길동" lastName="김" size="sm" />);
    expect(screen.getByText('김홍')).toHaveClass('w-6', 'h-6', 'text-xs');
    
    rerender(<Avatar firstName="홍길동" lastName="김" size="lg" />);
    expect(screen.getByText('김홍')).toHaveClass('w-12', 'h-12', 'text-base');
    
    rerender(<Avatar firstName="홍길동" lastName="김" size="xl" />);
    expect(screen.getByText('김홍')).toHaveClass('w-16', 'h-16', 'text-lg');
  });

  it('사용자 정의 클래스명을 적용해야 한다', () => {
    mockUseCurrentLanguage.mockReturnValue('ko');
    
    render(<Avatar firstName="홍길동" lastName="김" className="custom-class" />);
    
    const avatar = screen.getByText('김홍');
    expect(avatar.className).toContain('custom-class');
  });

  it('사용자 정의 스타일을 적용해야 한다', () => {
    mockUseCurrentLanguage.mockReturnValue('ko');
    const customStyle = { backgroundColor: 'red' };
    render(
      <Avatar
        firstName="홍길동"
        lastName="김"
        style={customStyle}
        useGradient={false}
        userId=""
      />
    );
    const avatar = screen.getByText('김홍');
    expect(avatar.getAttribute('style')).toContain('background-color: red');
  });

  it('userId를 사용하여 배경색을 결정해야 한다', () => {
    mockUseCurrentLanguage.mockReturnValue('ko');
    
    render(<Avatar firstName="홍길동" lastName="김" userId="custom-user-id" />);
    
    expect(mockGetAvatarBackgroundColor).toHaveBeenCalledWith('custom-user-id');
  });

  it('userId가 없으면 firstName + lastName을 사용해야 한다', () => {
    mockUseCurrentLanguage.mockReturnValue('ko');
    
    render(<Avatar firstName="홍길동" lastName="김" />);
    
    expect(mockGetAvatarBackgroundColor).toHaveBeenCalledWith('홍길동김');
  });

  it('그라디언트 배경을 사용할 수 있어야 한다', () => {
    mockUseCurrentLanguage.mockReturnValue('ko');
    
    render(
      <Avatar 
        firstName="홍길동" 
        lastName="김" 
        useGradient={true}
        gradientColors={{ from: 'from-red-500', to: 'to-blue-500' }}
      />
    );
    
    const avatar = screen.getByText('김홍');
    expect(avatar.className).toContain('bg-gradient-to-r');
    expect(avatar.className).toContain('from-red-500');
    expect(avatar.className).toContain('to-blue-500');
  });

  it('기본 그라디언트 색상을 사용해야 한다', () => {
    mockUseCurrentLanguage.mockReturnValue('ko');
    
    render(<Avatar firstName="홍길동" lastName="김" useGradient={true} />);
    
    const avatar = screen.getByText('김홍');
    expect(avatar.className).toContain('bg-gradient-to-r');
    expect(avatar.className).toContain('from-blue-600');
    expect(avatar.className).toContain('to-purple-600');
  });
}); 