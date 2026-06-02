import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { UiElementsComponent } from '../ui-elements/ui-elements';

export type HeaderRole = 'REF' | 'ADMIN' | 'FORMATEUR' | 'ELEVE';

export interface HeaderUser {
  nom: string;
  email: string;
}

export interface HeaderRoleMeta {
  label: string;
  bg: string;
  couleur: string;
}

const ROLE_META: Record<HeaderRole, HeaderRoleMeta> = {
  REF: { label: 'Référente', bg: 'var(--blue-050)', couleur: 'var(--blue-700)' },
  ADMIN: { label: 'Admin', bg: 'var(--red-bg)', couleur: 'var(--red)' },
  FORMATEUR: { label: 'Formateur', bg: 'var(--grey-card)', couleur: 'var(--ink-2)' },
  ELEVE: { label: 'Élève', bg: 'var(--blue-050)', couleur: 'var(--blue-700)' }
};

@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [FormsModule, UiElementsComponent],
  templateUrl: './page-header.html',
  styleUrl: './page-header.scss'
})
export class PageHeaderComponent {
  @Input() role: HeaderRole = 'REF';
  @Input() user: HeaderUser = { nom: 'Utilisateur', email: 'user@example.com' };
  @Input() notifications = 0;

  @Output() roleChange = new EventEmitter<HeaderRole>();
  @Output() logout = new EventEmitter<void>();
  @Output() search = new EventEmitter<string>();

  switcher = false;
  menu = false;
  searchValue = '';

  readonly roles: HeaderRole[] = ['REF', 'ADMIN', 'FORMATEUR', 'ELEVE'];

  get roleMeta(): HeaderRoleMeta {
    return ROLE_META[this.role];
  }

  toggleSwitcher(): void {
    this.switcher = !this.switcher;
    this.menu = false;
  }

  toggleMenu(): void {
    this.menu = !this.menu;
    this.switcher = false;
  }

  closeOverlays(): void {
    this.menu = false;
    this.switcher = false;
  }

  onRoleSelect(role: HeaderRole): void {
    this.role = role;
    this.roleChange.emit(role);
    this.closeOverlays();
  }

  onSearchChange(value: string): void {
    this.searchValue = value;
    this.search.emit(value);
  }

  onLogoutClick(): void {
    this.logout.emit();
    this.closeOverlays();
  }

  getRoleLabel(role: HeaderRole): string {
    return ROLE_META[role].label;
  }
}
