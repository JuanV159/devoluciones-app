import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { SolicitudService } from '../../../core/services/solicitud.service';
import { mensajeApiError } from '../../../core/utils/api-error';
import { rutChilenoValidator } from '../../../core/utils/rut.validator';

@Component({
  selector: 'app-solicitud-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './form.component.html',
  styleUrl: './form.component.scss',
})
export class SolicitudFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly solicitudService = inject(SolicitudService);

  readonly cargando = signal(false);
  readonly error = signal<string | null>(null);
  readonly esEdicion = signal(false);

  private id: number | null = null;

  readonly form = this.fb.group({
    rutCliente: this.fb.nonNullable.control('', [Validators.required, rutChilenoValidator()]),
    nombreCliente: this.fb.nonNullable.control('', [Validators.required, Validators.minLength(3)]),
    monto: this.fb.control<number | null>(null, [
      Validators.required,
      Validators.min(0.01),
      Validators.max(10_000_000),
    ]),
    bancoDestino: this.fb.nonNullable.control('', Validators.required),
    cuentaDestino: this.fb.nonNullable.control('', Validators.required),
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.id = Number(idParam);
      this.esEdicion.set(true);
      this.cargar(this.id);
    }
  }

  submit(): void {
    this.error.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    if (raw.monto == null) {
      this.form.controls.monto.markAsTouched();
      return;
    }
    const body = {
      rutCliente: raw.rutCliente,
      nombreCliente: raw.nombreCliente,
      monto: raw.monto,
      bancoDestino: raw.bancoDestino,
      cuentaDestino: raw.cuentaDestino,
    };
    this.cargando.set(true);

    const request$ =
      this.id != null
        ? this.solicitudService.actualizar(this.id, body)
        : this.solicitudService.crear(body);

    request$.subscribe({
      next: (solicitud) => {
        this.cargando.set(false);
        void this.router.navigate(['/solicitudes', solicitud.id]);
      },
      error: (err) => {
        this.cargando.set(false);
        this.error.set(mensajeApiError(err, 'No se pudo guardar la solicitud'));
      },
    });
  }

  private cargar(id: number): void {
    this.cargando.set(true);
    this.solicitudService.obtener(id).subscribe({
      next: (s) => {
        this.form.setValue({
          rutCliente: s.rutCliente,
          nombreCliente: s.nombreCliente,
          monto: Number(s.monto),
          bancoDestino: s.bancoDestino,
          cuentaDestino: s.cuentaDestino,
        });
        this.cargando.set(false);
      },
      error: (err) => {
        this.cargando.set(false);
        this.error.set(mensajeApiError(err, 'No se pudo cargar la solicitud'));
      },
    });
  }
}
