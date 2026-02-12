import { useEffect, useState, useCallback } from 'react';
import SockJS from 'sockjs-client';
import { Client, IMessage } from '@stomp/stompjs';
import { Anomaly, FleetStatus } from '../types';

const WS_URL = process.env.REACT_APP_WS_URL || 'http://localhost:8080/ws';

export const useWebSocket = () => {
  const [anomalies, setAnomalies] = useState<Anomaly[]>([]);
  const [fleetUpdates, setFleetUpdates] = useState<FleetStatus[]>([]);
  const [isConnected, setIsConnected] = useState(false);
  const [client, setClient] = useState<Client | null>(null);

  useEffect(() => {
    const stompClient = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: (str) => {
        console.log('STOMP: ' + str);
      },
      onConnect: () => {
        console.log('✅ WebSocket connected');
        setIsConnected(true);

        // Subscribe to anomalies
        stompClient.subscribe('/topic/anomalies', (message: IMessage) => {
          const anomaly: Anomaly = JSON.parse(message.body);
          setAnomalies((prev) => [anomaly, ...prev].slice(0, 50)); // Keep last 50
        });

        // Subscribe to fleet status
        stompClient.subscribe('/topic/fleet-status', (message: IMessage) => {
          const status: FleetStatus = JSON.parse(message.body);
          setFleetUpdates((prev) => {
            const filtered = prev.filter((s) => s.trainId !== status.trainId);
            return [status, ...filtered].slice(0, 50);
          });
        });
      },
      onDisconnect: () => {
        console.log('❌ WebSocket disconnected');
        setIsConnected(false);
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
        setIsConnected(false);
      },
    });

    stompClient.activate();
    setClient(stompClient);

    return () => {
      stompClient.deactivate();
    };
  }, []);

  const clearAnomalies = useCallback(() => {
    setAnomalies([]);
  }, []);

  return { anomalies, fleetUpdates, isConnected, clearAnomalies };
};