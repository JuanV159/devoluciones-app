import { Routes } from '@angular/router';
import { BandejaComponent } from './bandeja/bandeja.component';
import { DetalleSolicitudComponent } from './detalle/detalle.component';
import { SolicitudFormComponent } from './form/form.component';

export const SOLICITUDES_ROUTES: Routes = [
  { path: '', component: BandejaComponent },
  { path: 'nueva', component: SolicitudFormComponent },
  { path: ':id/editar', component: SolicitudFormComponent },
  { path: ':id', component: DetalleSolicitudComponent },
];
