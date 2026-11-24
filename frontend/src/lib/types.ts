export interface ApiResponse<T> {
  code: number;
  message: string;
  result: T;
  timestamp: string;
}

export interface CreditApplication {
  applicationId: string;
  decision: "APPROVED" | "REJECTED";
  reasonCode: string;
  policyVersion: string;
  monthlySurplus: number;
  approvedLimit: number;
  currency: "SGD";
  assessedAt: string;
}

export interface CreditAccount {
  accountId: string;
  totalLimit: number;
  usedLimit: number;
  availableLimit: number;
  currency: "SGD";
  status: "ACTIVE";
}

export interface Instalment {
  id: string;
  sequenceNo: number;
  amount: number;
  dueDate: string;
  status: "UNPAID" | "PAID";
  paidAt: string | null;
}

export interface Drawdown {
  drawdownId: string;
  originalAmount: number;
  outstandingPrincipal: number;
  availableLimit: number;
  status: "ACTIVE" | "PAID_OFF";
  createdAt: string;
}

export interface DrawdownDetail extends Drawdown {
  instalments: Instalment[];
}

export interface Repayment {
  repaymentId: string;
  paidAmount: number;
  remainingDrawdownPrincipal: number;
  availableLimit: number;
  instalmentStatus: "PAID";
}

export interface RepaymentHistory {
  id: string;
  instalmentId: string;
  amount: number;
  status: "SUCCESS";
  paidAt: string;
}

export interface ApplicationInput {
  monthlyNetIncome: number;
  monthlyLivingExpenses: number;
  monthlyDebtPayments: number;
  currency: "SGD";
}
