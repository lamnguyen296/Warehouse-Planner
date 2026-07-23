import { Bell, CheckCheck, Wifi, WifiOff } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useRealtime } from '../realtime/useRealtime';

const referenceRoutes = {
  WorkshopRequest: '/workshop',
  Planning: '/planning',
  PurchaseRequest: '/purchase',
  RecycleOrder: '/recycle',
  AssemblyOrder: '/assembly',
  TransferOrder: '/transfer',
};

const formatTime = (value) => {
  if (!value) return '';
  return new Intl.DateTimeFormat('en-GB', {
    day: '2-digit',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
};

const NotificationBell = () => {
  const [open, setOpen] = useState(false);
  const rootRef = useRef(null);
  const navigate = useNavigate();
  const { notifications, unreadCount, connected, markAsRead, markAllAsRead } = useRealtime();

  useEffect(() => {
    const closeOnOutsideClick = (event) => {
      if (rootRef.current && !rootRef.current.contains(event.target)) setOpen(false);
    };
    document.addEventListener('mousedown', closeOnOutsideClick);
    return () => document.removeEventListener('mousedown', closeOnOutsideClick);
  }, []);

  const openNotification = async (notification) => {
    await markAsRead(notification.id);
    setOpen(false);
    navigate(referenceRoutes[notification.referenceType] || '/dashboard');
  };

  return (
    <div className="notification-root" ref={rootRef}>
      <button
        className="notification-trigger"
        type="button"
        aria-label={`Notifications${unreadCount ? `, ${unreadCount} unread` : ''}`}
        aria-expanded={open}
        onClick={() => setOpen((current) => !current)}
      >
        <Bell size={19} />
        {unreadCount > 0 && <span className="notification-count">{unreadCount > 99 ? '99+' : unreadCount}</span>}
      </button>

      {open && (
        <div className="notification-popover">
          <div className="notification-heading">
            <div>
              <strong>Notifications</strong>
              <span className={`connection-state ${connected ? 'online' : ''}`} title={connected ? 'Live updates connected' : 'Reconnecting live updates'}>
                {connected ? <Wifi size={13} /> : <WifiOff size={13} />}
                {connected ? 'Live' : 'Reconnecting'}
              </span>
            </div>
            <button
              type="button"
              className="notification-read-all"
              title="Mark all as read"
              aria-label="Mark all notifications as read"
              disabled={unreadCount === 0}
              onClick={markAllAsRead}
            >
              <CheckCheck size={18} />
            </button>
          </div>

          <div className="notification-list">
            {notifications.length === 0 && <p className="notification-empty">No notifications yet.</p>}
            {notifications.map((notification) => (
              <button
                type="button"
                className={`notification-item ${notification.readAt ? '' : 'unread'}`}
                key={notification.id}
                onClick={() => openNotification(notification)}
              >
                <span className="notification-item-title">{notification.title}</span>
                <span className="notification-message">{notification.message}</span>
                <time>{formatTime(notification.createdAt)}</time>
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default NotificationBell;
