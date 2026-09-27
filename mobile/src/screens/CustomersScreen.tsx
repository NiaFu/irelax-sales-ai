import React, { useCallback, useEffect, useState } from 'react';
import { FlatList, Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';
import { api } from '../api';
import { Card, ErrorView, Loading, Pill, PrimaryButton, SectionTitle } from '../components';
import { theme } from '../theme';
import type { Customer, SalesStage } from '../types';

const stages: SalesStage[] = ['NEW', 'CONTACTED', 'VISITED', 'INTERESTED', 'HOT', 'SOLD', 'LOST', 'AFTER_SALES'];

export function CustomersScreen() {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [selected, setSelected] = useState<string | null>(null);
  const [creating, setCreating] = useState(false);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(async (term = search) => {
    try { setError(''); setCustomers(await api.customers(term || undefined)); }
    catch (e) { setError(e instanceof Error ? e.message : 'Unable to load customers'); }
    finally { setLoading(false); }
  }, [search]);

  useEffect(() => { void load(''); }, []); // initial only

  if (selected) return <CustomerDetail customerId={selected} onBack={() => { setSelected(null); void load(''); }} />;
  if (creating) return <NewCustomer onBack={() => setCreating(false)} onCreated={id => { setCreating(false); setSelected(id); }} />;
  if (loading) return <Loading />;
  if (error && customers.length === 0) return <ErrorView message={error} onRetry={() => void load()} />;

  return (
    <View style={styles.root}>
      <View style={styles.header}>
        <View style={styles.rowBetween}><View><Text style={styles.title}>Customers</Text><Text style={styles.subtitle}>CRM and sales context</Text></View><Pressable onPress={() => setCreating(true)} style={styles.add}><Text style={styles.addText}>Add</Text></Pressable></View>
        <TextInput value={search} onChangeText={setSearch} onSubmitEditing={() => void load()} placeholder="Search by customer name" placeholderTextColor={theme.muted} style={styles.search} returnKeyType="search" />
      </View>
      <FlatList
        data={customers}
        keyExtractor={item => item.id}
        contentContainerStyle={styles.list}
        renderItem={({ item }) => <Pressable onPress={() => setSelected(item.id)}><Card>
          <View style={styles.rowBetween}><Text style={styles.customer}>{item.displayName}</Text><Pill text={item.salesStage} /></View>
          <Text style={styles.body}>{item.phone}</Text>
          {item.feedback ? <Text style={styles.muted} numberOfLines={2}>{item.feedback}</Text> : null}
          {item.interests.length ? <Text style={styles.muted}>Products: {item.interests.map(x => `${x.productName}${x.source === 'AI' ? ' (AI)' : ''}`).join(', ')}</Text> : null}
        </Card></Pressable>}
      />
    </View>
  );
}

function CustomerDetail({ customerId, onBack }: { customerId: string; onBack: () => void }) {
  const [customer, setCustomer] = useState<Customer | null>(null);
  const [note, setNote] = useState('');
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    try { setError(''); setCustomer(await api.customer(customerId)); }
    catch (e) { setError(e instanceof Error ? e.message : 'Unable to load customer'); }
  }, [customerId]);
  useEffect(() => { void load(); }, [load]);

  if (!customer && !error) return <Loading />;
  if (!customer) return <ErrorView message={error} onRetry={() => void load()} />;

  const changeStage = async (stage: SalesStage) => {
    const payload = {
      firstName: customer.firstName, lastName: customer.lastName, phone: customer.phone, email: customer.email,
      preferredChannel: customer.preferredChannel, salesStage: stage, firstVisitDate: customer.firstVisitDate,
      firstVisitLocation: customer.firstVisitLocation, feedback: customer.feedback, budgetMin: customer.budgetMin,
      budgetMax: customer.budgetMax, notes: customer.notes, nextFollowUpAt: customer.nextFollowUpAt,
    };
    setCustomer(await api.updateCustomer(customer.id, payload));
  };

  return <ScrollView style={styles.root} contentContainerStyle={styles.detailContent}>
    <Pressable onPress={onBack}><Text style={styles.back}>Back</Text></Pressable>
    <View style={styles.rowBetween}><View><Text style={styles.title}>{customer.displayName}</Text><Text style={styles.subtitle}>{customer.phone}</Text></View><Pill text={customer.salesStage} /></View>

    <SectionTitle>Sales stage</SectionTitle>
    <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.stageRow}>
      {stages.map(stage => <Pressable key={stage} onPress={() => void changeStage(stage)} style={[styles.stageButton, customer.salesStage === stage && styles.stageButtonActive]}><Text style={[styles.stageText, customer.salesStage === stage && styles.stageTextActive]}>{stage}</Text></Pressable>)}
    </ScrollView>

    <SectionTitle>Customer context</SectionTitle>
    <Card>
      <Field label="First visit" value={[customer.firstVisitDate, customer.firstVisitLocation].filter(Boolean).join(' · ') || 'Not recorded'} />
      <Field label="Feedback" value={customer.feedback || 'Not recorded'} />
      <Field label="Budget" value={customer.budgetMin || customer.budgetMax ? `$${customer.budgetMin ?? '?'} – $${customer.budgetMax ?? '?'}` : 'Not recorded'} />
      <Field label="Last contact" value={customer.lastContactAt ? new Date(customer.lastContactAt).toLocaleString() : 'Not yet'} />
      <Field label="Next follow-up" value={customer.nextFollowUpAt ? new Date(customer.nextFollowUpAt).toLocaleString() : 'Not scheduled'} />
    </Card>

    <SectionTitle>Product interest</SectionTitle>
    {customer.interests.length === 0 ? <Card><Text style={styles.muted}>No product interests recorded.</Text></Card> : customer.interests.map(item => <Card key={item.id}>
      <View style={styles.rowBetween}><Text style={styles.customer}>{item.productName}</Text><Pill text={`${item.source} · ${item.level}`} /></View>
      {item.reason ? <Text style={styles.muted}>{item.reason}</Text> : null}
    </Card>)}

    <SectionTitle>Notes</SectionTitle>
    <Card>
      <TextInput value={note} onChangeText={setNote} placeholder="Add a customer note" placeholderTextColor={theme.muted} multiline style={styles.noteInput} />
      <PrimaryButton title="Add note" disabled={!note.trim()} onPress={async () => { await api.addNote(customer.id, note.trim()); setNote(''); await load(); }} />
    </Card>
    {customer.customerNotes.map(item => <Card key={item.id}><Text style={styles.body}>{item.body}</Text><Text style={styles.muted}>{new Date(item.createdAt).toLocaleString()}</Text></Card>)}
  </ScrollView>;
}

