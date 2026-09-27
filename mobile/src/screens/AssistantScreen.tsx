import React, { useState } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';
import { api } from '../api';
import { Card, PrimaryButton } from '../components';
import { theme } from '../theme';

const suggestions = [
  'Who should I follow up today?',
  'Show HOT customers',
  'Who has not replied for 7 days?',
];

export function AssistantScreen() {
  const [query, setQuery] = useState('');
  const [answer, setAnswer] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const ask = async (value = query) => {
    if (!value.trim()) return;
    setBusy(true); setError('');
    try { setAnswer((await api.assistantQuery(value.trim())).answer); }
    catch (e) { setError(e instanceof Error ? e.message : 'Unable to query assistant'); }
    finally { setBusy(false); }
  };

  return <ScrollView style={styles.root} contentContainerStyle={styles.content}>
    <Text style={styles.title}>AI Assistant</Text>
    <Text style={styles.subtitle}>CRM-aware sales queries. Customer messages remain draft-only unless you press Send.</Text>

    <Card>
      <TextInput value={query} onChangeText={setQuery} multiline placeholder="Ask about follow-ups, customers or products…" placeholderTextColor={theme.muted} style={styles.input} />
      <PrimaryButton title={busy ? 'Checking…' : 'Ask'} disabled={busy || !query.trim()} onPress={() => void ask()} />
    </Card>

    <View style={styles.chips}>{suggestions.map(item => <Pressable key={item} onPress={() => { setQuery(item); void ask(item); }} style={styles.chip}><Text style={styles.chipText}>{item}</Text></Pressable>)}</View>

    {error ? <Text style={styles.error}>{error}</Text> : null}
    {answer ? <Card><Text style={styles.answer}>{answer}</Text></Card> : null}

    <Card>
      <Text style={styles.noteTitle}>V1 assistant scope</Text>
      <Text style={styles.note}>This screen queries structured CRM data. Message drafting uses the richer OpenAI context inside each customer conversation.</Text>
    </Card>
  </ScrollView>;
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: theme.background },
  content: { padding: 16, gap: 12, paddingBottom: 50 },
  title: { fontSize: 30, fontWeight: '800', color: theme.text },
  subtitle: { color: theme.muted, lineHeight: 20 },
  input: { minHeight: 90, borderWidth: 1, borderColor: theme.border, borderRadius: 10, padding: 10, color: theme.text, textAlignVertical: 'top' },
  chips: { gap: 8 },
  chip: { backgroundColor: theme.primarySoft, borderRadius: 10, padding: 11 },
  chipText: { color: theme.primary, fontWeight: '700' },
  answer: { color: theme.text, lineHeight: 22 },
  error: { color: theme.danger },
  noteTitle: { fontWeight: '800', color: theme.text },
  note: { color: theme.muted, lineHeight: 20 },
});
