import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Carga } from '../../../core/models/carga.model';
import { CargaService } from '../../../core/services/carga.service';
import { mensajeApiError } from '../../../core/utils/api-error';

@Component({
  selector: 'app-carga-upload',
  standalone: true,
  imports: [RouterLink, DatePipe, DecimalPipe],
  templateUrl: './carga-upload.component.html',
  styleUrl: './carga-upload.component.scss',
})
export class CargaUploadComponent {
  private readonly cargaService = inject(CargaService);

  readonly archivo = signal<File | null>(null);
  readonly cargando = signal(false);
  readonly error = signal<string | null>(null);
  readonly resultado = signal<Carga | null>(null);

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.archivo.set(file);
    this.error.set(null);
    this.resultado.set(null);
  }

  subir(): void {
    const file = this.archivo();
    if (!file) {
      this.error.set('Selecciona un archivo CSV');
      return;
    }
    if (!file.name.toLowerCase().endsWith('.csv')) {
      this.error.set('El archivo debe ser .csv');
      return;
    }

    this.cargando.set(true);
    this.error.set(null);
    this.resultado.set(null);

    this.cargaService.subir(file).subscribe({
      next: (carga) => {
        this.resultado.set(carga);
        this.cargando.set(false);
      },
      error: (err) => {
        this.cargando.set(false);
        this.error.set(mensajeApiError(err, 'No se pudo procesar la carga'));
      },
    });
  }
}
