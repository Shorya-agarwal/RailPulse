import axios from 'axios';
import { Anomaly, FleetStatus, SystemStats } from '../types';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

export const api = {
  // Get latest anomalies
  getAnomalies: async (limit: number = 10): Promise<Anomaly[]> => {
    const response = await axios.get(`${API_BASE_URL}/anomalies?limit=${limit}`);
    return response.data;
  },

  // Get recent anomalies (last N minutes)
  getRecentAnomalies: async (minutes: number = 5): Promise<Anomaly[]> => {
    const response = await axios.get(`${API_BASE_URL}/anomalies/recent?minutes=${minutes}`);
    return response.data;
  },

  // Get fleet status
  getFleetStatus: async (): Promise<FleetStatus[]> => {
    const response = await axios.get(`${API_BASE_URL}/fleet`);
    return response.data;
  },

  // Get system stats
  getStats: async (): Promise<SystemStats> => {
    const response = await axios.get(`${API_BASE_URL}/stats`);
    return response.data;
  },

  // Health check
  healthCheck: async (): Promise<{ status: string; timestamp: string }> => {
    const response = await axios.get(`${API_BASE_URL}/health`);
    return response.data;
  },
};