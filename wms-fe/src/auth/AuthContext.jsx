import { useEffect, useMemo, useState } from 'react';
import authService from '../services/authService';
import AuthContext from './auth-context';
import {
  clearSession,
  getAccessToken,
  getRefreshToken,
  getTokenPermissions,
  getTokenRoles,
  hasUsableAccessToken,
  saveSession,
} from './session';

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    const restoreSession = async () => {
      if (!getAccessToken() && !getRefreshToken()) {
        setReady(true);
        return;
      }

      try {
        if ((!hasUsableAccessToken() || getTokenPermissions().length === 0) && getRefreshToken()) {
          const refreshed = await authService.refresh(getRefreshToken());
          saveSession(refreshed);
        }
        const currentUser = await authService.myInfo();
        setUser(currentUser);
      } catch {
        clearSession();
      } finally {
        setReady(true);
      }
    };

    restoreSession();
  }, []);

  const login = async (username, password) => {
    const response = await authService.login(username, password);
    saveSession(response);
    const currentUser = await authService.myInfo();
    setUser(currentUser);
    return currentUser;
  };

  const logout = async () => {
    const accessToken = getAccessToken();
    try {
      if (accessToken) await authService.logout(accessToken);
    } finally {
      clearSession();
      setUser(null);
    }
  };

  const value = useMemo(() => ({
    user,
    roles: user?.roles?.map((role) => role.name) || getTokenRoles(),
    permissions: getTokenPermissions(),
    ready,
    authenticated: Boolean(user && getAccessToken()),
    login,
    logout,
    hasRole: (...allowedRoles) => (user?.roles?.map((role) => role.name) || getTokenRoles())
      .some((role) => allowedRoles.includes(role)),
    hasPermission: (...allowedPermissions) => getTokenPermissions()
      .some((permission) => allowedPermissions.includes(permission)),
  }), [user, ready]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
