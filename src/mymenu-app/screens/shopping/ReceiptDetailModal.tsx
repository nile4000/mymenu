import {useState, useEffect, useCallback} from 'react';
import {View, StyleSheet, Modal, Text} from 'react-native';
import {
  Button,
  DataTable,
  Headline,
  TextInput,
  useTheme,
} from 'react-native-paper';
import {useSupabase} from 'app/App';
import {Item} from 'mymenu-app/interfaces/interfaces';
import {getUserReceiptItems} from 'mymenu-app/supabase/getters';
import {useAppSettings} from 'app/components/AppSettings';
import {updateItem} from 'mymenu-app/supabase/updaters';

const ReceiptDetailModal = ({receipt, onClose}) => {
  const supabase = useSupabase();
  const [items, setItems] = useState<Item[]>([]);
  const theme = useTheme();
  const appSettings = useAppSettings();

  const [editItem, setEditItem] = useState(null);

  const [editedValues, setEditedValues] = useState({
    name: '',
    price: '',
    quantity: '',
    unit: '',
  });

  const addReceiptItem = async id => {
    const {data, error} = await supabase
      .from('Item')
      .insert([
        {receipt_id: id, name: 'test', price: 0, unit: 'Stk.', quantity: 1},
      ])
      .select();
    if (error) {
      console.error('Fehler beim Erstellen des Receipt Items:', error);
    } else {
      setItems(currentItems => [...currentItems, data[0]]);
    }
  };

  const loadReceiptItems = useCallback(async () => {
    if (receipt) {
      getUserReceiptItems(receipt.id, supabase).then(data => {
        setItems(data);
      });
    }
  }, [receipt]);

  const handleEdit = item => {
    setEditItem(item.id);
    setEditedValues({
      name: item.name,
      price: item.price.toString(),
      quantity: item.quantity.toString(),
      unit: item.unit,
    });
  };

  const handleSave = async (itemId: number) => {
    const data = await updateItem(itemId, editedValues, supabase);
    if (data) {
      setItems(currentItems =>
        currentItems.map(item =>
          item.id === itemId ? {...item, ...data[0]} : item,
        ),
      );
    }
    setEditItem(null);
  };

  const handleCancel = () => {
    setEditItem(null);
    onClose();
  };

  useEffect(() => {
    loadReceiptItems();
  }, [loadReceiptItems]);

  return (
    <Modal
      animationType="slide"
      transparent={false}
      visible={!!receipt}
      onRequestClose={onClose}>
      <Headline
        style={[styles.padded, {color: appSettings.currentTheme.colors.text}]}>
        {appSettings.t('articleList')} {receipt?.id}
      </Headline>
      <View style={styles.buttonContainer}>
        <Button
          onPress={() => addReceiptItem(receipt.id)}
          style={styles.createButton}
          mode="contained"
          icon="plus">
          {appSettings.t('createItems')}
        </Button>
        <Button
          mode="text"
          style={styles.closeButton}
          onPress={handleCancel}
          icon="close">
          {appSettings.t('formClose')}
        </Button>
      </View>
      <DataTable>
        <DataTable.Header
          style={[styles.Header, {backgroundColor: theme.colors.surface}]}>
          <DataTable.Title>Name</DataTable.Title>
          <DataTable.Title>Preis</DataTable.Title>
          <DataTable.Title>Menge</DataTable.Title>
          <DataTable.Title>Einheit</DataTable.Title>
          <DataTable.Title>Bearbeiten</DataTable.Title>
        </DataTable.Header>
        {items.map(item => (
          <DataTable.Row key={item.id}>
            {editItem === item.id ? (
              <>
                <DataTable.Cell>
                  <TextInput
                    value={editedValues.name}
                    onChangeText={text =>
                      setEditedValues(prev => ({...prev, name: text}))
                    }
                  />
                </DataTable.Cell>
                <DataTable.Cell>
                  <TextInput
                    keyboardType="numeric"
                    value={editedValues.price}
                    onChangeText={text =>
                      setEditedValues(prev => ({...prev, price: text}))
                    }
                  />
                </DataTable.Cell>
                <DataTable.Cell>
                  <TextInput
                    keyboardType="numeric"
                    value={editedValues.quantity}
                    onChangeText={text =>
                      setEditedValues(prev => ({...prev, quantity: text}))
                    }
                  />
                </DataTable.Cell>
                <DataTable.Cell>
                  <TextInput
                    value={editedValues.unit}
                    onChangeText={text =>
                      setEditedValues(prev => ({...prev, unit: text}))
                    }
                  />
                </DataTable.Cell>
                <DataTable.Cell>
                  <Button
                    icon="content-save"
                    compact
                    onPress={() => handleSave(item.id)}>
                    {' '}
                  </Button>
                </DataTable.Cell>
              </>
            ) : (
              <>
                <DataTable.Cell>{item.name}</DataTable.Cell>
                <DataTable.Cell>{item.price}</DataTable.Cell>
                <DataTable.Cell>{item.quantity}</DataTable.Cell>
                <DataTable.Cell>{item.unit}</DataTable.Cell>
                <DataTable.Cell>
                  <Button
                    icon="pencil"
                    compact
                    onPress={() => handleEdit(item)}>
                    {' '}
                  </Button>
                </DataTable.Cell>
              </>
            )}
          </DataTable.Row>
        ))}
      </DataTable>
    </Modal>
  );
};

const styles = StyleSheet.create({
  buttonContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 20,
  },
  modalView: {
    margin: 20,
    backgroundColor: '#f8f9fa',
    borderRadius: 20,
    padding: 35,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 4,
    },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
  },
  createButton: {
    flex: 1,
    marginRight: 5,
  },
  closeButton: {
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

export default ReceiptDetailModal;
