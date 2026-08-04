import { Client } from '@stomp/stompjs';

const apiBaseUrl = (import.meta.env.VITE_API_URL || 'http://localhost:8081/wms').replace(/\/$/, '');
const brokerUrl = `${apiBaseUrl.replace(/^http/, 'ws')}/ws`;

export const createNotificationSocket = ({
  onConnected,
  onDisconnected,
  onNotification,
  onError,
  csrfToken,
}) => {
  const client = new Client({
    brokerURL: brokerUrl,
    reconnectDelay: 3_000,
    heartbeatIncoming: 10_000,
    heartbeatOutgoing: 10_000,
    debug: () => {},
    connectHeaders: csrfToken?.headerName && csrfToken?.token
      ? { [csrfToken.headerName]: csrfToken.token }
      : {},
  });

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
  client.onWebSocketClose = (event) => {
    onDisconnected();
    if (client.active && event.code !== 1000) {
      onError(new Error(`WebSocket closed with code ${event.code}`));
    }
  };
  client.onStompError = (frame) => onError(new Error(frame.headers.message || 'WebSocket error'));
  client.onWebSocketError = onError;

  return client;
};
