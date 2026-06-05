import { Component, ChangeDetectionStrategy, inject } from '@angular/core';
import {
  LucideGraduationCap,
  LucideLifeBuoy,
  LucideCalendar,
  LucideUsers,
  LucideDynamicIcon,
  } from '@lucide/angular';
import { RouterLink, RouterLinkActive } from '@angular/router';


@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive, LucideDynamicIcon,LucideGraduationCap, LucideLifeBuoy, LucideCalendar, LucideUsers],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SidebarComponent {
  protected readonly routes: {path: string, label: string, icon: any}[]= [
    {
      path: '/app/dashboard',
      label: 'Tableau de bord',
      icon: LucideGraduationCap
    },
    {
      path: '/app/promotions',
      label: 'Promotions',
      icon: LucideUsers
    },
    {
      path: '/app/Calendrier',
      label: 'Calendrier',
      icon: LucideCalendar
    },
  ]
}
