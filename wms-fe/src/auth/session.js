const ACCESS_TOKEN_KEY = 'wms_access_token';
const REFRESH_TOKEN_KEY = 'wms_refresh_token';

const decodePayload = (token) => {
  try {
    const payload = token.split('.')[1];
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(decodeURIComponent(atob(normalized).split('').map((char) =>
      `%${char.charCodeAt(0).toString(16).padStart(2, '0')}`
    ).join('')));
  } catch {
    return null;
  }
};

export const getAccessToken = () => localStorage.getItem(ACCESS_TOKEN_KEY);
export const getRefreshToken = () => localStorage.getItem(REFRESH_TOKEN_KEY);

export const saveSession = ({ accessToken, refreshToken }) => {
  localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
  if (refreshToken) localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
};

export const clearSession = () => {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
};

export const getTokenClaims = () => decodePayload(getAccessToken() || '');

export const getTokenAuthorities = () => {
  const scope = getTokenClaims()?.scope || '';
  return [...new Set(scope.split(/\s+/).filter(Boolean))];
};

export const getTokenRoles = () => {
  return getTokenAuthorities()
    .filter((authority) => authority.startsWith('ROLE_'))
    .map((authority) => authority.substring(5));
};

export const getTokenPermissions = () => getTokenAuthorities()
  .filter((authority) => !authority.startsWith('ROLE_'));

export const hasUsableAccessToken = () => {
  const token = getAccessToken();
  const claims = getTokenClaims();
  return Boolean(token && claims?.exp && claims.exp * 1000 > Date.now());
};
