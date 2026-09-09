import { Rol } from '../models/auth.model';
import { AccionSolicitud, Estado, Solicitud } from '../models/solicitud.model';

const ACCIONES_SUPERVISOR: AccionSolicitud[] = ['APROBAR', 'RECHAZAR', 'PAGAR'];

/** Espejo UI de la máquina de estados (R1–R2). La API sigue siendo autoridad. */
export function accionesDisponibles(solicitud: Solicitud, rol: Rol): AccionSolicitud[] {
  const porEstado: Partial<Record<Estado, AccionSolicitud[]>> = {
    BORRADOR: ['ENVIAR', 'ANULAR'],
    EN_REVISION: ['APROBAR', 'RECHAZAR'],
    APROBADA: ['PAGAR'],
    RECHAZADA: solicitud.reaperturas < 1 ? ['REABRIR'] : [],
    PAGADA: [],
    ANULADA: [],
  };

  return (porEstado[solicitud.estado] ?? []).filter((accion) => {
    if (ACCIONES_SUPERVISOR.includes(accion)) {
      return rol === 'SUPERVISOR';
    }
    return true;
  });
}

export function puedeEditar(solicitud: Solicitud): boolean {
  return solicitud.estado === 'BORRADOR';
}
