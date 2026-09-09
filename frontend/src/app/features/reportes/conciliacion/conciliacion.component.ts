import { CurrencyPipe, PercentPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Conciliacion } from '../../../core/models/reporte.model';
import { ReporteService } from '../../../core/services/reporte.service';
import { mensajeApiError } from '../../../core/utils/api-error';

@Component({
  selector: 'app-conciliacion',
  standalone: true,
  imports: [ReactiveFormsModule, CurrencyPipe, PercentPipe],
  templateUrl: './conciliacion.component.html',
  styleUrl: './conciliacion.component.scss',
})
export class ConciliacionComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly reporteService = inject(ReporteService);

  readonly cargando = signal(false);
  readonly error = signal<string | null>(null);
  readonly data = signal<Conciliacion | null>(null);

  readonly form = this.fb.nonNullable.group({
    desde: ['2026-01-01', Validators.required],
    hasta: ['2026-12-31', Validators.required],
  });

  ngOnInit(): void {
    this.consultar();
  }

  consultar(): void {
    this.error.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { desde, hasta } = this.form.getRawValue();
    if (hasta < desde) {
      this.error.set('hasta no puede ser anterior a desde');
      return;
    }

    this.cargando.set(true);
    this.reporteService.conciliacion(desde, hasta).subscribe({
      next: (res) => {
        this.data.set(res);
        this.cargando.set(false);
      },
      error: (err) => {
        this.cargando.set(false);
        this.error.set(mensajeApiError(err, 'No se pudo cargar el reporte'));
      },
    });
  }
}
