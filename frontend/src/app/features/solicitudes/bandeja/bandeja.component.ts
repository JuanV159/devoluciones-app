import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { EstadoBadgeComponent } from '../../../shared/components/estado-badge.component';
import { SolicitudService } from '../../../core/services/solicitud.service';
import { ESTADOS, Page, Solicitud } from '../../../core/models/solicitud.model';
import { mensajeApiError } from '../../../core/utils/api-error';

type FiltrosBandeja = {
  estado: string;
  rut: string;
  desde: string;
  hasta: string;
};

const FILTROS_VACIOS: FiltrosBandeja = {
  estado: '',
  rut: '',
  desde: '',
  hasta: '',
};

@Component({
  selector: 'app-bandeja',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, CurrencyPipe, DatePipe, EstadoBadgeComponent],
  templateUrl: './bandeja.component.html',
  styleUrl: './bandeja.component.scss',
})
export class BandejaComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly solicitudService = inject(SolicitudService);

  readonly estados = ESTADOS;
  readonly cargando = signal(false);
  readonly error = signal<string | null>(null);
  readonly page = signal<Page<Solicitud> | null>(null);

  /** Fecha local YYYY-MM-DD para max del date picker. */
  readonly hoy = fechaLocalIso(new Date());

  readonly filtros = this.fb.nonNullable.group({
    estado: [''],
    rut: [''],
    desde: [''],
    hasta: [''],
  });

  /** Filtros confirmados con Filtrar/Limpiar; la paginación solo usa estos. */
  private filtrosAplicados: FiltrosBandeja = { ...FILTROS_VACIOS };

  private pageIndex = 0;
  private readonly pageSize = 10;

  get paginaActual(): number {
    return this.pageIndex + 1;
  }

  get totalPaginas(): number {
    const total = this.page()?.page?.totalPages ?? 0;
    return total > 0 ? total : 1;
  }

  get totalElementos(): number {
    return this.page()?.page?.totalElements ?? 0;
  }

  get esPrimera(): boolean {
    return this.pageIndex <= 0;
  }

  get esUltima(): boolean {
    const meta = this.page()?.page;
    if (!meta || meta.totalElements === 0) {
      return true;
    }
    return this.pageIndex >= meta.totalPages - 1;
  }

  /** min de Hasta = Desde del formulario (si hay). */
  get minHasta(): string {
    return this.filtros.controls.desde.value || '';
  }

  ngOnInit(): void {
    this.cargar();
  }

  /** Confirma el formulario y vuelve a página 0. */
  filtrar(): void {
    if (!this.validarFechasFormulario()) {
      return;
    }
    this.filtrosAplicados = this.filtros.getRawValue();
    this.pageIndex = 0;
    this.cargar();
  }

  limpiar(): void {
    this.filtros.reset({ ...FILTROS_VACIOS });
    this.filtrosAplicados = { ...FILTROS_VACIOS };
    this.pageIndex = 0;
    this.error.set(null);
    this.cargar();
  }

  onDesdeChange(): void {
    const desde = this.filtros.controls.desde.value;
    const hasta = this.filtros.controls.hasta.value;
    if (desde && hasta && hasta < desde) {
      this.filtros.controls.hasta.setValue('');
    }
  }

  anterior(): void {
    if (this.esPrimera) {
      return;
    }
    this.pageIndex -= 1;
    this.cargar();
  }

  siguiente(): void {
    if (this.esUltima) {
      return;
    }
    this.pageIndex += 1;
    this.cargar();
  }

  private cargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    const f = this.filtrosAplicados;
    this.solicitudService
      .listar({
        estado: (f.estado || undefined) as never,
        rut: f.rut || undefined,
        desde: f.desde ? new Date(f.desde).toISOString() : undefined,
        hasta: f.hasta ? new Date(`${f.hasta}T23:59:59`).toISOString() : undefined,
        page: this.pageIndex,
        size: this.pageSize,
      })
      .subscribe({
        next: (respuesta) => {
          this.pageIndex = respuesta.page?.number ?? this.pageIndex;
          this.page.set(respuesta);
          this.cargando.set(false);
        },
        error: (err) => {
          this.cargando.set(false);
          this.error.set(mensajeApiError(err, 'No se pudo cargar la bandeja'));
        },
      });
  }

  private validarFechasFormulario(): boolean {
    const { desde, hasta } = this.filtros.getRawValue();
    if (desde && desde > this.hoy) {
      this.error.set('La fecha Desde no puede ser posterior a hoy');
      return false;
    }
    if (hasta && hasta > this.hoy) {
      this.error.set('La fecha Hasta no puede ser posterior a hoy');
      return false;
    }
    if (desde && hasta && hasta < desde) {
      this.error.set('La fecha Hasta no puede ser anterior a Desde');
      return false;
    }
    return true;
  }
}

function fechaLocalIso(date: Date): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}
