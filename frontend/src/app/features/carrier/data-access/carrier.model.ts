export interface Carrier {
  id: number;
  name: string;
  contactInfo: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CarrierRequest {
  name: string;
  contactInfo?: string;
}
