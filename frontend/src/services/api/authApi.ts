import * as apiClient from './client';
import type * as Oazapfts from '@oazapfts/runtime';

// 인증이 필요 없는 API 목록 (화이트리스트 방식)
const PUBLIC_APIS = new Set([
  'registerUser'
]);

/**
 * 함수가 인증이 필요한지 확인
 * @param functionName - 함수 이름
 * @returns 인증 필요 여부
 */
function requiresAuthentication(functionName: string): boolean {
  return !PUBLIC_APIS.has(functionName);
}

/**
 * 함수를 인증 헤더와 함께 래핑
 * @param fn - 원본 함수
 * @param authOpts - 인증 옵션
 * @returns 래핑된 함수
 */
function wrapWithAuth<T extends (...args: any[]) => any>(
  fn: T,
  authOpts: Oazapfts.RequestOpts
): T {
  return ((...args: any[]) => {
    // 마지막 인자가 opts인지 확인
    const lastArg = args[args.length - 1];
    const hasOpts = lastArg && typeof lastArg === 'object' && lastArg !== null && 'headers' in lastArg;
    
    if (hasOpts) {
      // 기존 opts가 있으면 인증 헤더 병합
      const mergedOpts = {
        ...lastArg as Record<string, unknown>,
        headers: {
          ...(lastArg as Record<string, unknown>).headers as Record<string, unknown>,
          ...authOpts.headers,
        },
      };
      return fn(...args.slice(0, -1), mergedOpts);
    } else {
      // opts가 없으면 인증 헤더 추가
      return fn(...args, authOpts);
    }
  }) as T;
}

/**
 * 인증이 필요한 API 클라이언트를 생성하는 팩토리 함수
 * @param accessToken - 사용자 액세스 토큰
 * @returns 인증 헤더가 포함된 API 클라이언트
 */
export function createAuthenticatedApiClient(accessToken: string) {
  const authOpts: Oazapfts.RequestOpts = {
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  };

  // client.ts에서 export된 모든 함수를 자동으로 처리
  const authenticatedClient: Record<string, unknown> = {};
  
  // apiClient의 모든 export를 순회
  for (const [key, value] of Object.entries(apiClient)) {
    // 함수인지 확인
    if (typeof value === 'function') {
      if (requiresAuthentication(key)) {
        // 인증이 필요한 함수는 래핑
        authenticatedClient[key] = wrapWithAuth(value, authOpts);
      }
      // 인증이 필요 없는 함수는 제외 (publicApi에서 처리)
    }
  }

  return authenticatedClient;
}

/**
 * 인증이 필요 없는 API들 (자동 감지)
 */
export const publicApi: Record<string, unknown> = {};

// apiClient의 모든 export를 순회하여 공개 API만 추출
for (const [key, value] of Object.entries(apiClient)) {
  if (typeof value === 'function' && !requiresAuthentication(key)) {
    publicApi[key] = value;
  }
}

// 기존 함수들도 호환성을 위해 유지 (deprecated)
/**
 * @deprecated createAuthenticatedApiClient를 사용하세요
 */
export async function updateAuthenticatedUser(accessToken: string, updateUserRequest: apiClient.UpdateUserRequest) {
  const client = createAuthenticatedApiClient(accessToken);
  return (client.updateUser as typeof apiClient.updateUser)(updateUserRequest);
}

/**
 * @deprecated publicApi.registerUser를 사용하세요
 */
export async function registerNewUser(registerUserRequest: apiClient.RegisterUserRequest) {
  return (publicApi.registerUser as typeof apiClient.registerUser)(registerUserRequest);
}
