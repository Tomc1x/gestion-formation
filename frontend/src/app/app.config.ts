import { ApplicationConfig, provideBrowserGlobalErrorListeners, inject } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { HttpElevePlanningAdapter } from './core/adapters/eleve-planning-http.adapter';
import { HttpFormateurPlanningAdapter } from './core/adapters/formateur-planning-http.adapter';
import { BaseCalendarAdapter } from './core/adapters/calendar.adapter';
import { AuthService } from './core/services/auth.service';
import { HttpUserAdminAdapter } from './core/adapters/user-admin-http.adapter';
import { BaseUserAdminAdapter } from './core/adapters/user-admin.adapter';
import { HttpCoursAdapter } from './core/adapters/cours-http.adapter';
import { BaseCoursAdapter } from './core/adapters/cours.adapter';
import { HttpFiliereAdapter } from './core/adapters/filiere-http.adapter';
import { BaseFiliereAdapter } from './core/adapters/filiere.adapter';
import { HttpCursusAdapter } from './core/adapters/cursus-http.adapter';
import { BaseCursusAdapter } from './core/adapters/cursus.adapter';
import { HttpPromotionAdapter } from './core/adapters/promotion-http.adapter';
import { BasePromotionAdapter } from './core/adapters/promotion.adapter';
import { HttpInscriptionAdapter } from './core/adapters/inscription-http.adapter';
import { BaseInscriptionAdapter } from './core/adapters/inscription.adapter';
import { HttpFormateurAdapter } from './core/adapters/formateur-http.adapter';
import { BaseFormateurAdapter } from './core/adapters/formateur.adapter';
import { HttpEleveAdapter } from './core/adapters/eleve-http.adapter';
import { BaseEleveAdapter } from './core/adapters/eleve.adapter';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    {
      provide: BaseCalendarAdapter,
      useFactory: () => {
        const authService = inject(AuthService);
        return authService.currentRole() === 'FORMATEUR'
          ? inject(HttpFormateurPlanningAdapter)
          : inject(HttpElevePlanningAdapter);
      },
    },
    {provide: BaseUserAdminAdapter, useClass: HttpUserAdminAdapter},
    {provide: BaseCoursAdapter, useClass: HttpCoursAdapter},
    {provide: BaseFiliereAdapter, useClass: HttpFiliereAdapter},
    {provide: BaseCursusAdapter, useClass: HttpCursusAdapter},
    {provide: BasePromotionAdapter, useClass: HttpPromotionAdapter},
    {provide: BaseInscriptionAdapter, useClass: HttpInscriptionAdapter},
    {provide: BaseFormateurAdapter, useClass: HttpFormateurAdapter},
    {provide: BaseEleveAdapter, useClass: HttpEleveAdapter}
  ]
};
