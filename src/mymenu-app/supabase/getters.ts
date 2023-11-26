import {SupabaseClient} from '@supabase/supabase-js';

function handleSupabaseError(
  error,
  errorMessage = 'Fehler beim Abrufen der Daten',
) {
  console.error(errorMessage, error);
}

export const getUserIdByFirebaseUID = async (
  firebaseUID: string | undefined,
  supabase: SupabaseClient<any, 'public', any>,
): Promise<number | null> => {
  try {
    const {data, error} = await supabase
      .from('User')
      .select('id')
      .eq('firebase_uid', firebaseUID);

    if (error || !data || data.length === 0) {
      handleSupabaseError(error, 'Fehler beim Abrufen der User-ID');
      return null;
    }
    return data[0].id;
  } catch (err) {
    handleSupabaseError(err, 'Unerwarteter Fehler');
    return null;
  }
};

export const getUserReceipts = async (
  userId: number,
  supabase: SupabaseClient<any, 'public', any>,
): Promise<any[]> => {
  try {
    const {data, error} = await supabase
      .from('Receipt')
      .select('*')
      .eq('user_id', userId);

    if (error) {
      handleSupabaseError(error);
      return [];
    }
    return data || [];
  } catch (err) {
    handleSupabaseError(err, 'Unerwarteter Fehler');
    return [];
  }
};

export const getUserReceiptItems = async (
  receipt_id: number,
  supabase: SupabaseClient<any, 'public', any>,
): Promise<any[]> => {
  try {
    const {data, error} = await supabase
      .from('Item')
      .select('*')
      .eq('receipt_id', receipt_id);

    if (error) {
      handleSupabaseError(error);
      return [];
    }
    return data || [];
  } catch (err) {
    handleSupabaseError(err, 'Unerwarteter Fehler');
    return [];
  }
};
