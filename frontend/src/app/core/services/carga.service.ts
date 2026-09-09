import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Carga } from '../models/carga.model';

@Injectable({ providedIn: 'root' })
export class CargaService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/cargas`;

  subir(archivo: File): Observable<Carga> {
    const formData = new FormData();
    formData.append('archivo', archivo, archivo.name);
    return this.http.post<Carga>(this.base, formData);
  }

  obtener(id: number): Observable<Carga> {
    return this.http.get<Carga>(`${this.base}/${id}`);
  }
}
