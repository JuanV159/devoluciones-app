export type Estado =
  | 'BORRADOR'
  | 'EN_REVISION'
  | 'APROBADA'
  | 'RECHAZADA'
  | 'PAGADA'
  | 'ANULADA';

export type Origen = 'MANUAL' | 'CARGA_MASIVA';

export type AccionSolicitud =
  | 'ENVIAR'
  | 'APROBAR'
  | 'RECHAZAR'
  | 'PAGAR'
  | 'REABRIR'
  | 'ANULAR';

export interface Solicitud {
  id: number;
  folio: string;
  rutCliente: string;
  nombreCliente: string;
  monto: number;
  moneda: string;
  bancoDestino: string;
  cuentaDestino: string;
  origen: Origen;
  estado: Estado;
  motivoRechazo: string | null;
  referenciaBanco: string | null;
  reaperturas: number;
  creadaPor: string;
  fechaCreacion: string;
  actualizadaPor: string | null;
  fechaActualizacion: string | null;
}

export interface SolicitudRequest {
  rutCliente: string;
  nombreCliente: string;
  monto: number;
  bancoDestino: string;
  cuentaDestino: string;
}

export interface EventoSolicitud {
  id: number;
  estadoOrigen: Estado;
  estadoDestino: Estado;
  usuario: string;
  fecha: string;
  comentario: string | null;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  /** Índice 0-based que serializa Spring Page; no usar en plantillas (conflicto con `number`). */
  number?: number;
  first: boolean;
  last: boolean;
  pageable?: {
    pageNumber: number;
    pageSize: number;
  };
}

export interface SolicitudFiltros {
  estado?: Estado | '';
  rut?: string;
  desde?: string;
  hasta?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export const ESTADOS: Estado[] = [
  'BORRADOR',
  'EN_REVISION',
  'APROBADA',
  'RECHAZADA',
  'PAGADA',
  'ANULADA',
];
