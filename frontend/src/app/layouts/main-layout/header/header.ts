import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
} from '@angular/core';
import { Router } from '@angular/router';
import {
  LucideMenu,
  LucideSearch,
  LucideBell,
  LucideChevronDown,
  LucideUser,
  LucideSettings,
  LucideLogOut,
} from '@lucide/angular';
import { SidebarService } from '../sidebar/sidebar.service';
import { AuthService } from '../../../core/services/auth.service';
import { RoleBadgeComponent } from '../../../shared/components/role-badge/role-badge';

@Component({
  selector: 'app-header',
  imports: [
    LucideMenu,
    LucideSearch,
    LucideBell,
    LucideChevronDown,
    LucideUser,
    LucideSettings,
    LucideLogOut,
    RoleBadgeComponent,
  ],
  templateUrl: './header.html',
  styleUrl: './header.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HeaderComponent {
  protected readonly sidebarService = inject(SidebarService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly menuOpen = signal(false);

  protected readonly currentUser = computed(() => this.authService.currentUser());
  protected readonly currentRole = computed(() => this.authService.currentRole());
  protected readonly currentRoleMeta = computed(() => this.authService.currentRoleMeta());

  protected logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
