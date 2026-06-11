import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { MockCalendarAdapter } from './core/adapters/calendar-mock';
import { BaseCalendarAdapter } from './core/adapters/calendar.adapter';
import { HttpUserAdminAdapter } from './core/adapters/user-admin-http.adapter';
import { BaseUserAdminAdapter } from './core/adapters/user-admin.adapter';
import { HttpCoursAdapter } from './core/adapters/cours-http.adapter';
import { BaseCoursAdapter } from './core/adapters/cours.adapter';
import { HttpFiliereAdapter } from './core/adapters/filiere-http.adapter';
import { BaseFiliereAdapter } from './core/adapters/filiere.adapter';
import { HttpCursusAdapter } from './core/adapters/cursus-http.adapter';
import { BaseCursusAdapter } from './core/adapters/cursus.adapter';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    {provide: BaseCalendarAdapter, useClass: MockCalendarAdapter},
    {provide: BaseUserAdminAdapter, useClass: HttpUserAdminAdapter},
    {provide: BaseCoursAdapter, useClass: HttpCoursAdapter},
    {provide: BaseFiliereAdapter, useClass: HttpFiliereAdapter},
    {provide: BaseCursusAdapter, useClass: HttpCursusAdapter}
  ]
};
