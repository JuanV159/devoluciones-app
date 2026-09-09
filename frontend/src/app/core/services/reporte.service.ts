import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Conciliacion } from '../models/reporte.model';

@Injectable({ providedIn: 'root' })
export class ReporteService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/reportes`;

  conciliacion(desde: string, hasta: string): Observable<Conciliacion> {
    const params = new HttpParams().set('desde', desde).set('hasta', hasta);
    return this.http.get<Conciliacion>(`${this.base}/conciliacion`, { params });
  }
}