function NewCustomer({ onBack, onCreated }: { onBack: () => void; onCreated: (id: string) => void }) {
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [phone, setPhone] = useState('');
  const [feedback, setFeedback] = useState('');
  const [error, setError] = useState('');

  const create = async () => {
    try {
      const result = await api.createCustomer({ firstName, lastName: lastName || null, phone, feedback: feedback || null, preferredChannel: 'SMS', salesStage: 'NEW' });
      onCreated(result.id);
    } catch (e) { setError(e instanceof Error ? e.message : 'Unable to create customer'); }
  };

  return <ScrollView style={styles.root} contentContainerStyle={styles.detailContent}>
    <Pressable onPress={onBack}><Text style={styles.back}>Back</Text></Pressable>
    <Text style={styles.title}>New customer</Text>
    {error ? <Text style={styles.error}>{error}</Text> : null}
    <Input label="First name" value={firstName} onChangeText={setFirstName} />
    <Input label="Last name" value={lastName} onChangeText={setLastName} />
    <Input label="Phone" value={phone} onChangeText={setPhone} keyboardType="phone-pad" />
    <Input label="First meeting feedback" value={feedback} onChangeText={setFeedback} multiline />
    <PrimaryButton title="Create customer" disabled={!firstName.trim() || !phone.trim()} onPress={() => void create()} />
  </ScrollView>;
}

function Field({ label, value }: { label: string; value: string }) { return <View><Text style={styles.fieldLabel}>{label}</Text><Text style={styles.body}>{value}</Text></View>; }
function Input({ label, ...props }: { label: string } & React.ComponentProps<typeof TextInput>) { return <View style={{ gap: 6 }}><Text style={styles.fieldLabel}>{label}</Text><TextInput {...props} placeholderTextColor={theme.muted} style={[styles.search, props.multiline && { minHeight: 100, textAlignVertical: 'top' }]} /></View>; }

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: theme.background },
  header: { padding: 16, gap: 12 },
  detailContent: { padding: 16, gap: 10, paddingBottom: 50 },
  title: { fontSize: 30, fontWeight: '800', color: theme.text },
  subtitle: { color: theme.muted },
  list: { paddingHorizontal: 16, gap: 10, paddingBottom: 40 },
  rowBetween: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', gap: 10 },
  customer: { fontSize: 16, fontWeight: '700', color: theme.text, flexShrink: 1 },
  body: { color: theme.text, lineHeight: 20 },
  muted: { color: theme.muted, fontSize: 12 },
  add: { borderWidth: 1, borderColor: theme.primary, borderRadius: 10, paddingHorizontal: 14, paddingVertical: 9 },
  addText: { color: theme.primary, fontWeight: '700' },
  search: { backgroundColor: theme.surface, borderWidth: 1, borderColor: theme.border, borderRadius: 10, padding: 11, color: theme.text },
  back: { color: theme.primary, fontWeight: '700', marginBottom: 4 },
  stageRow: { gap: 7, paddingVertical: 3 },
  stageButton: { paddingHorizontal: 10, paddingVertical: 8, borderWidth: 1, borderColor: theme.border, borderRadius: 999, backgroundColor: theme.surface },
  stageButtonActive: { backgroundColor: theme.primary, borderColor: theme.primary },
  stageText: { color: theme.text, fontSize: 12, fontWeight: '600' },
  stageTextActive: { color: '#FFFFFF' },
  fieldLabel: { fontSize: 12, color: theme.muted, fontWeight: '700', textTransform: 'uppercase' },
  noteInput: { borderWidth: 1, borderColor: theme.border, borderRadius: 10, minHeight: 80, padding: 10, textAlignVertical: 'top', color: theme.text },
  error: { color: theme.danger },
});
