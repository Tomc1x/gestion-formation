import { Component, ChangeDetectionStrategy, computed, inject } from '@angular/core';
import {
  LucideGraduationCap,
  LucideLifeBuoy,
  LucideCalendar,
  LucideUsers,
  LucideBookOpen,
  LucideListTree,
  LucideDynamicIcon,
  type LucideIconInput,
} from '@lucide/angular';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { SidebarService } from './sidebar.service';
import { AuthService } from '../../../core/services/auth.service';
import { Role } from '../../../core/services/auth.constants';

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive, LucideDynamicIcon, LucideGraduationCap, LucideLifeBuoy],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SidebarComponent {
  protected readonly sidebarService = inject(SidebarService);
  private readonly authService = inject(AuthService);

  protected readonly routes: { path: string; label: string; icon: LucideIconInput; roles?: Role[] }[] = [
    { path: '/app/dashboard', label: 'Tableau de bord', icon: LucideGraduationCap },
    { path: '/app/promotions', label: 'Promotions', icon: LucideUsers },
    { path: '/app/calendrier', label: 'Calendrier', icon: LucideCalendar },
    { path:'/app/admin/utilisateurs', label: 'Utilisateurs', icon: LucideUsers, roles: ['ADMIN'] },
    { path:'/app/admin/cours', label: 'Catalogue de cours', icon: LucideBookOpen, roles: ['REF'] },
    { path:'/app/admin/cursus', label: 'Cursus', icon: LucideListTree, roles: ['REF'] },
  ];

  protected readonly visibleRoutes = computed(() =>
    this.routes.filter(route => !route.roles || route.roles.includes(this.authService.currentRole()))
  );
}
