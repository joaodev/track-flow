import { ApplicationConfig, provideBrowserGlobalErrorListeners, isDevMode } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { provideState, provideStore } from '@ngrx/store';
import { provideEffects } from '@ngrx/effects';
import { provideStoreDevtools } from '@ngrx/store-devtools';
import { shipmentFeature } from './features/shipment/data-access/shipment.reducer';
import { ShipmentEffects } from './features/shipment/data-access/shipment.effects';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authFeature } from './features/auth/data-access/auth.reducer';
import { AuthEffects } from './features/auth/data-access/auth.effects';
import { authInterceptor } from './features/auth/data-access/auth.interceptor';
import { userFeature } from './features/user/data-access/user.reducer';
import { UserEffects } from './features/user/data-access/user.effects';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideStore(),
    provideState(shipmentFeature),
    provideState(authFeature),
    provideState(userFeature),
    provideEffects(ShipmentEffects, AuthEffects, UserEffects),
    provideStoreDevtools({ maxAge: 25, logOnly: !isDevMode() }),
  ],
};
