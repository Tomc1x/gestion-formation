import { Component, ChangeDetectionStrategy, inject } from '@angular/core';
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
    username: ['', Validators.required],
    password: ['', Validators.required],
    rememberMe: [false],
  });

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
    const { username, password } = this.form.getRawValue();
    this.auth.login({ username, password }).subscribe(() => {
      this.router.navigate(['/app']);
    });
  }
}
