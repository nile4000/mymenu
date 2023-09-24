import {useSupabase} from 'app/App';
import {useEffect, useState} from 'react';
import {View, Text, FlatList, StyleSheet} from 'react-native';
import {Button, Headline, useTheme} from 'react-native-paper';
import auth from '@react-native-firebase/auth';
import {SupabaseClient} from '@supabase/supabase-js';
import {Checkbox} from 'react-native-paper';
import {useAppSettings} from 'app/components/AppSettings';

interface Receipt {
  created_at: string;
  generated_at: string | null;
  id: number;
  user_id: number | null;
}

const getUserIdByFirebaseUID = async (
  firebaseUID: string | undefined,
  supabase: SupabaseClient<any, 'public', any>,
): Promise<number | null> => {
  try {
    const {data, error} = await supabase
      .from('User')
      .select('id')
      .eq('firebase_uid', firebaseUID);

    if (error || !data || data.length === 0) {
      console.error('Fehler beim Abrufen der Daten:', error || 'Keine Daten');
      return null;
    }
    return data[0].id;
  } catch (err) {
    console.error('Unerwarteter Fehler:', err);
    return null;
  }
};

const getUserReceipts = async (
  userId: number,
  supabase: SupabaseClient<any, 'public', any>,
) => {
  try {
    const {data, error} = await supabase
      .from('Receipt')
      .select('*')
      .eq('user_id', userId);

    if (error) {
      console.error('Fehler beim Abrufen der Receipts:', error);
      return [];
    }
    return data || [];
  } catch (err) {
    console.error('Unerwarteter Fehler:', err);
    return [];
  }
};
// Hilfsfunktion, um nur das Datum zu extrahieren
const formatDate = (dateString: string): string => {
  const date = new Date(dateString);
  return `${date.getDate().toString().padStart(2, '0')}.${(date.getMonth() + 1)
    .toString()
    .padStart(2, '0')}.${date.getFullYear()}`;
};

const ReceiptHeader = () => {
  return (
    <View style={styles.header}>
      <Text style={styles.headerText}>Nummer</Text>
      <Text style={styles.headerText}>Datum</Text>
    </View>
  );
};

const ReceiptItem = ({item, isSelected, onToggle}) => {
  return (
    <View style={styles.receiptItem}>
      <Checkbox
        status={isSelected ? 'checked' : 'unchecked'}
        onPress={onToggle}
      />
      <Text style={styles.itemText}>{item.id}</Text>
      <Text style={styles.itemText}>
        {item.generated_at ? formatDate(item.generated_at) : 'N/A'}
      </Text>
    </View>
  );
};

const ShoppingList = () => {
  const supabase = useSupabase();
  const [receipts, setReceipts] = useState<Receipt[]>([]);
  const appSettings = useAppSettings();
  const theme = useTheme();

  const loadReceipts = async () => {
    const {currentUser} = auth();
    if (!currentUser) return;

    const userId = await getUserIdByFirebaseUID(currentUser.uid, supabase);
    if (!userId) return;

    const userReceipts = await getUserReceipts(userId, supabase);
    setReceipts(userReceipts);
  };
  // Zustand für ausgewählte Einträge
  const [selectedItems, setSelectedItems] = useState<Record<number, boolean>>(
    {},
  );

  const toggleSelectedItem = (id: number) => {
    setSelectedItems(prevSelected => ({
      ...prevSelected,
      [id]: !prevSelected[id],
    }));
  };

  const deleteSelectedItems = async () => {
    const isToDelete = Object.keys(selectedItems)
      .filter(id => selectedItems[parseInt(id)])
      .map(id => parseInt(id));

    const {error} = await supabase
      .from('Receipt')
      .delete()
      .in('id', isToDelete);

    // Aktualisieren der Liste
    loadReceipts();
  };

  useEffect(() => {
    loadReceipts();
  }, [supabase]);

  const createReceipt = async () => {
    const {currentUser} = auth();
    if (!currentUser) return;

    const userId = await getUserIdByFirebaseUID(currentUser.uid, supabase);
    if (!userId) return;

    const {error} = await supabase
      .from('Receipt')
      .insert([{user_id: userId, generated_at: new Date().toISOString()}]);

    if (error) {
      console.error('Fehler beim Erstellen des Receipts:', error);
    } else {
      // Lade die Liste nach dem erfolgreichen Erstellen erneut
      loadReceipts();
    }
  };

  return (
    <View style={styles.container}>
      <Headline
        style={[styles.padded, {color: appSettings.currentTheme.colors.text}]}>
        {appSettings.t('shoppingList')}
      </Headline>
      {/* <Text style={{color: theme.colors.text}}></Text> */}
      <View style={styles.buttonContainer}>
        <Button style={styles.createButton} onPress={createReceipt} icon="plus">
          {appSettings.t('createItems')}
        </Button>

        <Button
          style={styles.deleteButton}
          onPress={deleteSelectedItems}
          icon="delete">
          {appSettings.t('deleteSelectedItems')}
        </Button>
      </View>

      <ReceiptHeader />

      <FlatList
        data={receipts}
        keyExtractor={item => item.id.toString()}
        renderItem={({item}) => (
          <ReceiptItem
            item={item}
            isSelected={selectedItems[item.id]}
            onToggle={() => toggleSelectedItem(item.id)}
          />
        )}
      />
    </View>
  );
};
const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 10,
    backgroundColor: '#f5f5f5', // leichter Hintergrund
  },
  buttonContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between', // verteilt die Buttons gleichmäßig im Container
    marginBottom: 20, // oder der gewünschte Abstand zwischen den Buttons und den nachfolgenden Elementen
  },
  createButton: {
    flex: 1,
    marginRight: 5, // Abstand zwischen den Buttons
    elevation: 5,
    shadowOffset: {width: 2, height: 2},
    shadowOpacity: 0.3,
    shadowRadius: 3,
  },
  deleteButton: {
    flex: 1,
    marginLeft: 5,
    color: '#ff0000',
  },
  header: {
    flexDirection: 'row',
    borderBottomWidth: 2,
    borderBottomColor: '#e0e0e0',
    marginBottom: 10,
    paddingHorizontal: 5, // Hinzugefügt für bessere Abstände
  },
  headerText: {
    flex: 1,
    fontSize: 18, // Vergrößerte Schrift
    textAlign: 'center',
    paddingBottom: 5, // Hinzugefügt für bessere Abstände
  },
  receiptItem: {
    flexDirection: 'row',
    padding: 10,
    borderBottomWidth: 1,
    borderRadius: 8,
    borderBottomColor: '#e0e0e0',
    backgroundColor: '#ffffff', // Weißer Hintergrund für Listenelemente
    marginVertical: 2, // Vertikaler Abstand zwischen Listenelementen
  },
  itemText: {
    flex: 1,
    fontSize: 14, // Ein wenig kleiner als die Kopfzeile
    textAlign: 'center',
  },
  padded: {
    paddingBottom: 40,
    paddingTop: 20,
  },
});

export default ShoppingList;
