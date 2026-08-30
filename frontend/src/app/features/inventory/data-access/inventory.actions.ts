import { createActionGroup, emptyProps, props } from '@ngrx/store';
import {
  AdjustStockRequest,
  Inventory,
  LowStockEvent,
  UpdateThresholdRequest,
} from './inventory.model';

export const InventoryActions = createActionGroup({
  source: 'Inventory',
  events: {
    'Load Inventory': emptyProps(),
    'Load Inventory Success': props<{ inventory: Inventory[] }>(),
    'Load Inventory Failure': props<{ error: string }>(),

    'Adjust Stock': props<{ productId: number; request: AdjustStockRequest }>(),
    'Adjust Stock Success': props<{ inventory: Inventory }>(),
    'Adjust Stock Failure': props<{ error: string }>(),

    'Update Threshold': props<{ productId: number; request: UpdateThresholdRequest }>(),
    'Update Threshold Success': props<{ inventory: Inventory }>(),
    'Update Threshold Failure': props<{ error: string }>(),

    'Low Stock Alert Received': props<{ event: LowStockEvent }>(),
  },
});
