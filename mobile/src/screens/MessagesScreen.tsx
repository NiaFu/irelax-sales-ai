import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { FlatList, Pressable, RefreshControl, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';
import { api } from '../api';
import { Card, ErrorView, Loading, Pill, PrimaryButton } from '../components';
import { theme } from '../theme';
import type { ConversationDetail, ConversationSummary, Message } from '../types';

export function MessagesScreen() {
  const [conversations, setConversations] = useState<ConversationSummary[]>([]);
  const [selected, setSelected] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [refreshing, setRefreshing] = useState(false);

  const load = useCallback(async () => {
    try {
      setError('');
      setConversations(await api.conversations());
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Unable to load messages');
    } finally { setLoading(false); }
  }, []);

  useEffect(() => {
    void load();
    const timer = setInterval(() => void load(), 15000);
    return () => clearInterval(timer);
  }, [load]);

  if (selected) return <ConversationView conversationId={selected} onBack={() => { setSelected(null); void load(); }} />;
  if (loading) return <Loading />;
  if (error && conversations.length === 0) return <ErrorView message={error} onRetry={() => void load()} />;

  return (
    <View style={styles.root}>
      <View style={styles.header}><Text style={styles.title}>Messages</Text><Text style={styles.subtitle}>SMS customer conversations</Text></View>
      <FlatList
        data={conversations}
        keyExtractor={item => item.id}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={async () => { setRefreshing(true); await load(); setRefreshing(false); }} />}
        contentContainerStyle={styles.list}
        ListEmptyComponent={<Card><Text style={styles.muted}>No SMS conversations yet.</Text></Card>}
        renderItem={({ item }) => (
          <Pressable onPress={() => setSelected(item.id)}>
            <Card>
              <View style={styles.rowBetween}>
                <Text style={styles.customer}>{item.customerName}</Text>
                <Pill text={item.salesStage} />
              </View>
              <Text numberOfLines={2} style={styles.messagePreview}>{item.lastMessage || 'No messages yet'}</Text>
              <View style={styles.rowBetween}>
                <Text style={styles.muted}>{item.phone}</Text>
                <Text style={styles.muted}>{item.lastMessageAt ? relativeTime(item.lastMessageAt) : ''}</Text>
              </View>
            </Card>
          </Pressable>
        )}
      />
    </View>
  );
}

function ConversationView({ conversationId, onBack }: { conversationId: string; onBack: () => void }) {
  const [detail, setDetail] = useState<ConversationDetail | null>(null);
  const [error, setError] = useState('');
  const [draft, setDraft] = useState('');
  const [busy, setBusy] = useState(false);

  const load = useCallback(async () => {
    try {
      setError('');
      const result = await api.conversation(conversationId);
      setDetail(result);
      if (result.latestSuggestion?.status === 'PENDING') setDraft(result.latestSuggestion.draftReply);
    } catch (e) { setError(e instanceof Error ? e.message : 'Unable to load conversation'); }
  }, [conversationId]);

  useEffect(() => { void load(); }, [load]);

  const latestInbound = useMemo(() => detail ? [...detail.messages].reverse().find(m => m.direction === 'INBOUND') : undefined, [detail]);

  if (!detail && !error) return <Loading />;
  if (!detail) return <ErrorView message={error} onRetry={() => void load()} />;

  const regenerate = async () => {
    if (!latestInbound) return;
    setBusy(true);
    try {
      const suggestion = await api.generateSuggestion(latestInbound.id);
      setDraft(suggestion.draftReply);
      await load();
      setDraft(suggestion.draftReply);
    } finally { setBusy(false); }
  };

  const send = async () => {
    if (!draft.trim()) return;
    setBusy(true);
    try {
      await api.sendMessage(conversationId, draft.trim(), detail.latestSuggestion?.id);
      setDraft('');
      await load();
      setDraft('');
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Unable to send message');
    } finally { setBusy(false); }
  };

  return (
    <View style={styles.root}>
      <View style={styles.conversationHeader}>
        <Pressable onPress={onBack}><Text style={styles.back}>Back</Text></Pressable>
        <View style={{ flex: 1 }}><Text style={styles.customer}>{detail.conversation.customerName}</Text><Text style={styles.muted}>{detail.conversation.phone}</Text></View>
        <Pill text={detail.conversation.salesStage} />
      </View>
      <ScrollView style={{ flex: 1 }} contentContainerStyle={styles.messages}>
        {detail.messages.map(message => <Bubble key={message.id} message={message} />)}
        {detail.latestSuggestion ? (
          <Card>
            <View style={styles.rowBetween}><Text style={styles.aiTitle}>AI suggestion</Text><Pill text={detail.latestSuggestion.intent || 'DRAFT'} /></View>
            {detail.latestSuggestion.summary ? <Text style={styles.muted}>{detail.latestSuggestion.summary}</Text> : null}
            {detail.latestSuggestion.detectedProducts.length ? <Text style={styles.muted}>Products: {detail.latestSuggestion.detectedProducts.join(', ')}</Text> : null}
          </Card>
        ) : null}
      </ScrollView>
      <View style={styles.composer}>
        {error ? <Text style={styles.error}>{error}</Text> : null}
        <TextInput
          value={draft}
          onChangeText={setDraft}
          multiline
          placeholder="Write a reply or generate an AI draft…"
          placeholderTextColor={theme.muted}
          style={styles.input}
        />
        <View style={styles.actions}>
          <Pressable disabled={!latestInbound || busy} onPress={() => void regenerate()} style={styles.secondaryButton}><Text style={styles.secondaryText}>{busy ? 'Working…' : 'AI draft'}</Text></Pressable>
          <View style={{ flex: 1 }}><PrimaryButton title={busy ? 'Sending…' : 'Send'} disabled={!draft.trim() || busy} onPress={() => void send()} /></View>
        </View>
      </View>
    </View>
  );
}

