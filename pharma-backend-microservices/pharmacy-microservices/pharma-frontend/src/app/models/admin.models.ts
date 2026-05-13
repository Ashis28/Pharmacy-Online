export interface DashboardStats {
  totalOrders: number;
  pendingPrescriptions: number;
  lowStockCount: number;
  monthlyRevenue: number;
}

export interface AuditLog {
  id: number;
  adminEmail: string;
  action: string;
  targetEntity: string;
  targetId: string;
  details: string;
  timestamp: string;
}

export interface NotificationRequest {
  recipientEmail: string;
  subject: string;
  message: string;
  type: string;
}
