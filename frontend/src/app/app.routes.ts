import { Routes } from '@angular/router';
import { AuthLayoutComponent } from './layouts/auth-layout/auth-layout';
import { MainLayoutComponent } from './layouts/main-layout/main-layout';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: 'login',
    component: AuthLayoutComponent,
    canActivate: [guestGuard],
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
        canActivate: [roleGuard(['REF'])],
        loadComponent: () => import('./features/promotions/promotions-list/promotions-list').then(m => m.PromotionsListComponent)
      },
      {
        title: 'Promotion Detail',
        path: 'promotions/:id',
        canActivate: [roleGuard(['REF'])],
        loadComponent: () => import('./features/promotions/promotion-detail/promotion-detail').then(m => m.PromotionDetailComponent)
      },
      {
        title: 'Calendrier',
        path: 'calendrier',
        canActivate: [roleGuard(['FORMATEUR', 'ELEVE'])],
        loadComponent: () => import('./features/calendrier/mon-calendrier/mon-calendrier').then(m => m.MonCalendrierComponent)
      },
      {
        title: 'Utilisateurs',
        path: 'admin/utilisateurs',
        canActivate: [roleGuard(['ADMIN'])],
        loadComponent: () => import('./features/administration/utilisateurs/utilisateurs').then(m => m.UtilisateursComponent)
      },
      {
        title: 'Catalogue de cours',
        path: 'admin/cours',
        canActivate: [roleGuard(['REF'])],
        loadComponent: () => import('./features/administration/cours/cours').then(m => m.CoursComponent)
      },
      {
        title: 'Cursus',
        path: 'admin/cursus',
        canActivate: [roleGuard(['REF'])],
        loadComponent: () => import('./features/administration/cursus/cursus').then(m => m.CursusComponent)
      },
      {
        title: 'Détail du cursus',
        path: 'admin/cursus/:id',
        canActivate: [roleGuard(['REF'])],
        loadComponent: () => import('./features/administration/cursus-detail/cursus-detail').then(m => m.CursusDetailComponent)
      },
      {
        title: 'Promotions',
        path: 'admin/promotions',
        canActivate: [roleGuard(['REF'])],
        loadComponent: () => import('./features/administration/promotions/promotions').then(m => m.PromotionsComponent)
      },
      {
        title: 'Promotion',
        path: 'admin/promotions/:id',
        canActivate: [roleGuard(['REF'])],
        loadComponent: () => import('./features/promotions/promotion-detail/promotion-detail').then(m => m.PromotionDetailComponent)
      },
      {
        title: 'Élèves inscrits',
        path: 'cours-planifies/:id/inscrits',
        canActivate: [roleGuard(['FORMATEUR'])],
        loadComponent: () => import('./features/formateur/inscrits/inscrits').then(m => m.InscritsComponent)
      }
    ]
  },
  { path: '**', redirectTo: 'login' }
];
