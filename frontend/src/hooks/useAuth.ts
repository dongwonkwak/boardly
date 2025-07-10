import log from "@/utils/logger";
import { useAuth } from "react-oidc-context";

export function useOAuth() {
  const auth = useAuth();

  const login = async () => {
    try {
      await auth.signinRedirect();
    } catch (error) {
      log.error("Login failed", error);
    }
  };

  const logout = async () => {
    try {
      await auth.removeUser();
    } catch (error) {
      log.error("Logout failed", error);
    }
  };

  const isAuthenticated = auth.isAuthenticated;
  const user = auth.user;
  const isLoading = auth.isLoading;

  return {
    login,
    logout,
    isAuthenticated,
    user,
    isLoading,
    auth,
  };
} 