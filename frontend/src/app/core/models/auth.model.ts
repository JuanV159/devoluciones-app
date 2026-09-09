export type Rol = 'ANALISTA' | 'SUPERVISOR';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  access_token: string;
  expires_in: number;
  rol: Rol;
}

export interface SesionUsuario {
  username: string;
  rol: Rol;
  accessToken: string;
}
