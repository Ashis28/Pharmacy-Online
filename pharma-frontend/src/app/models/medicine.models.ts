export interface Medicine {
  id?: number;
  name: string;
  description?: string;
  price: number;
  stockQuantity: number;
  requiresPrescription: boolean;
  expiryDate?: string;
  categoryId?: number | null;
  categoryName?: string;
}

export interface Category {
  id?: number;
  name: string;
  description?: string;
}

export interface Prescription {
  id?: number;
  customerId: number;
  customerEmail?: string;
  imageUrl: string;
  status?: 'PENDING' | 'APPROVED' | 'REJECTED';
  rejectionReason?: string;
  uploadedAt?: string;
}

export interface PrescriptionRejectRequest {
  reason: string;
}
