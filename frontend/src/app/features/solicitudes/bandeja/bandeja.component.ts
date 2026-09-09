import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { EstadoBadgeComponent } from '../../../shared/components/estado-badge.component';
import { SolicitudService } from '../../../core/services/solicitud.service';
import { ESTADOS, Page, Solicitud } from '../../../core/models/solicitud.model';
import { mensajeApiError } from '../../../core/utils/api-error';

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

  readonly filtros = this.fb.nonNullable.group({
    estado: [''],
    rut: [''],
    desde: [''],
    hasta: [''],
  });

  private pageIndex = 0;
  private readonly pageSize = 10;

  /** Evita `p.number` en el template (Angular lo interpreta mal → NaN). */
  get paginaActual(): number {
    return this.pageIndex + 1;
  }

  get totalPaginas(): number {
    const p = this.page();
    return p?.totalPages && p.totalPages > 0 ? p.totalPages : 1;
  }

  get totalElementos(): number {
    return this.page()?.totalElements ?? 0;
  }

  ngOnInit(): void {
    this.buscar();
  }

  buscar(resetPage = true): void {
    if (resetPage) {
      this.pageIndex = 0;
    }
    this.cargando.set(true);
    this.error.set(null);

    const raw = this.filtros.getRawValue();
    this.solicitudService
      .listar({
        estado: (raw.estado || undefined) as never,
        rut: raw.rut || undefined,
        desde: raw.desde ? new Date(raw.desde).toISOString() : undefined,
        hasta: raw.hasta ? new Date(`${raw.hasta}T23:59:59`).toISOString() : undefined,
        page: this.pageIndex,
        size: this.pageSize,
      })
      .subscribe({
        next: (page) => {
          this.pageIndex = page.number ?? page.pageable?.pageNumber ?? this.pageIndex;
          this.page.set(page);
          this.cargando.set(false);
        },
        error: (err) => {
          this.cargando.set(false);
          this.error.set(mensajeApiError(err, 'No se pudo cargar la bandeja'));
        },
      });
  }

  limpiar(): void {
    this.filtros.reset({ estado: '', rut: '', desde: '', hasta: '' });
    this.buscar();
  }

  anterior(): void {
    if (this.pageIndex === 0) {
      return;
    }
    this.pageIndex -= 1;
    this.buscar(false);
  }

  siguiente(): void {
    const p = this.page();
    if (!p || p.last) {
      return;
    }
    this.pageIndex += 1;
    this.buscar(false);
  }
}
