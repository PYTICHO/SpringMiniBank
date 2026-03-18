import { apiClient } from "./client";
import type { CreateCardResponse, TransactionRequest, TransactionResponse } from "../types/banking";

export async function createCard(accessToken: string) {
  const response = await apiClient.post<CreateCardResponse>(
    "/api/banking/create_card",
    {},
    {
      headers: {
        Authorization: `Bearer ${accessToken}`
      }
    }
  );

  return response.data;
}

export async function makeTransaction(accessToken: string, payload: TransactionRequest) {
  const response = await apiClient.post<TransactionResponse>(
    "/api/banking/make_transaction",
    payload,
    {
      headers: {
        Authorization: `Bearer ${accessToken}`
      }
    }
  );

  return response.data;
}
