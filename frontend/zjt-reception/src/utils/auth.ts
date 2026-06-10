const TOKEN_KEY = 'reception_token'
const STORE_ID_KEY = 'reception_store_id'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function getStoreId(): string | null {
  return localStorage.getItem(STORE_ID_KEY)
}

export function setStoreId(id: string) {
  localStorage.setItem(STORE_ID_KEY, id)
}
