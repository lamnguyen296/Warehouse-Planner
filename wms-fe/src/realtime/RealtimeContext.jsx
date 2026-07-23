import { useEffect, useRef, useState } from 'react';
import toast from 'react-hot-toast';
import { useAuth } from '../auth/useAuth';
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

    const initialize = async () => {
      if (!ready || !authenticated) return;

      try {
        const [storedNotifications, count] = await Promise.all([
          notificationService.getAll(),
          notificationService.getUnreadCount(),
        ]);
        if (!active) return;
        knownEventIds.current = new Set(storedNotifications.map((notification) => notification.eventId));
        setNotifications(storedNotifications);
        setUnreadCount(count.count);
      } catch {
        if (!active) return;
      }

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
        onError: () => active && setConnected(false),
      });
      client.activate();
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
