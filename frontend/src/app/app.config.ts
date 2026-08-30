import { ApplicationConfig, provideBrowserGlobalErrorListeners, isDevMode, provideAppInitializer, inject } from '@angular/core';
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
import { TranslationService } from './core/translation.service';
import { MatPaginatorIntl } from '@angular/material/paginator';
import { AppPaginatorIntl } from './core/app-paginator-intl.service';
import { productFeature } from './features/product/data-access/product.reducer';
import { ProductEffects } from './features/product/data-access/product.effects';
import { inventoryFeature } from './features/inventory/data-access/inventory.reducer';
import { InventoryEffects } from './features/inventory/data-access/inventory.effects';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideAppInitializer(() => {
      const translation = inject(TranslationService);
      return translation.loadAll();
    }),
    provideStore(),
    provideState(shipmentFeature),
    provideState(authFeature),
    provideState(userFeature),
    provideState(productFeature),
    provideState(inventoryFeature),
    provideEffects(ShipmentEffects, AuthEffects, UserEffects, ProductEffects, InventoryEffects),
    provideStoreDevtools({ maxAge: 25, logOnly: !isDevMode() }),
    { provide: MatPaginatorIntl, useClass: AppPaginatorIntl },
  ],
};
