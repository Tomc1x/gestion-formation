import { Component, ChangeDetectionStrategy, inject } from '@angular/core';
import {
  LucideGraduationCap,
  LucideLifeBuoy,
  LucideCalendar,
  LucideUsers,
  LucideDynamicIcon,
  type LucideIconInput,
} from '@lucide/angular';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { SidebarService } from './sidebar.service';

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive, LucideDynamicIcon, LucideGraduationCap, LucideLifeBuoy],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SidebarComponent {
  protected readonly sidebarService = inject(SidebarService);

  protected readonly routes: { path: string; label: string; icon: LucideIconInput }[] = [
    { path: '/app/dashboard', label: 'Tableau de bord', icon: LucideGraduationCap },
    { path: '/app/promotions', label: 'Promotions', icon: LucideUsers },
    { path: '/app/calendrier', label: 'Calendrier', icon: LucideCalendar },
  ];
}
