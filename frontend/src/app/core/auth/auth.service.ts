import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, Rol, SesionUsuario } from '../models/auth.model';

const TOKEN_KEY = 'dev_access_token';
const USER_KEY = 'dev_username';
const ROL_KEY = 'dev_rol';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly sesionSignal = signal<SesionUsuario | null>(this.leerSesion());

  readonly sesion = this.sesionSignal.asReadonly();

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, request).pipe(
      tap((response) => {
        this.guardarSesion({
          username: request.username,
          rol: response.rol,
          accessToken: response.access_token,
        });
      })
    );
  }

  logout(): void {
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(USER_KEY);
    sessionStorage.removeItem(ROL_KEY);
    this.sesionSignal.set(null);
    void this.router.navigate(['/login']);
  }

  isAuthenticated(): boolean {
    return !!this.sesionSignal()?.accessToken;
  }

  token(): string | null {
    return this.sesionSignal()?.accessToken ?? null;
  }

  rol(): Rol | null {
    return this.sesionSignal()?.rol ?? null;
  }

  username(): string | null {
    return this.sesionSignal()?.username ?? null;
  }

  private guardarSesion(sesion: SesionUsuario): void {
    sessionStorage.setItem(TOKEN_KEY, sesion.accessToken);
    sessionStorage.setItem(USER_KEY, sesion.username);
    sessionStorage.setItem(ROL_KEY, sesion.rol);
    this.sesionSignal.set(sesion);
  }

  private leerSesion(): SesionUsuario | null {
    const accessToken = sessionStorage.getItem(TOKEN_KEY);
    const username = sessionStorage.getItem(USER_KEY);
    const rol = sessionStorage.getItem(ROL_KEY) as Rol | null;
    if (!accessToken || !username || !rol) {
      return null;
    }
    return { accessToken, username, rol };
  }
}
