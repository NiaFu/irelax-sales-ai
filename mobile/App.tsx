import React, { useState } from 'react';
import { SafeAreaView, StatusBar, StyleSheet, Text, Pressable, View, useColorScheme } from 'react-native';
import { TodayScreen } from './src/screens/TodayScreen';
import { MessagesScreen } from './src/screens/MessagesScreen';
import { CustomersScreen } from './src/screens/CustomersScreen';
import { AssistantScreen } from './src/screens/AssistantScreen';
import { theme } from './src/theme';

type Tab = 'Today' | 'Messages' | 'Customers' | 'Assistant';
const tabs: Tab[] = ['Today', 'Messages', 'Customers', 'Assistant'];

export default function App() {
  const [tab, setTab] = useState<Tab>('Today');
  const scheme = useColorScheme();
  return (
    <SafeAreaView style={styles.safe}>
      <StatusBar barStyle={scheme === 'dark' ? 'light-content' : 'dark-content'} />
      <View style={styles.body}>
        {tab === 'Today' && <TodayScreen />}
        {tab === 'Messages' && <MessagesScreen />}
        {tab === 'Customers' && <CustomersScreen />}
        {tab === 'Assistant' && <AssistantScreen />}
      </View>
      <View style={styles.tabs}>
        {tabs.map(item => <Pressable key={item} onPress={() => setTab(item)} style={styles.tab}>
          <Text style={[styles.tabText, tab === item && styles.tabActive]}>{item}</Text>
        </Pressable>)}
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: theme.surface },
  body: { flex: 1 },
  tabs: { flexDirection: 'row', borderTopWidth: 1, borderColor: theme.border, backgroundColor: theme.surface, paddingVertical: 9 },
  tab: { flex: 1, alignItems: 'center', paddingVertical: 6 },
  tabText: { color: theme.muted, fontSize: 12, fontWeight: '700' },
  tabActive: { color: theme.primary },
});
