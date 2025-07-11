import { create } from 'zustand';
import { devtools } from 'zustand/middleware';
import { getAuthenticatedUser, updateAuthenticatedUser } from '../services/api/authApi';
import type { UserResponse, UpdateUserRequest } from '../services/api/client';

interface UserState {
  user: UserResponse | null;
  isLoading: boolean;
  error: string | null;
}

interface UserActions {
  fetchUser: (accessToken: string) => Promise<void>;
  updateUser: (accessToken: string, userData: UpdateUserRequest) => Promise<void>;
  setUser: (user: UserResponse | null) => void;
  clearUser: () => void;
  clearError: () => void;
}

type UserStore = UserState & UserActions;

export const useUserStore = create<UserStore>()(
  devtools(
    (set) => ({
      // 상태
      user: null,
      isLoading: false,
      error: null,

      // 액션
      fetchUser: async (accessToken: string) => {
        set({ isLoading: true, error: null });
        
        try {
          const response = await getAuthenticatedUser(accessToken);
          
          if (response.status === 200) {
            set({ user: response.data, isLoading: false });
          } else {
            const errorMessage = response.data?.message || '사용자 정보를 가져오는데 실패했습니다.';
            set({ error: errorMessage, isLoading: false });
          }
        } catch (error) {
          const errorMessage = error instanceof Error ? error.message : '네트워크 오류가 발생했습니다.';
          set({ error: errorMessage, isLoading: false });
        }
      },

      updateUser: async (accessToken: string, userData: UpdateUserRequest) => {
        set({ isLoading: true, error: null });
        
        try {
          const response = await updateAuthenticatedUser(accessToken, userData);
          
          if (response.status === 200) {
            set({ user: response.data, isLoading: false });
          } else {
            const errorMessage = response.data?.message || '사용자 정보 업데이트에 실패했습니다.';
            set({ error: errorMessage, isLoading: false });
          }
        } catch (error) {
          const errorMessage = error instanceof Error ? error.message : '네트워크 오류가 발생했습니다.';
          set({ error: errorMessage, isLoading: false });
        }
      },

      setUser: (user: UserResponse | null) => {
        set({ user });
      },

      clearUser: () => {
        set({ user: null, error: null });
      },

      clearError: () => {
        set({ error: null });
      },
    }),
    {
      name: 'user-store',
      }
  )
);

// 편의를 위한 선택자들
export const useUser = () => useUserStore((state) => state.user);
export const useUserLoading = () => useUserStore((state) => state.isLoading);
export const useUserError = () => useUserStore((state) => state.error); 