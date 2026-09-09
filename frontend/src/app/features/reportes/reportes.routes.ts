import { Routes } from '@angular/router';
import { ConciliacionComponent } from './conciliacion/conciliacion.component';

export const REPORTES_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'conciliacion' },
  { path: 'conciliacion', component: ConciliacionComponent },
];
