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
    ],
  },
  { path: '**', redirectTo: 'solicitudes' },
];
