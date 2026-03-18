import React, { createContext, useContext, useEffect, useMemo, useState } from "react";

import * as authApi from "../api/auth";
import { clearTokens, getRefreshToken, saveTokens } from "../storage/tokens";
import type { AuthResponse, LoginRequest, RegisterRequest } from "../types/auth";

type AuthContextValue = {
  user: AuthResponse | null;
  loading: boolean;
  signIn: (payload: LoginRequest) => Promise<void>;
  signUp: (payload: RegisterRequest) => Promise<void>;
  signOut: () => Promise<void>;
  refreshSession: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    void bootstrap();
  }, []);

  async function bootstrap() {
    try {
      const storedRefreshToken = await getRefreshToken();

      if (!storedRefreshToken) {
        setLoading(false);
        return;
      }

      const nextUser = await authApi.refresh({ refreshToken: storedRefreshToken });
      await saveTokens(nextUser.accessToken, nextUser.refreshToken);
      setUser(nextUser);
    } catch {
      await clearTokens();
      setUser(null);
    } finally {
      setLoading(false);
    }
  }

  async function signIn(payload: LoginRequest) {
    const nextUser = await authApi.login(payload);
    await saveTokens(nextUser.accessToken, nextUser.refreshToken);
    setUser(nextUser);
  }

  async function signUp(payload: RegisterRequest) {
    const nextUser = await authApi.register(payload);
    await saveTokens(nextUser.accessToken, nextUser.refreshToken);
    setUser(nextUser);
  }

  async function signOut() {
    await clearTokens();
    setUser(null);
  }

  async function refreshSession() {
    const storedRefreshToken = await getRefreshToken();

    if (!storedRefreshToken) {
      throw new Error("No refresh token");
    }

    const nextUser = await authApi.refresh({ refreshToken: storedRefreshToken });
    await saveTokens(nextUser.accessToken, nextUser.refreshToken);
    setUser(nextUser);
  }

  const value = useMemo(
    () => ({
      user,
      loading,
      signIn,
      signUp,
      signOut,
      refreshSession
    }),
    [user, loading]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }

  return context;
}
