import { Client } from '@stomp/stompjs';
import { getAccessToken } from '../auth/session';

const apiBaseUrl = (import.meta.env.VITE_API_URL || 'http://localhost:8081/wms').replace(/\/$/, '');
const brokerUrl = `${apiBaseUrl.replace(/^http/, 'ws')}/ws`;

export const createNotificationSocket = ({
  onConnected,
  onDisconnected,
  onNotification,
  onError,
}) => {
  const client = new Client({
    brokerURL: brokerUrl,
    reconnectDelay: 3_000,
    heartbeatIncoming: 10_000,
    heartbeatOutgoing: 10_000,
    debug: () => {},
  });

  client.beforeConnect = () => {
    const token = getAccessToken();
    client.connectHeaders = token ? { Authorization: `Bearer ${token}` } : {};
  };

  client.onConnect = () => {
    onConnected();
    client.subscribe('/user/queue/notifications', (frame) => {
      try {
        onNotification(JSON.parse(frame.body));
      } catch (error) {
        onError(error);
      }
    });
  };
  client.onDisconnect = onDisconnected;
  client.onWebSocketClose = onDisconnected;
  client.onStompError = (frame) => onError(new Error(frame.headers.message || 'WebSocket error'));
  client.onWebSocketError = onError;

  return client;
};
