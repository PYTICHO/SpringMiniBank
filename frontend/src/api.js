const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';
const STORAGE_KEY = 'kasay-bank-session';

export function loadSession() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function saveSession(session) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
}

export function clearSession() {
  localStorage.removeItem(STORAGE_KEY);
}

async function parseResponse(response) {
  const contentType = response.headers.get('content-type') || '';

  if (contentType.includes('application/json')) {
    return response.json();
  }

  const text = await response.text();
  return text ? { message: text } : {};
}

export function createApi(getSession, setSession, onUnauthorized) {
  let refreshPromise = null;

  async function request(path, options = {}, allowRetry = true) {
    const session = getSession();
    const headers = new Headers(options.headers || {});

    if (options.body && !headers.has('Content-Type')) {
      headers.set('Content-Type', 'application/json');
    }

    if (session?.accessToken) {
      headers.set('Authorization', `Bearer ${session.accessToken}`);
    }

    const response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers
    });

    if (response.status === 401 && allowRetry && session?.refreshToken) {
      const refreshed = await refreshSession(session.refreshToken);
      if (refreshed) {
        return request(path, options, false);
      }
    }

    const payload = await parseResponse(response);

    if (!response.ok) {
      const error = new Error(
        payload.message || payload.error || 'Request failed'
      );
      error.status = response.status;
      error.payload = payload;
      throw error;
    }

    return payload;
  }

  async function refreshSession(refreshToken) {
    if (refreshPromise) {
      return refreshPromise;
    }

    refreshPromise = (async () => {
      try {
        const response = await fetch(`${API_BASE_URL}/api/auth/refresh`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({ refreshToken })
        });

        const payload = await parseResponse(response);
        if (!response.ok) {
          throw new Error(payload.message || 'Refresh failed');
        }

        const nextSession = {
          ...payload,
          receivedAt: new Date().toISOString()
        };

        saveSession(nextSession);
        setSession(nextSession);
        return nextSession;
      } catch {
        clearSession();
        setSession(null);
        onUnauthorized();
        return null;
      } finally {
        refreshPromise = null;
      }
    })();

    return refreshPromise;
  }

  return {
    register: (body) =>
      request('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify(body)
      }),
    login: (body) =>
      request('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify(body)
      }),
    refresh: (refreshToken) => refreshSession(refreshToken),
    checkAuth: () => request('/api/banking/checkAuth'),
    getRecipients: () => request('/api/banking/recipients'),
    getMyCards: () => request('/api/banking/my_cards'),
    createCard: () =>
      request('/api/banking/create_card', {
        method: 'POST'
      }),
    makeDeposit: (body) =>
      request('/api/banking/make_deposit', {
        method: 'POST',
        body: JSON.stringify(body)
      }),
    makeTransaction: (body) =>
      request('/api/banking/make_transaction', {
        method: 'POST',
        body: JSON.stringify(body)
      })
  };
}
