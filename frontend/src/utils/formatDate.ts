// Intl.RelativeTimeFormat을 사용한 국제화된 상대적 시간 포맷팅 함수
export const formatTimeAgo = (timestamp: string, locale: string): string => {
  const now = new Date();
  const date = new Date(timestamp);
  const diffInMs = now.getTime() - date.getTime();
  
  // 1분 미만
  if (diffInMs < 60 * 1000) {
    return new Intl.RelativeTimeFormat(locale, { numeric: 'auto' }).format(0, 'second');
  }
  
  // 1시간 미만 (분 단위)
  if (diffInMs < 60 * 60 * 1000) {
    const diffInMinutes = Math.floor(diffInMs / (60 * 1000));
    return new Intl.RelativeTimeFormat(locale, { numeric: 'auto' }).format(-diffInMinutes, 'minute');
  }
  
  // 24시간 미만 (시간 단위)
  if (diffInMs < 24 * 60 * 60 * 1000) {
    const diffInHours = Math.floor(diffInMs / (60 * 60 * 1000));
    return new Intl.RelativeTimeFormat(locale, { numeric: 'auto' }).format(-diffInHours, 'hour');
  }
  
  // 7일 미만 (일 단위)
  if (diffInMs < 7 * 24 * 60 * 60 * 1000) {
    const diffInDays = Math.floor(diffInMs / (24 * 60 * 60 * 1000));
    return new Intl.RelativeTimeFormat(locale, { numeric: 'auto' }).format(-diffInDays, 'day');
  }
  
  // 4주 미만 (주 단위)
  if (diffInMs < 4 * 7 * 24 * 60 * 60 * 1000) {
    const diffInWeeks = Math.floor(diffInMs / (7 * 24 * 60 * 60 * 1000));
    return new Intl.RelativeTimeFormat(locale, { numeric: 'auto' }).format(-diffInWeeks, 'week');
  }
  
  // 12개월 미만 (개월 단위)
  if (diffInMs < 12 * 30 * 24 * 60 * 60 * 1000) {
    const diffInMonths = Math.floor(diffInMs / (30 * 24 * 60 * 60 * 1000));
    return new Intl.RelativeTimeFormat(locale, { numeric: 'auto' }).format(-diffInMonths, 'month');
  }
  
  // 1년 이상 (년 단위)
  const diffInYears = Math.floor(diffInMs / (365 * 24 * 60 * 60 * 1000));
  return new Intl.RelativeTimeFormat(locale, { numeric: 'auto' }).format(-diffInYears, 'year');
}; 