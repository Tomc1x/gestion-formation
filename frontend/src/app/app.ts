import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { PageHeaderComponent, HeaderRole, HeaderUser } from './shared/components/page-header/page-header';
import { UiElementsComponent } from './shared/components/ui-elements/ui-elements';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, PageHeaderComponent, UiElementsComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = signal('frontend');
  protected role: HeaderRole = 'REF';
  protected user: HeaderUser = {
    nom: 'Jean Dupont',
    email: 'jean.dupont@gestion-formation.local'
  };
  protected notifications = 1;

  onRoleChange(role: HeaderRole): void {
    this.role = role;
  }

  onLogout(): void {
    console.log('logout');
  }

  onSearch(value: string): void {
    console.log('search', value);
  }
}
