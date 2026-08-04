import { useEffect, useMemo, useState } from 'react';
import { SESSION_EXPIRED_EVENT } from '../api/axiosClient';
import authService from '../services/authService';
import AuthContext from './auth-context';
import { clearLegacyTokenStorage } from './session';

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    clearLegacyTokenStorage();
    const handleSessionExpired = () => setUser(null);
    window.addEventListener(SESSION_EXPIRED_EVENT, handleSessionExpired);

    const restoreSession = async () => {
      try {
        await authService.csrf();
        const currentUser = await authService.myInfo();
        setUser(currentUser);
      } catch {
        setUser(null);
      } finally {
        setReady(true);
      }
    };

    restoreSession();
    return () => window.removeEventListener(SESSION_EXPIRED_EVENT, handleSessionExpired);
  }, []);

  const login = async (username, password) => {
    await authService.login(username, password);
    const currentUser = await authService.myInfo();
    setUser(currentUser);
    return currentUser;
  };

  const logout = async () => {
    try {
      await authService.logout();
    } finally {
      setUser(null);
    }
  };

  const roles = useMemo(
    () => user?.roles?.map((role) => role.name) || [],
    [user]
  );
  const permissions = useMemo(
    () => [...new Set((user?.roles || []).flatMap((role) =>
      (role.permissions || []).map((permission) => permission.code)
    ))],
    [user]
  );

  const value = useMemo(() => ({
    user,
    roles,
    permissions,
    ready,
    authenticated: Boolean(user),
    login,
    logout,
    hasRole: (...allowedRoles) => roles.some((role) => allowedRoles.includes(role)),
    hasPermission: (...allowedPermissions) => permissions
      .some((permission) => allowedPermissions.includes(permission)),
  }), [user, roles, permissions, ready]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
