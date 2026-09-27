export type SalesStage = 'NEW' | 'CONTACTED' | 'VISITED' | 'INTERESTED' | 'HOT' | 'SOLD' | 'LOST' | 'AFTER_SALES';
export type MessageDirection = 'INBOUND' | 'OUTBOUND';
export type MessageStatus = 'RECEIVED' | 'DRAFT' | 'QUEUED' | 'SENT' | 'DELIVERED' | 'UNDELIVERED' | 'FAILED';

export interface ProductInterest {
  id: string;
  productId: string;
  productName: string;
  level: 'LOW' | 'MEDIUM' | 'HIGH';
  source: 'CUSTOMER' | 'AI';
  confidence?: number | null;
  reason?: string | null;
}

export interface Customer {
  id: string;
  firstName: string;
  lastName?: string | null;
  displayName: string;
  phone: string;
  email?: string | null;
  preferredChannel: string;
  salesStage: SalesStage;
  firstVisitDate?: string | null;
  firstVisitLocation?: string | null;
  feedback?: string | null;
  budgetMin?: number | null;
  budgetMax?: number | null;
  notes?: string | null;
  lastContactAt?: string | null;
  nextFollowUpAt?: string | null;
  interests: ProductInterest[];
  customerNotes: { id: string; body: string; createdAt: string }[];
}

export interface ConversationSummary {
  id: string;
  customerId: string;
  customerName: string;
  phone: string;
  salesStage: SalesStage;
  channel: string;
  lastMessage: string;
  lastDirection?: MessageDirection | null;
  lastMessageAt?: string | null;
}

export interface Message {
  id: string;
  direction: MessageDirection;
  sender: string;
  receiver: string;
  content: string;
  status: MessageStatus;
  externalMessageId?: string | null;
  errorCode?: string | null;
  sentAt?: string | null;
  receivedAt?: string | null;
  createdAt: string;
}

export interface AiSuggestion {
  id: string;
  messageId: string;
  draftReply: string;
  intent?: string | null;
  sentiment?: string | null;
  detectedProducts: string[];
  followUpRequired: boolean;
  suggestedFollowUpAt?: string | null;
  stageSuggestion?: SalesStage | null;
  summary?: string | null;
  model?: string | null;
  status: 'PENDING' | 'USED' | 'DISMISSED';
  createdAt: string;
}

export interface ConversationDetail {
  conversation: ConversationSummary;
  messages: Message[];
  latestSuggestion?: AiSuggestion | null;
}

export interface FollowUp {
  id: string;
  customerId: string;
  customerName: string;
  type: string;
  dueAt: string;
  status: 'OPEN' | 'COMPLETED' | 'CANCELLED';
  reason?: string | null;
  priority: 'LOW' | 'NORMAL' | 'HIGH';
  aiGenerated: boolean;
  completedAt?: string | null;
}

export interface Dashboard {
  dueToday: number;
  overdue: number;
  openTasks: number;
  newLeads: number;
  hotCustomers: number;
  priorityTasks: FollowUp[];
  recentConversations: ConversationSummary[];
}

export interface Product {
  id: string;
  name: string;
  brand?: string | null;
  model?: string | null;
  price?: number | null;
  warrantyYears?: number | null;
  description?: string | null;
  features?: string | null;
  strengths?: string | null;
  productUrl?: string | null;
  manualUrl?: string | null;
  active: boolean;
}