function Bubble({ message }: { message: Message }) {
  const outgoing = message.direction === 'OUTBOUND';
  return (
    <View style={[styles.bubble, outgoing ? styles.outgoing : styles.incoming]}>
      <Text style={styles.bubbleText}>{message.content}</Text>
      <Text style={styles.bubbleMeta}>{message.status} · {relativeTime(message.receivedAt || message.sentAt || message.createdAt)}</Text>
    </View>
  );
}

function relativeTime(value: string) {
  const date = new Date(value);
  const mins = Math.max(0, Math.round((Date.now() - date.getTime()) / 60000));
  if (mins < 1) return 'now';
  if (mins < 60) return `${mins}m`;
  const hours = Math.floor(mins / 60);
  if (hours < 24) return `${hours}h`;
  return `${Math.floor(hours / 24)}d`;
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: theme.background },
  header: { padding: 16, paddingBottom: 8 },
  title: { fontSize: 30, fontWeight: '800', color: theme.text },
  subtitle: { color: theme.muted },
  list: { padding: 16, paddingTop: 8, gap: 10, paddingBottom: 40 },
  rowBetween: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', gap: 10 },
  customer: { fontSize: 16, fontWeight: '700', color: theme.text },
  messagePreview: { color: theme.text, lineHeight: 20 },
  muted: { color: theme.muted, fontSize: 12 },
  conversationHeader: { paddingHorizontal: 14, paddingVertical: 12, borderBottomWidth: 1, borderColor: theme.border, backgroundColor: theme.surface, flexDirection: 'row', alignItems: 'center', gap: 12 },
  back: { color: theme.primary, fontWeight: '700' },
  messages: { padding: 14, gap: 10, paddingBottom: 24 },
  bubble: { maxWidth: '84%', padding: 11, borderRadius: 14, gap: 5 },
  incoming: { alignSelf: 'flex-start', backgroundColor: theme.surface, borderWidth: 1, borderColor: theme.border },
  outgoing: { alignSelf: 'flex-end', backgroundColor: theme.primarySoft },
  bubbleText: { color: theme.text, lineHeight: 20 },
  bubbleMeta: { color: theme.muted, fontSize: 10 },
  aiTitle: { fontWeight: '800', color: theme.primary },
  composer: { backgroundColor: theme.surface, borderTopWidth: 1, borderColor: theme.border, padding: 12, gap: 8 },
  input: { minHeight: 76, maxHeight: 150, borderWidth: 1, borderColor: theme.border, borderRadius: 12, padding: 11, color: theme.text, textAlignVertical: 'top' },
  actions: { flexDirection: 'row', gap: 8, alignItems: 'center' },
  secondaryButton: { paddingVertical: 12, paddingHorizontal: 14, borderRadius: 10, borderWidth: 1, borderColor: theme.primary },
  secondaryText: { color: theme.primary, fontWeight: '700' },
  error: { color: theme.danger, fontSize: 12 },
});
