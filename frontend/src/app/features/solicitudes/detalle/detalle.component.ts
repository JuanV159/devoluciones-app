import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { AccionSolicitud, EventoSolicitud, Solicitud } from '../../../core/models/solicitud.model';
import { SolicitudService } from '../../../core/services/solicitud.service';
import { accionesDisponibles, puedeEditar } from '../../../core/utils/acciones-disponibles';
import { mensajeApiError } from '../../../core/utils/api-error';
import { EstadoBadgeComponent } from '../../../shared/components/estado-badge.component';

@Component({
  selector: 'app-detalle-solicitud',
  standalone: true,
  imports: [RouterLink, CurrencyPipe, DatePipe, ReactiveFormsModule, EstadoBadgeComponent],
  templateUrl: './detalle.component.html',
  styleUrl: './detalle.component.scss',
})
export class DetalleSolicitudComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly auth = inject(AuthService);
  private readonly solicitudService = inject(SolicitudService);
  private readonly fb = inject(FormBuilder);

  readonly cargando = signal(true);
  readonly accionando = signal(false);
  readonly error = signal<string | null>(null);
  readonly solicitud = signal<Solicitud | null>(null);
  readonly historial = signal<EventoSolicitud[]>([]);
  readonly acciones = signal<AccionSolicitud[]>([]);
  readonly editable = signal(false);
  readonly mostrarRechazo = signal(false);

  readonly rechazoForm = this.fb.nonNullable.group({
    motivoRechazo: ['', [Validators.required, Validators.minLength(3)]],
  });

  id = 0;

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    forkJoin({
      solicitud: this.solicitudService.obtener(this.id),
      historial: this.solicitudService.historial(this.id),
    }).subscribe({
      next: ({ solicitud, historial }) => {
        this.aplicar(solicitud, historial);
        this.cargando.set(false);
      },
      error: (err) => {
        this.cargando.set(false);
        this.error.set(mensajeApiError(err, 'No se pudo cargar el detalle'));
      },
    });
  }

  ejecutar(accion: AccionSolicitud): void {
    if (accion === 'RECHAZAR') {
      this.mostrarRechazo.set(true);
      return;
    }
    this.correrAccion(accion);
  }

  confirmarRechazo(): void {
    if (this.rechazoForm.invalid) {
      this.rechazoForm.markAllAsTouched();
      return;
    }
    this.correrAccion('RECHAZAR', this.rechazoForm.controls.motivoRechazo.value);
  }

  private correrAccion(accion: AccionSolicitud, motivo?: string): void {
    this.accionando.set(true);
    this.error.set(null);

    const llamadas: Record<AccionSolicitud, () => ReturnType<SolicitudService['enviar']>> = {
      ENVIAR: () => this.solicitudService.enviar(this.id),
      APROBAR: () => this.solicitudService.aprobar(this.id),
      RECHAZAR: () => this.solicitudService.rechazar(this.id, motivo ?? ''),
      PAGAR: () => this.solicitudService.pagar(this.id),
      REABRIR: () => this.solicitudService.reabrir(this.id),
      ANULAR: () => this.solicitudService.anular(this.id),
    };

    llamadas[accion]().subscribe({
      next: (solicitud) => {
        this.solicitudService.historial(this.id).subscribe({
          next: (historial) => {
            this.aplicar(solicitud, historial);
            this.mostrarRechazo.set(false);
            this.rechazoForm.reset({ motivoRechazo: '' });
            this.accionando.set(false);
          },
          error: () => {
            this.aplicar(solicitud, this.historial());
            this.accionando.set(false);
          },
        });
      },
      error: (err) => {
        this.accionando.set(false);
        this.error.set(mensajeApiError(err, `No se pudo ejecutar ${accion}`));
      },
    });
  }

  private aplicar(solicitud: Solicitud, historial: EventoSolicitud[]): void {
    const rol = this.auth.rol();
    this.solicitud.set(solicitud);
    this.historial.set(historial);
    this.editable.set(puedeEditar(solicitud));
    this.acciones.set(rol ? accionesDisponibles(solicitud, rol) : []);
  }
}
