import React from 'react';
import { ActivityIndicator, Pressable, StyleSheet, Text, View } from 'react-native';
import { theme } from './theme';

export function Card({ children }: { children: React.ReactNode }) {
  return <View style={styles.card}>{children}</View>;
}

export function SectionTitle({ children }: { children: React.ReactNode }) {
  return <Text style={styles.sectionTitle}>{children}</Text>;
}

export function Pill({ text }: { text: string }) {
  return <View style={styles.pill}><Text style={styles.pillText}>{text}</Text></View>;
}

export function PrimaryButton({ title, onPress, disabled = false }: { title: string; onPress: () => void; disabled?: boolean }) {
  return <Pressable disabled={disabled} onPress={onPress} style={({ pressed }) => [styles.button, disabled && styles.buttonDisabled, pressed && !disabled && { opacity: 0.8 }]}>
    <Text style={styles.buttonText}>{title}</Text>
  </Pressable>;
}

export function Loading() {
  return <View style={styles.center}><ActivityIndicator size="large" /><Text style={styles.muted}>Loading…</Text></View>;
}

export function ErrorView({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return <View style={styles.center}><Text style={styles.error}>{message}</Text>{onRetry ? <PrimaryButton title="Retry" onPress={onRetry} /> : null}</View>;
}

const styles = StyleSheet.create({
  card: { backgroundColor: theme.surface, borderRadius: 14, padding: 14, borderWidth: 1, borderColor: theme.border, gap: 8 },
  sectionTitle: { fontSize: 18, fontWeight: '700', color: theme.text, marginTop: 6 },
  pill: { alignSelf: 'flex-start', backgroundColor: theme.primarySoft, paddingHorizontal: 9, paddingVertical: 4, borderRadius: 999 },
  pillText: { color: theme.primary, fontWeight: '700', fontSize: 12 },
  button: { backgroundColor: theme.primary, paddingHorizontal: 16, paddingVertical: 12, borderRadius: 10, alignItems: 'center' },
  buttonDisabled: { opacity: 0.45 },
  buttonText: { color: '#FFFFFF', fontWeight: '700' },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center', gap: 12, padding: 24 },
  muted: { color: theme.muted },
  error: { color: theme.danger, textAlign: 'center' },
});
