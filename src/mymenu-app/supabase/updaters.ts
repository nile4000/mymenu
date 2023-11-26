import {SupabaseClient} from '@supabase/supabase-js';

function handleSupabaseError(
  error,
  errorMessage = 'Fehler beim Abrufen der Daten',
) {
  console.error(errorMessage, error);
}

export const updateItem = async (
  itemId: number,
  updatedValues: {name: string; price: string; quantity: string; unit: string},
  supabase: SupabaseClient,
) => {
  try {
    const updatedItem = {
      name: updatedValues.name,
      price: parseFloat(updatedValues.price),
      quantity: parseInt(updatedValues.quantity, 10).toString(),
      unit: updatedValues.unit,
    };

    const {data, error} = await supabase
      .from('Item')
      .update(updatedItem)
      .eq('id', itemId)
      .select();

    if (error) {
      handleSupabaseError(error, 'Fehler beim Aktualisieren des Artikels');
      return null;
    }
    return data;
  } catch (err) {
    handleSupabaseError(
      err,
      'Unerwarteter Fehler beim Aktualisieren des Artikels',
    );
    return null;
  }
};
