export type EstadoCarga = 'PROCESANDO' | 'COMPLETADA' | 'FALLIDA';

export interface CargaError {
  fila: number;
  campo: string | null;
  motivo: string;
  referenciaBanco: string | null;
}

export interface Carga {
  id: number;
  nombreArchivo: string;
  estado: EstadoCarga;
  totalFilas: number;
  filasOk: number;
  filasRechazadas: number;
  filasOmitidas: number;
  creadaPor: string;
  fechaCreacion: string;
  fechaFin: string | null;
  errores: CargaError[];
}
