import { getAuth } from "firebase/auth";

/**
 * Returns a promise that resolves with an ID token, if available, that can be verified on a server.
 * @return {!Promise<?string>} The promise that resolves with an ID token if
 *     available. Otherwise, the promise resolves with null.
 */
export const getIdToken = async () => {
  try {
    const auth = getAuth();
    const user = auth.currentUser;
    return user ? await user.getIdToken() : null;
  } catch (error) {
    console.error("Failed to retrieve ID token", error);
    return null;
  }
};
