export interface Anomaly {
    id: string;
    trainId: string;
    anomalyType: string;
    severity: string;
    description: string;
    detectedAt: string;
    speedKmh: number;
    vibrationLevel: number;
    location?: {
      type: string;
      coordinates: [number, number];
    };
  }
  
  export interface FleetStatus {
    trainId: string;
    lastSeen: string;
    currentSpeed: number;
    currentLocation?: {
      lat: number;
      lon: number;
    } | null;  // ← Update this to match new serializer format
    vibrationLevel: number;
    engineTemp: number;
    status: 'MOVING' | 'STOPPED' | 'DANGER';
  }
  
  export interface SystemStats {
    totalAnomalies: number;
    totalTrains: number;
    trainsMoving: number;
    trainsStopped: number;
    trainsInDanger: number;
    totalArchived: number;
    bufferSize: number;
    anomaliesLastHour: number;
  }