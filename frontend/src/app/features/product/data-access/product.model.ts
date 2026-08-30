export interface Product {
  id: number;
  sku: string;
  name: string;
  description: string | null;
  unitPrice: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateProductRequest {
  sku: string;
  name: string;
  description?: string;
  unitPrice: number;
  initialQuantity?: number;
}

export interface UpdateProductRequest {
  name: string;
  description?: string;
  unitPrice: number;
}
