import { Component, ChangeDetectionStrategy, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  LucideGraduationCap,
  LucideUser,
  LucideLock,
  LucideArrowRight,
} from '@lucide/angular';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, LucideGraduationCap, LucideUser, LucideLock, LucideArrowRight],
  templateUrl: './login.html',
  styleUrl: './login.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  readonly form = this.fb.nonNullable.group({
    email: ['', Validators.required],
    password: ['', Validators.required],
    rememberMe: [false],
  });

  readonly errorMessage = signal<string | null>(null);

  readonly stats = [
    { value: '4', label: 'Filières' },
    { value: '12', label: 'Cursus actifs' },
    { value: '320+', label: 'Stagiaires' },
  ];

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.errorMessage.set(null);
    const { email, password } = this.form.getRawValue();
    this.auth.login({ email, password }).subscribe({
      next: () => this.router.navigate(['/app']),
      error: (err: HttpErrorResponse) => {
        if (err.status === 401) {
          this.errorMessage.set('Identifiants incorrects, veuillez réessayer.');
        } else if (err.status === 429) {
          this.errorMessage.set('Trop de tentatives. Veuillez patienter avant de réessayer.');
        } else {
          this.errorMessage.set('Une erreur est survenue. Veuillez réessayer.');
        }
      },
    });
  }
}
