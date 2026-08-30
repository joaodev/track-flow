import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, map, mergeMap, of } from 'rxjs';
import { ProductService } from './product.service';
import { ProductActions } from './product.actions';
import { extractErrorCode } from '../../../core/http-error.utils';

@Injectable()
export class ProductEffects {
  private actions$ = inject(Actions);
  private productService = inject(ProductService);

  loadProducts$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ProductActions.loadProducts),
      mergeMap(() =>
        this.productService.getAll().pipe(
          map((products) => ProductActions.loadProductsSuccess({ products })),
          catchError((error) =>
            of(ProductActions.loadProductsFailure({ error: extractErrorCode(error) })),
          ),
        ),
      ),
    ),
  );

  createProduct$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ProductActions.createProduct),
      mergeMap(({ request }) =>
        this.productService.create(request).pipe(
          map((product) => ProductActions.createProductSuccess({ product })),
          catchError((error) =>
            of(ProductActions.createProductFailure({ error: extractErrorCode(error) })),
          ),
        ),
      ),
    ),
  );

  updateProduct$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ProductActions.updateProduct),
      mergeMap(({ id, request }) =>
        this.productService.update(id, request).pipe(
          map((product) => ProductActions.updateProductSuccess({ product })),
          catchError((error) =>
            of(ProductActions.updateProductFailure({ error: extractErrorCode(error) })),
          ),
        ),
      ),
    ),
  );

  setActive$ = createEffect(() =>
    this.actions$.pipe(
      ofType(ProductActions.setActive),
      mergeMap(({ id, active }) =>
        (active ? this.productService.activate(id) : this.productService.deactivate(id)).pipe(
          map((product) => ProductActions.setActiveSuccess({ product })),
          catchError((error) =>
            of(ProductActions.setActiveFailure({ error: extractErrorCode(error) })),
          ),
        ),
      ),
    ),
  );
}
