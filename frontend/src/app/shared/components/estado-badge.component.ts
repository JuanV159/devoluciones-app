import { Component, Input } from '@angular/core';
import { Estado } from '../../core/models/solicitud.model';

@Component({
  selector: 'app-estado-badge',
  standalone: true,
  template: `<span class="badge" [attr.data-estado]="estado">{{ estado }}</span>`,
  styles: [
    `
      .badge {
        display: inline-block;
        padding: 0.2rem 0.55rem;
        border-radius: 999px;
        font-size: 0.75rem;
        font-weight: 600;
        letter-spacing: 0.02em;
        background: #e5e7eb;
        color: #374151;
      }
      .badge[data-estado='BORRADOR'] {
        background: #e5e7eb;
        color: #374151;
      }
      .badge[data-estado='EN_REVISION'] {
        background: #dbeafe;
        color: #1d4ed8;
      }
      .badge[data-estado='APROBADA'] {
        background: #d1fae5;
        color: #047857;
      }
      .badge[data-estado='RECHAZADA'] {
        background: #fee2e2;
        color: #b91c1c;
      }
      .badge[data-estado='PAGADA'] {
        background: #ede9fe;
        color: #6d28d9;
      }
      .badge[data-estado='ANULADA'] {
        background: #f3f4f6;
        color: #6b7280;
      }
    `,
  ],
})
export class EstadoBadgeComponent {
  @Input({ required: true }) estado!: Estado;
}
