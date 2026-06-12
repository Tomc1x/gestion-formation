import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  computed,
  OnInit,
} from '@angular/core';
import { FormsModule, ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import {
  LucideSearch,
  LucideMoreVertical,
  LucideUserPlus,
  LucideChevronDown,
  LucidePencil,
  LucideKeyRound,
  LucideShieldCheck,
  LucideTrash2,
  LucideX,
  LucideMail,
  LucideUserCog,
  LucideCheckCircle, LucideEllipsisVertical, LucideCircleCheckBig,
} from '@lucide/angular';
import { BaseUserAdminAdapter } from '../../../core/adapters/user-admin.adapter';
import {
  UserAdmin,
  BackendRole,
  BACKEND_TO_FRONTEND_ROLE,
  ROLE_FILTER_LABELS,
  BACKEND_ROLE_OPTIONS,
} from '../../../core/models/user.model';
import { AvatarComponent } from '../../../shared/components/avatar/avatar';
import { RoleBadgeComponent } from '../../../shared/components/role-badge/role-badge';
import type { Role } from '../../../core/services/auth.constants';

@Component({
  selector: 'app-utilisateurs',
  imports: [
    FormsModule,
    ReactiveFormsModule,
    LucideSearch,
    LucideMoreVertical,
    LucideUserPlus,
    LucideChevronDown,
    LucidePencil,
    LucideKeyRound,
    LucideShieldCheck,
    LucideTrash2,
    LucideX,
    LucideMail,
    LucideUserCog,
    LucideCheckCircle,
    AvatarComponent,
    RoleBadgeComponent,
    LucideEllipsisVertical,
    LucideCircleCheckBig,
  ],
  templateUrl: './utilisateurs.html',
  styleUrl: './utilisateurs.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UtilisateursComponent implements OnInit {
  private readonly userAdminService = inject(BaseUserAdminAdapter);

  protected readonly roleFilters = ROLE_FILTER_LABELS;
  protected readonly roleOptions = BACKEND_ROLE_OPTIONS;

  protected readonly users = signal<UserAdmin[]>([]);
  protected readonly searchQuery = signal('');
  protected readonly selectedRole = signal<BackendRole | 'all'>('all');
  protected readonly loading = signal(true);
  protected readonly loadError = signal<string | null>(null);
  protected readonly openMenuUid = signal<number | null>(null);

  protected readonly filteredUsers = computed(() => {
    const q = this.searchQuery().toLowerCase();
    const role = this.selectedRole();
    return this.users().filter(u => {
      const matchesRole = role === 'all' || u.role === role;
      const name = `${u.firstName} ${u.lastName}`.toLowerCase();
      return matchesRole && (name.includes(q) || u.email.toLowerCase().includes(q));
    });
  });

  protected readonly showCreateModal = signal(false);
  protected readonly createModalTab = signal<'direct' | 'invite'>('direct');
  protected readonly inviteSent = signal<string | null>(null);
  protected readonly inviteLink = signal<string | null>(null);

  protected readonly inviteForm = new FormGroup({
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    role:  new FormControl<BackendRole | ''>('', { nonNullable: true, validators: Validators.required }),
  });

  protected readonly createForm = new FormGroup({
    firstName: new FormControl('', { nonNullable: true, validators: Validators.required }),
    lastName:  new FormControl('', { nonNullable: true, validators: Validators.required }),
    email:     new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    password:  new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(6)] }),
    role:      new FormControl<BackendRole | ''>('', { nonNullable: true, validators: Validators.required }),
  });

  protected readonly editingUser = signal<UserAdmin | null>(null);

  protected readonly editProfileForm = new FormGroup({
    firstName: new FormControl('', { nonNullable: true, validators: Validators.required }),
    lastName:  new FormControl('', { nonNullable: true, validators: Validators.required }),
    email:     new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
  });

  protected readonly changingRoleUser = signal<UserAdmin | null>(null);

  protected readonly changeRoleForm = new FormGroup({
    role: new FormControl<BackendRole | ''>('', { nonNullable: true, validators: Validators.required }),
  });

  protected readonly resettingPasswordUser = signal<UserAdmin | null>(null);

  protected readonly deletingUser = signal<UserAdmin | null>(null);

  protected readonly resetPasswordForm = new FormGroup({
    newPassword: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(6)] }),
  });

  protected readonly submitting = signal(false);
  protected readonly formError = signal<string | null>(null);

  protected toFrontendRole(role: BackendRole): Role {
    return BACKEND_TO_FRONTEND_ROLE[role] as Role;
  }

  protected fullName(user: UserAdmin): string {
    return `${user.firstName} ${user.lastName}`;
  }

  ngOnInit(): void {
    this.loadUsers();
  }

  private loadUsers(): void {
    this.loading.set(true);
    this.userAdminService.getAll().subscribe({
      next: users => {
        this.users.set(users);
        this.loading.set(false);
      },
      error: () => {
        this.loadError.set('Impossible de charger les utilisateurs.');
        this.loading.set(false);
      },
    });
  }

  protected toggleEnabled(user: UserAdmin): void {
    const call$ = user.enabled
      ? this.userAdminService.disable(user.uid)
      : this.userAdminService.enable(user.uid);

    call$.subscribe({
      next: updated => this.replaceUser(updated),
    });
  }

  protected toggleMenu(uid: number): void {
    this.openMenuUid.update(c => (c === uid ? null : uid));
  }

  protected closeMenu(): void {
    this.openMenuUid.set(null);
  }

  protected openCreateModal(): void {
    this.createForm.reset();
    this.inviteForm.reset();
    this.formError.set(null);
    this.inviteSent.set(null);
    this.inviteLink.set(null);
    this.createModalTab.set('direct');
    this.showCreateModal.set(true);
  }

  protected closeCreateModal(): void {
    this.showCreateModal.set(false);
  }

  protected switchCreateTab(tab: 'direct' | 'invite'): void {
    this.createModalTab.set(tab);
    this.formError.set(null);
    this.inviteSent.set(null);
  }

  protected submitInvite(): void {
    if (this.inviteForm.invalid || this.submitting()) return;
    const v = this.inviteForm.getRawValue();
    if (!v.role) return;

    this.submitting.set(true);
    this.formError.set(null);

    this.userAdminService.invite({ email: v.email, role: v.role as BackendRole }).subscribe({
      next: (link) => {
        this.inviteSent.set(v.email);
        this.inviteLink.set(link);
        this.inviteForm.reset();
        this.submitting.set(false);
      },
      error: err => {
        this.formError.set(
          err.status === 409 ? 'Un compte existe déjà avec cet e-mail.' : 'Impossible d\'envoyer l\'invitation.'
        );
        this.submitting.set(false);
      },
    });
  }

  protected submitCreate(): void {
    if (this.createForm.invalid || this.submitting()) return;
    const v = this.createForm.getRawValue();
    if (!v.role) return;

    this.submitting.set(true);
    this.formError.set(null);

    this.userAdminService.create({
      firstName: v.firstName,
      lastName:  v.lastName,
      email:     v.email,
      password:  v.password,
      role:      v.role as BackendRole,
    }).subscribe({
      next: created => {
        this.users.update(list => [...list, created]);
        this.submitting.set(false);
        this.closeCreateModal();
      },
      error: err => {
        this.formError.set(err.status === 409 ? 'Cet e-mail est déjà utilisé.' : 'Une erreur est survenue.');
        this.submitting.set(false);
      },
    });
  }

  protected openEditModal(user: UserAdmin): void {
    this.closeMenu();
    this.editProfileForm.setValue({
      firstName: user.firstName,
      lastName:  user.lastName,
      email:     user.email,
    });
    this.formError.set(null);
    this.editingUser.set(user);
  }

  protected closeEditModal(): void {
    this.editingUser.set(null);
  }

  protected submitEdit(): void {
    const user = this.editingUser();
    if (!user || this.editProfileForm.invalid || this.submitting()) return;

    this.submitting.set(true);
    this.formError.set(null);

    this.userAdminService.update(user.uid, this.editProfileForm.getRawValue()).subscribe({
      next: updated => {
        this.replaceUser(updated);
        this.submitting.set(false);
        this.closeEditModal();
      },
      error: err => {
        this.formError.set(err.status === 409 ? 'Cet e-mail est déjà utilisé.' : 'Une erreur est survenue.');
        this.submitting.set(false);
      },
    });
  }

  protected openChangeRoleModal(user: UserAdmin): void {
    this.closeMenu();
    this.changeRoleForm.setValue({ role: user.role });
    this.formError.set(null);
    this.changingRoleUser.set(user);
  }

  protected closeChangeRoleModal(): void {
    this.changingRoleUser.set(null);
  }

  protected submitChangeRole(): void {
    const user = this.changingRoleUser();
    if (!user || this.changeRoleForm.invalid || this.submitting()) return;
    const role = this.changeRoleForm.getRawValue().role as BackendRole;

    this.submitting.set(true);
    this.formError.set(null);

    this.userAdminService.changeRole(user.uid, { role }).subscribe({
      next: updated => {
        this.replaceUser(updated);
        this.submitting.set(false);
        this.closeChangeRoleModal();
      },
      error: () => {
        this.formError.set('Une erreur est survenue.');
        this.submitting.set(false);
      },
    });
  }

  // ── Réinitialiser le mot de passe ───────────────────────────────────────────
  protected openResetPasswordModal(user: UserAdmin): void {
    this.closeMenu();
    this.resetPasswordForm.reset();
    this.formError.set(null);
    this.resettingPasswordUser.set(user);
  }

  protected closeResetPasswordModal(): void {
    this.resettingPasswordUser.set(null);
  }

  protected submitResetPassword(): void {
    const user = this.resettingPasswordUser();
    if (!user || this.resetPasswordForm.invalid || this.submitting()) return;

    this.submitting.set(true);
    this.formError.set(null);

    this.userAdminService.changePassword(user.uid, {
      newPassword: this.resetPasswordForm.getRawValue().newPassword,
    }).subscribe({
      next: () => {
        this.submitting.set(false);
        this.closeResetPasswordModal();
      },
      error: () => {
        this.formError.set('Une erreur est survenue.');
        this.submitting.set(false);
      },
    });
  }

  // ── Supprimer ───────────────────────────────────────────────────────────────
  protected openDeleteModal(user: UserAdmin): void {
    this.closeMenu();
    this.formError.set(null);
    this.deletingUser.set(user);
  }

  protected closeDeleteModal(): void {
    this.deletingUser.set(null);
  }

  protected confirmDelete(): void {
    const user = this.deletingUser();
    if (!user || this.submitting()) return;

    this.submitting.set(true);
    this.formError.set(null);

    this.userAdminService.delete(user.uid).subscribe({
      next: () => {
        this.users.update(list => list.filter(u => u.uid !== user.uid));
        this.submitting.set(false);
        this.closeDeleteModal();
      },
      error: () => {
        this.formError.set('Impossible de supprimer cet utilisateur.');
        this.submitting.set(false);
      },
    });
  }

  // ── Utilitaires ─────────────────────────────────────────────────────────────
  private replaceUser(updated: UserAdmin): void {
    this.users.update(list => list.map(u => (u.uid === updated.uid ? updated : u)));
  }
}
