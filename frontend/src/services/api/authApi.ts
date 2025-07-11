import * as apiClient from './client';
import type { UpdateUserRequest, RegisterUserRequest } from './client';
import type * as Oazapfts from '@oazapfts/runtime';


function createAuthenticatedOptsWithToken(accessToken: string): Oazapfts.RequestOpts {
  return {
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  };
}

/**
 * 인증된 사용자 정보 조회
 */
export async function getAuthenticatedUser(accessToken: string) {
  const authOpts = createAuthenticatedOptsWithToken(accessToken);
  return apiClient.getUser(authOpts);
}

/**
 * 인증된 사용자 정보 업데이트
 */
export async function updateAuthenticatedUser(accessToken: string, updateUserRequest: UpdateUserRequest) {
  const authOpts = createAuthenticatedOptsWithToken(accessToken);
  return apiClient.updateUser(updateUserRequest, authOpts);
}

/**
 * 사용자 등록 (인증 불필요)
 */
export async function registerNewUser(registerUserRequest: RegisterUserRequest) {
  return apiClient.registerUser(registerUserRequest);
}
