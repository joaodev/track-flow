import { createEntityAdapter, EntityState } from '@ngrx/entity';
import { Product } from './product.model';
import { createFeature, createReducer, on } from '@ngrx/store';
import { ProductActions } from './product.actions';

export interface ProductState extends EntityState<Product> {
  loading: boolean;
  error: string | null;
}

export const productAdapter = createEntityAdapter<Product>();

export const initialState: ProductState = productAdapter.getInitialState({
  loading: false,
  error: null,
});

export const productFeature = createFeature({
  name: 'product',
  reducer: createReducer(
    initialState,

    on(
      ProductActions.loadProducts,
      ProductActions.createProduct,
      ProductActions.updateProduct,
      ProductActions.setActive,
      (state): ProductState => ({ ...state, loading: true, error: null }),
    ),

    on(ProductActions.loadProductsSuccess, (state, { products }): ProductState =>
      productAdapter.setAll(products, { ...state, loading: false }),
    ),

    on(ProductActions.createProductSuccess, (state, { product }): ProductState =>
      productAdapter.addOne(product, { ...state, loading: false }),
    ),

    on(
      ProductActions.updateProductSuccess,
      ProductActions.setActiveSuccess,
      (state, { product }): ProductState =>
        productAdapter.upsertOne(product, { ...state, loading: false }),
    ),

    on(ProductActions.deleteProduct, (state): ProductState => ({
      ...state, loading: true
    })),

    on(ProductActions.deleteProductSuccess, (state, { id }): ProductState =>
      productAdapter.removeOne(id, { ...state, loading: false }),
    ),

    on(
      ProductActions.loadProductsFailure,
      ProductActions.createProductFailure,
      ProductActions.updateProductFailure,
      ProductActions.deleteProductFailure,
      ProductActions.setActiveFailure,
      (state, { error }): ProductState => ({
        ...state,
        loading: false,
        error,
      }),
    ),
  ),
});
