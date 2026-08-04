import { useEffect, useRef, useState } from 'react';
import toast from 'react-hot-toast';
import { useAuth } from '../auth/useAuth';
import authService from '../services/authService';
import notificationService from '../services/notificationService';
import { createNotificationSocket } from '../services/websocketService';
import RealtimeContext from './realtime-context';

export const RealtimeProvider = ({ children }) => {
  const { authenticated, ready, user } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [connected, setConnected] = useState(false);
  const knownEventIds = useRef(new Set());

  useEffect(() => {
    let active = true;
    let client;
    let recoveringSession = false;

    const recoverSocketSession = async () => {
      if (!active || recoveringSession) return;
      recoveringSession = true;
      try {
        await authService.myInfo();
        const csrfToken = await authService.csrf();
        if (client && csrfToken?.headerName && csrfToken?.token) {
          client.connectHeaders = { [csrfToken.headerName]: csrfToken.token };
        }
      } catch {
        // The HTTP auth flow emits the session-expired event on a terminal 401.
      } finally {
        recoveringSession = false;
      }
    };

    const initialize = async () => {
      if (!ready || !authenticated) return;

      try {
        const csrfToken = await authService.csrf();
        const [storedNotifications, count] = await Promise.all([
          notificationService.getAll(),
          notificationService.getUnreadCount(),
        ]);
        if (!active) return;
        knownEventIds.current = new Set(storedNotifications.map((notification) => notification.eventId));
        setNotifications(storedNotifications);
        setUnreadCount(count.count);

        client = createNotificationSocket({
          onConnected: () => active && setConnected(true),
          onDisconnected: () => active && setConnected(false),
          onNotification: (notification) => {
            if (!active || knownEventIds.current.has(notification.eventId)) return;
            knownEventIds.current.add(notification.eventId);
            setNotifications((current) => [notification, ...current].slice(0, 50));
            if (!notification.readAt) setUnreadCount((current) => current + 1);
            toast.success(notification.message, { id: notification.eventId });
          },
          onError: () => {
            if (!active) return;
            setConnected(false);
            recoverSocketSession();
          },
          csrfToken,
        });
        client.activate();
      } catch {
        if (active) setConnected(false);
      }
    };

    initialize();
    return () => {
      active = false;
      setConnected(false);
      if (client) client.deactivate();
    };
  }, [authenticated, ready, user?.username]);

  const markAsRead = async (id) => {
    const current = notifications.find((notification) => notification.id === id);
    if (!current || current.readAt) return current;
    const updated = await notificationService.markAsRead(id);
    setNotifications((items) => items.map((item) => item.id === id ? updated : item));
    setUnreadCount((count) => Math.max(0, count - 1));
    return updated;
  };

  const markAllAsRead = async () => {
    await notificationService.markAllAsRead();
    const readAt = new Date().toISOString();
    setNotifications((items) => items.map((item) => item.readAt ? item : { ...item, readAt }));
    setUnreadCount(0);
  };

  const value = {
    notifications,
    unreadCount,
    connected,
    markAsRead,
    markAllAsRead,
  };

  return <RealtimeContext.Provider value={value}>{children}</RealtimeContext.Provider>;
};
