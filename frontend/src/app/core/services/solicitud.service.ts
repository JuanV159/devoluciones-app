import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  EventoSolicitud,
  Page,
  Solicitud,
  SolicitudFiltros,
  SolicitudRequest,
} from '../models/solicitud.model';

@Injectable({ providedIn: 'root' })
export class SolicitudService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/solicitudes`;

  listar(filtros: SolicitudFiltros = {}): Observable<Page<Solicitud>> {
    let params = new HttpParams()
      .set('page', String(filtros.page ?? 0))
      .set('size', String(filtros.size ?? 10))
      .set('sort', filtros.sort ?? 'fechaCreacion,desc');

    if (filtros.estado) {
      params = params.set('estado', filtros.estado);
    }
    if (filtros.rut?.trim()) {
      params = params.set('rut', filtros.rut.trim());
    }
    if (filtros.desde) {
      params = params.set('desde', filtros.desde);
    }
    if (filtros.hasta) {
      params = params.set('hasta', filtros.hasta);
    }

    return this.http.get<Page<Solicitud>>(this.base, { params });
  }

  obtener(id: number): Observable<Solicitud> {
    return this.http.get<Solicitud>(`${this.base}/${id}`);
  }

  crear(body: SolicitudRequest): Observable<Solicitud> {
    return this.http.post<Solicitud>(this.base, body);
  }

  actualizar(id: number, body: SolicitudRequest): Observable<Solicitud> {
    return this.http.put<Solicitud>(`${this.base}/${id}`, body);
  }

  historial(id: number): Observable<EventoSolicitud[]> {
    return this.http.get<EventoSolicitud[]>(`${this.base}/${id}/historial`);
  }

  enviar(id: number): Observable<Solicitud> {
    return this.http.post<Solicitud>(`${this.base}/${id}/enviar`, {});
  }

  aprobar(id: number): Observable<Solicitud> {
    return this.http.post<Solicitud>(`${this.base}/${id}/aprobar`, {});
  }

  rechazar(id: number, motivoRechazo: string): Observable<Solicitud> {
    return this.http.post<Solicitud>(`${this.base}/${id}/rechazar`, { motivoRechazo });
  }

  pagar(id: number): Observable<Solicitud> {
    return this.http.post<Solicitud>(`${this.base}/${id}/pagar`, {});
  }

  reabrir(id: number): Observable<Solicitud> {
    return this.http.post<Solicitud>(`${this.base}/${id}/reabrir`, {});
  }

  anular(id: number): Observable<Solicitud> {
    return this.http.post<Solicitud>(`${this.base}/${id}/anular`, {});
  }
}
