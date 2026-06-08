import { Component, ChangeDetectionStrategy, input, computed } from '@angular/core';
import { Role, ROLE_META, RoleMeta } from '../../../core/services/auth.constants';

@Component({
  selector: 'app-role-badge',
  imports: [],
  templateUrl: './role-badge.html',
  styleUrl: './role-badge.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RoleBadgeComponent {
  role = input.required<Role>();
  size = input<'sm' | 'md'>('md');

  meta = computed<RoleMeta>(() => ROLE_META[this.role()]);
}
