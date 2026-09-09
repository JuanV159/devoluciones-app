import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { ShellComponent } from './shared/layout/shell.component';

export const routes: Routes = [
  {
    path: 'login',
    loadChildren: () => import('./features/auth/auth.routes').then((m) => m.AUTH_ROUTES),
  },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'solicitudes' },
      {
        path: 'solicitudes',
        loadChildren: () =>
          import('./features/solicitudes/solicitudes.routes').then((m) => m.SOLICITUDES_ROUTES),
      },
      {
        path: 'cargas',
        loadChildren: () => import('./features/cargas/cargas.routes').then((m) => m.CARGAS_ROUTES),
      },
      {
        path: 'reportes',
        loadChildren: () =>
          import('./features/reportes/reportes.routes').then((m) => m.REPORTES_ROUTES),
      },
    ],
  },
  { path: '**', redirectTo: 'solicitudes' },
];
