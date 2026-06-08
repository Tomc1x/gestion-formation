import { Routes } from '@angular/router';
import { AuthLayoutComponent } from './layouts/auth-layout/auth-layout';
import { MainLayoutComponent } from './layouts/main-layout/main-layout';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: 'login',
    component: AuthLayoutComponent,
    children: [
      {
        path: '',
        loadComponent: () => import('./features/auth/login/login').then(m => m.LoginComponent)
      }
    ]
  },
  {
    path: 'register',
    component: AuthLayoutComponent,
    children: [
      {
        title: 'Créer votre compte',
        path: '',
        loadComponent: () => import('./features/auth/register/register').then(m => m.RegisterComponent)
      }
    ]
  },
  {
    path: 'app',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        title: 'Tableau de bord',
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard').then(m => m.DashboardComponent)
      },
      {
        title: 'Promotions',
        path: 'promotions',
        loadComponent: () => import('./features/promotions/promotions-list/promotions-list').then(m => m.PromotionsListComponent)
      },
      {
        title: 'Promotion Detail',
        path: 'promotions/:id',
        loadComponent: () => import('./features/promotions/promotion-detail/promotion-detail').then(m => m.PromotionDetailComponent)
      },
      {
        title: 'Calendrier',
        path: 'calendrier',
        loadComponent: () => import('./features/calendrier/mon-calendrier/mon-calendrier').then(m => m.MonCalendrierComponent)
      },
      {
        title: 'Utilisateurs',
        path: 'admin/utilisateurs',
        loadComponent: () => import('./features/administration/utilisateurs/utilisateurs').then(m => m.UtilisateursComponent)
      }
    ]
  },
  { path: '**', redirectTo: 'login' }
];
