import { HttpErrorResponse } from '@angular/common/http';
import { ApiError } from '../models/api-error.model';

export function mensajeApiError(error: unknown, fallback = 'Ocurrió un error inesperado'): string {
  if (!(error instanceof HttpErrorResponse)) {
    return fallback;
  }
  const body = error.error as ApiError | { message?: string } | string | null;
  if (body && typeof body === 'object' && 'detalle' in body && body.detalle) {
    return String(body.detalle);
  }
  if (body && typeof body === 'object' && 'message' in body && body.message) {
    return String(body.message);
  }
  if (typeof body === 'string' && body.trim()) {
    return body;
  }
  if (error.status === 0) {
    return 'No se pudo conectar con el servidor';
  }
  return fallback;
}
