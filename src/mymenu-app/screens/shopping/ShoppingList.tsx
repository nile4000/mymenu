import {useSupabase} from 'app/App';
import {useEffect, useState, useCallback} from 'react';
import {View, StyleSheet} from 'react-native';
import {Button, Headline, DataTable, useTheme} from 'react-native-paper';
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

const getUserItems = async (
  userId: number,
  supabase: SupabaseClient<any, 'public', any>,
) => {
  try {
    const {data, error} = await supabase
      .from('Receipt')
      .select('*')
      .eq('user_id', userId);

    if (error) {
      console.error('Fehler beim Abrufen der Daten:', error);
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

const ShoppingList = () => {
  const supabase = useSupabase();
  const [items, setReceipts] = useState<Receipt[]>([]);
  const appSettings = useAppSettings();
  const theme = useTheme();

  // DetailView
  const showDetail = (item: Receipt) => {
    console.log('Detailansicht für', item.id, 'wird angezeigt');
  };

  // DataList
  const [page, setPage] = useState<number>(0);
  const [itemsPerPage, onItemsPerPageChange] = useState<number>(10);
  const from = page * itemsPerPage;
  const totalItems = items.length;
  const to = Math.min((page + 1) * itemsPerPage, totalItems);

  const fetchUserId = useCallback(async () => {
    const {currentUser} = auth();
    if (!currentUser) return null;
    return await getUserIdByFirebaseUID(currentUser.uid, supabase);
  }, [supabase]);

  const loadItems = useCallback(async () => {
    const userId = await fetchUserId();
    if (!userId) return;
    const userReceipts = await getUserItems(userId, supabase);
    setReceipts(userReceipts);
  }, [fetchUserId, supabase]);

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

    await supabase.from('Receipt').delete().in('id', isToDelete);
    // Aktualisieren der Liste
    loadItems();
  };

  useEffect(() => {
    loadItems();
  }, [supabase]);

  const createItem = async () => {
    const userId = await fetchUserId();
    if (!userId) return;
    const {error} = await supabase
      .from('Receipt')
      .insert([{user_id: userId, generated_at: new Date().toISOString()}]);
    if (error) {
      console.error('Fehler beim Erstellen des Receipts:', error);
    } else {
      loadItems();
    }
  };

  return (
    <View style={styles.container}>
      <Headline
        style={[styles.padded, {color: appSettings.currentTheme.colors.text}]}>
        {appSettings.t('shoppingList')}
      </Headline>
      <View style={styles.buttonContainer}>
        <Button
          mode="contained"
          style={styles.createButton}
          onPress={createItem}
          icon="plus">
          {appSettings.t('createItems')}
        </Button>
        <Button
          mode="text"
          style={styles.deleteButton}
          onPress={deleteSelectedItems}
          disabled={
            Object.entries(selectedItems).filter(
              ([_, isSelected]) => isSelected,
            ).length === 0
          }
          icon="delete">
          {appSettings.t('deleteSelectedItems')}
        </Button>
      </View>
      <DataTable>
        <DataTable.Header
          style={[styles.Header, {backgroundColor: theme.colors.surface}]}>
          <DataTable.Title>Nummer</DataTable.Title>
          <DataTable.Title>Erstelldatum</DataTable.Title>
          <DataTable.Title> </DataTable.Title>
        </DataTable.Header>
        {items.slice(from, to).map(item => (
          <DataTable.Row key={item.id} onPress={() => showDetail(item)}>
            <DataTable.Cell>{item.id}</DataTable.Cell>
            <DataTable.Cell>
              {item.generated_at ? formatDate(item.generated_at) : 'N/A'}
            </DataTable.Cell>
            <DataTable.Cell numeric>
              <Checkbox
                status={selectedItems[item.id] ? 'checked' : 'unchecked'}
                onPress={() => toggleSelectedItem(item.id)}
              />
            </DataTable.Cell>
          </DataTable.Row>
        ))}
        <DataTable.Pagination
          page={page}
          numberOfPages={Math.ceil(totalItems / itemsPerPage)}
          onPageChange={page => setPage(page)}
          label={`${from + 1}-${to} von ${totalItems}`}
          numberOfItemsPerPageList={[5, 10, 20]}
          numberOfItemsPerPage={itemsPerPage}
          onItemsPerPageChange={onItemsPerPageChange}
          showFastPaginationControls
          selectPageDropdownLabel={'Einträge pro Seite'}
        />
      </DataTable>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 5,
  },
  buttonContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between', // verteilt die Buttons gleichmäßig im Container
    marginBottom: 20, // oder der gewünschte Abstand zwischen den Buttons und den nachfolgenden Elementen
  },
  createButton: {
    flex: 1,
    marginRight: 5,
  },
  deleteButton: {
    flex: 1,
    marginLeft: 5,
  },
  Header: {
    borderTopEndRadius: 10,
    borderTopStartRadius: 10,
  },
  padded: {
    paddingBottom: 20,
    paddingTop: 20,
  },
});

export default ShoppingList;
