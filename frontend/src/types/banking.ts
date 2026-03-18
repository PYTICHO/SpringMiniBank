export type CreateCardResponse = {
  cardNumber: string;
  holderName: string;
  expireDate: string;
  type: string;
  status: string;
  createdAt: string;
  accountId: number;
};

export type TransactionRequest = {
  amount: number;
  currency: string;
  type: string;
  description?: string;
  to: string;
};

export type TransactionResponse = {
  amount: number;
  currency: string;
  type: string;
  status: string;
  description?: string;
  created_at: string;
  fromAccountId: number | null;
  toAccountId: number | null;
};
