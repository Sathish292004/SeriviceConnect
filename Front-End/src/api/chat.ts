import { gatewayClient } from '@/lib/axios'
import type {
  Conversation,
  Message,
  Quote,
  CreateConversationRequest,
  SendMessageRequest,
  CreateQuoteRequest,
} from '@/types'

const BASE = '/api/v1/bookings'

export const chatApi = {
  /** Get all conversations for the current authenticated user (customer or provider) */
  getConversations: () =>
    gatewayClient.get<Conversation[]>(`${BASE}/conversations`),

  /** Get single conversation details including full message history and quotes */
  getConversation: (id: number) =>
    gatewayClient.get<Conversation>(`${BASE}/conversations/${id}`),

  /** Create or retrieve existing conversation with a provider (CUSTOMER) */
  createConversation: (data: CreateConversationRequest) =>
    gatewayClient.post<Conversation>(`${BASE}/conversations`, data),

  /** Send a message in a conversation (CUSTOMER or PROVIDER). Automatically sanitized server-side. */
  sendMessage: (conversationId: number, data: SendMessageRequest) =>
    gatewayClient.post<Message>(`${BASE}/conversations/${conversationId}/messages`, data),

  /** Get all quotes in a conversation */
  getQuotes: (conversationId: number) =>
    gatewayClient.get<Quote[]>(`${BASE}/conversations/${conversationId}/quotes`),

  /** Create an official quote for a customer (PROVIDER) */
  createQuote: (conversationId: number, data: CreateQuoteRequest) =>
    gatewayClient.post<Quote>(`${BASE}/conversations/${conversationId}/quotes`, data),

  /** Accept a quote (CUSTOMER) */
  acceptQuote: (quoteId: number) =>
    gatewayClient.patch<Quote>(`${BASE}/quotes/${quoteId}/accept`),

  /** Decline a quote (CUSTOMER) */
  declineQuote: (quoteId: number) =>
    gatewayClient.patch<Quote>(`${BASE}/quotes/${quoteId}/decline`),
}
