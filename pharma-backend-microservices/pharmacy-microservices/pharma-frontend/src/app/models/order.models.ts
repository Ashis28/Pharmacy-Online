export interface OrderItem {
  id?: number;
  medicineId: number;
  medicineName?: string;
  quantity: number;
  unitPrice?: number;
}

export interface Order {
  id?: number;
  customerId: number;
  customerEmail?: string;
  status?: OrderStatus;
  totalAmount?: number;
  deliveryAddress: string;
  createdAt?: string;
  items: OrderItem[];
}

export type OrderStatus = 'PENDING' | 'PAID' | 'PACKED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';

export interface PlaceOrderRequest {
  customerId: number;
  deliveryAddress: string;
  items: { medicineId: number; quantity: number }[];
}

export interface UpdateOrderStatusRequest {
  status: OrderStatus;
}

export interface RevenueResponse {
  revenue: number;
}

export interface CartItem {
  medicine: import('./medicine.models').Medicine;
  quantity: number;
}
