import React, { useCallback, useEffect, useState } from 'react';
import { RefreshControl, ScrollView, StyleSheet, Text, View } from 'react-native';
import { api } from '../api';
import { Card, ErrorView, Loading, Pill, PrimaryButton, SectionTitle } from '../components';
import { theme } from '../theme';
import type { Dashboard } from '../types';

export function TodayScreen() {
  const [data, setData] = useState<Dashboard | null>(null);
  const [error, setError] = useState('');
  const [refreshing, setRefreshing] = useState(false);

  const load = useCallback(async () => {
    try {
      setError('');
      setData(await api.dashboard());
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Unable to load dashboard');
    }
  }, []);

  useEffect(() => { void load(); }, [load]);

  const refresh = async () => {
    setRefreshing(true);
    await load();
    setRefreshing(false);
  };

  if (!data && !error) return <Loading />;
  if (!data) return <ErrorView message={error} onRetry={() => void load()} />;

  return (
    <ScrollView style={styles.root} contentContainerStyle={styles.content} refreshControl={<RefreshControl refreshing={refreshing} onRefresh={refresh} />}>
      <Text style={styles.title}>Today</Text>
      <Text style={styles.subtitle}>Sales follow-ups and customer activity</Text>

      <View style={styles.metrics}>
        <Metric value={data.dueToday} label="Due today" />
        <Metric value={data.overdue} label="Overdue" danger={data.overdue > 0} />
        <Metric value={data.hotCustomers} label="Hot" />
        <Metric value={data.newLeads} label="New leads" />
      </View>

      <SectionTitle>Priority follow-ups</SectionTitle>
      {data.priorityTasks.length === 0 ? <Card><Text style={styles.muted}>No open follow-ups.</Text></Card> : data.priorityTasks.map(task => (
        <Card key={task.id}>
          <View style={styles.rowBetween}>
            <Text style={styles.customer}>{task.customerName}</Text>
            <Pill text={task.priority} />
          </View>
          <Text style={styles.body}>{task.reason || task.type.replaceAll('_', ' ')}</Text>
          <Text style={styles.muted}>{formatDate(task.dueAt)}{task.aiGenerated ? ' · AI suggested' : ''}</Text>
          <PrimaryButton title="Mark complete" onPress={async () => { await api.completeFollowUp(task.id); await load(); }} />
        </Card>
      ))}

      <SectionTitle>Recent conversations</SectionTitle>
      {data.recentConversations.map(item => (
        <Card key={item.id}>
          <View style={styles.rowBetween}><Text style={styles.customer}>{item.customerName}</Text><Pill text={item.salesStage} /></View>
          <Text style={styles.body} numberOfLines={2}>{item.lastMessage || 'No messages yet'}</Text>
          <Text style={styles.muted}>{item.lastMessageAt ? formatDate(item.lastMessageAt) : ''}</Text>
        </Card>
      ))}
    </ScrollView>
  );
}

function Metric({ value, label, danger }: { value: number; label: string; danger?: boolean }) {
  return <View style={styles.metric}><Text style={[styles.metricValue, danger && styles.danger]}>{value}</Text><Text style={styles.metricLabel}>{label}</Text></View>;
}

function formatDate(value: string) {
  return new Date(value).toLocaleString();
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: theme.background },
  content: { padding: 16, gap: 10, paddingBottom: 40 },
  title: { fontSize: 30, fontWeight: '800', color: theme.text },
  subtitle: { color: theme.muted, marginBottom: 4 },
  metrics: { flexDirection: 'row', flexWrap: 'wrap', gap: 10 },
  metric: { backgroundColor: theme.surface, borderWidth: 1, borderColor: theme.border, borderRadius: 12, padding: 12, minWidth: '47%', flexGrow: 1 },
  metricValue: { fontSize: 26, fontWeight: '800', color: theme.primary },
  metricLabel: { color: theme.muted, marginTop: 2 },
  danger: { color: theme.danger },
  rowBetween: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', gap: 10 },
  customer: { fontSize: 16, fontWeight: '700', color: theme.text, flexShrink: 1 },
  body: { color: theme.text, lineHeight: 20 },
  muted: { color: theme.muted, fontSize: 13 },
});
