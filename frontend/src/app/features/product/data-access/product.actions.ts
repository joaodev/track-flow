import { createActionGroup, emptyProps, props } from '@ngrx/store';
import { CreateProductRequest, Product, UpdateProductRequest } from './product.model';

export const ProductActions = createActionGroup({
  source: 'Product',
  events: {
    'Load Products': emptyProps(),
    'Load Products Success': props<{ products: Product[] }>(),
    'Load Products Failure': props<{ error: string }>(),

    'Create Product': props<{ request: CreateProductRequest }>(),
    'Create Product Success': props<{ product: Product }>(),
    'Create Product Failure': props<{ error: string }>(),

    'Update Product': props<{ id: number; request: UpdateProductRequest }>(),
    'Update Product Success': props<{ product: Product }>(),
    'Update Product Failure': props<{ error: string }>(),

    'Set Active': props<{ id: number; active: boolean }>(),
    'Set Active Success': props<{ product: Product }>(),
    'Set Active Failure': props<{ error: string }>(),

    'Delete Product': props<{ id: number }>(),
    'Delete Product Success': props<{ id: number }>(),
    'Delete Product Failure': props<{ error: string }>(),
  },
});
