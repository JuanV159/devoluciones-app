export interface ConciliacionDia {
  fecha: string;
  totalSolicitado: number;
  totalAprobado: number;
  totalPagado: number;
  tasaRechazo: number;
}

export interface BancoTop {
  bancoDestino: string;
  montoTotal: number;
}

export interface Conciliacion {
  desde: string;
  hasta: string;
  porDia: ConciliacionDia[];
  topBancos: BancoTop[];
}
