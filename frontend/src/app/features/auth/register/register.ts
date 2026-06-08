import {
  Component,
  ChangeDetectionStrategy,
  inject,
  signal,
  OnInit,
} from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import {
  ReactiveFormsModule,
  FormGroup,
  FormControl,
  Validators,
  AbstractControl,
  ValidationErrors,
} from '@angular/forms';
import {
  LucideGraduationCap,
  LucideUser,
  LucideLock,
  LucideCheckCircle,
  LucideAlertTriangle,
  LucideArrowRight,
  LucideEye,
  LucideEyeOff, LucideTriangleAlert, LucideCircleCheckBig,
} from '@lucide/angular';

function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const pwd = group.get('password')?.value ?? '';
  const confirm = group.get('confirmPassword')?.value ?? '';
  return pwd === confirm ? null : { mismatch: true };
}

interface InvitationPreview {
  email: string;
  role: string;
}

@Component({
  selector: 'app-register',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    LucideGraduationCap,
    LucideUser,
    LucideLock,
    LucideCheckCircle,
    LucideAlertTriangle,
    LucideArrowRight,
    LucideEye,
    LucideEyeOff,
    LucideTriangleAlert,
    LucideCircleCheckBig,
  ],
  templateUrl: './register.html',
  styleUrl: './register.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegisterComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly http = inject(HttpClient);

  protected readonly status = signal<'loading' | 'invalid' | 'ready' | 'submitting' | 'success'>('loading');
  protected readonly invitedEmail = signal('');
  protected readonly token = signal('');
  protected readonly apiError = signal<string | null>(null);
  protected readonly showPassword = signal(false);
  protected readonly showConfirm = signal(false);

  protected readonly form = new FormGroup(
    {
      firstName:       new FormControl('', { nonNullable: true, validators: Validators.required }),
      lastName:        new FormControl('', { nonNullable: true, validators: Validators.required }),
      password:        new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(6)] }),
      confirmPassword: new FormControl('', { nonNullable: true, validators: Validators.required }),
    },
    { validators: passwordsMatch }
  );

  ngOnInit(): void {
    const t = this.route.snapshot.queryParamMap.get('token') ?? '';
    if (!t) {
      this.status.set('invalid');
      return;
    }
    this.token.set(t);

    this.http.get<InvitationPreview>(`/api/auth/invite/preview?token=${encodeURIComponent(t)}`).subscribe({
      next: preview => {
        this.invitedEmail.set(preview.email);
        this.status.set('ready');
      },
      error: () => this.status.set('invalid'),
    });
  }

  protected toggleShowPassword(): void {
    this.showPassword.update(v => !v);
  }

  protected toggleShowConfirm(): void {
    this.showConfirm.update(v => !v);
  }

  protected onSubmit(): void {
    if (this.form.invalid || this.status() !== 'ready') {
      this.form.markAllAsTouched();
      return;
    }

    this.status.set('submitting');
    this.apiError.set(null);
    const { firstName, lastName, password } = this.form.getRawValue();

    this.http.post('/api/auth/register-invitation', {
      token: this.token(),
      firstName,
      lastName,
      password,
    }).subscribe({
      next: () => {
        this.status.set('success');
        setTimeout(() => this.router.navigate(['/login']), 3000);
      },
      error: err => {
        this.apiError.set(
          err.status === 400 ? (err.error?.message ?? 'Ce lien est invalide ou expiré.')
            : err.status === 409 ? 'Un compte existe déjà avec cette adresse e-mail.'
            : 'Une erreur est survenue. Veuillez réessayer.'
        );
        this.status.set('ready');
      },
    });
  }
}
