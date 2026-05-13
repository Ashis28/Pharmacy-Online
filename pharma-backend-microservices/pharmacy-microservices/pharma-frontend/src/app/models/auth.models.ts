export interface SignupRequest {
  name: string;
  email: string;
  password: string;
  roles: string[];
  adminToken?: string;   // required when roles includes 'ADMIN'
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  name: string;
  id: number;
  roles: string[];
}

export interface AuthUser {
  token: string;
  name: string;
  email: string;
  id: number;
  roles: string[];
}
