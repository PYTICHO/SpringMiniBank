import { apiClient } from "./client";
import type { AuthResponse, LoginRequest, RefreshRequest, RegisterRequest } from "../types/auth";

export async function register(payload: RegisterRequest) {
  const response = await apiClient.post<AuthResponse>("/api/auth/register", payload);
  return response.data;
}

export async function login(payload: LoginRequest) {
  const response = await apiClient.post<AuthResponse>("/api/auth/login", payload);
  return response.data;
}

export async function refresh(payload: RefreshRequest) {
  const response = await apiClient.post<AuthResponse>("/api/auth/refresh", payload);
  return response.data;
}
