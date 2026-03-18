export type AuthResponse = {
  userId: number;
  firstName: string;
  lastName: string;
  phone: string;
  email: string;
  accessToken: string;
  refreshToken: string;
  refreshTokenExpiresAt: string;
};

export type RegisterRequest = {
  firstName: string;
  lastName: string;
  phone: string;
  email: string;
  password: string;
};

export type LoginRequest = {
  email: string;
  password: string;
};

export type RefreshRequest = {
  refreshToken: string;
};
