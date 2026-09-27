import type { AiSuggestion, ConversationDetail, ConversationSummary, Customer, Dashboard, Message, Product } from './types';

const API_URL = (process.env.EXPO_PUBLIC_API_URL ?? 'http://localhost:8080').replace(/\/$/, '');
const TOKEN = process.env.EXPO_PUBLIC_API_BEARER_TOKEN ?? '';

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(init?.headers as Record<string, string> | undefined),
  };
  if (TOKEN) headers.Authorization = `Bearer ${TOKEN}`;

  const response = await fetch(`${API_URL}${path}`, { ...init, headers });
  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `Request failed (${response.status})`);
  }
  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

export const api = {
  dashboard: () => request<Dashboard>('/api/dashboard/today'),
  conversations: () => request<ConversationSummary[]>('/api/conversations'),
  conversation: (id: string) => request<ConversationDetail>(`/api/conversations/${id}`),
  sendMessage: (conversationId: string, content: string, suggestionId?: string) =>
    request<Message>(`/api/conversations/${conversationId}/send`, {
      method: 'POST',
      body: JSON.stringify({ content, suggestionId: suggestionId ?? null }),
    }),
  generateSuggestion: (messageId: string) =>
    request<AiSuggestion>(`/api/messages/${messageId}/ai-suggestion`, { method: 'POST' }),
  customers: (search?: string) => request<Customer[]>(`/api/customers${search ? `?search=${encodeURIComponent(search)}` : ''}`),
  customer: (id: string) => request<Customer>(`/api/customers/${id}`),
  createCustomer: (payload: Record<string, unknown>) =>
    request<Customer>('/api/customers', { method: 'POST', body: JSON.stringify(payload) }),
  updateCustomer: (id: string, payload: Record<string, unknown>) =>
    request<Customer>(`/api/customers/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  addNote: (id: string, body: string) =>
    request(`/api/customers/${id}/notes`, { method: 'POST', body: JSON.stringify({ body }) }),
  products: () => request<Product[]>('/api/products'),
  completeFollowUp: (id: string) => request(`/api/follow-ups/${id}/complete`, { method: 'POST' }),
  assistantQuery: (query: string) =>
    request<{ answer: string }>('/api/assistant/query', { method: 'POST', body: JSON.stringify({ query }) }),
};
