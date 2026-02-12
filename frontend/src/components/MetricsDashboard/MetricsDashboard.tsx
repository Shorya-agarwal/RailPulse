import React, { useEffect, useState } from 'react';
import { api } from '../../services/api';
import { SystemStats } from '../../types';
import './MetricsDashboard.css';

const MetricsDashboard: React.FC = () => {
  const [stats, setStats] = useState<SystemStats | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const data = await api.getStats();
        setStats(data);
        setLoading(false);
      } catch (error) {
        console.error('Failed to fetch stats:', error);
      }
    };

    fetchStats();
    const interval = setInterval(fetchStats, 5000); // Refresh every 5 seconds

    return () => clearInterval(interval);
  }, []);

  if (loading || !stats) {
    return <div className="metrics-loading">Loading metrics...</div>;
  }

  return (
    <div className="metrics-dashboard">
      <div className="metric-card">
        <div className="metric-value">{stats.totalTrains}</div>
        <div className="metric-label">Total Trains</div>
      </div>

      <div className="metric-card success">
        <div className="metric-value">{stats.trainsMoving}</div>
        <div className="metric-label">Moving</div>
      </div>

      <div className="metric-card warning">
        <div className="metric-value">{stats.trainsStopped}</div>
        <div className="metric-label">Stopped</div>
      </div>

      <div className="metric-card danger">
        <div className="metric-value">{stats.trainsInDanger}</div>
        <div className="metric-label">In Danger</div>
      </div>

      <div className="metric-card">
        <div className="metric-value">{stats.totalAnomalies}</div>
        <div className="metric-label">Total Anomalies</div>
      </div>

      <div className="metric-card">
        <div className="metric-value">{stats.anomaliesLastHour}</div>
        <div className="metric-label">Last Hour</div>
      </div>

      <div className="metric-card">
        <div className="metric-value">{stats.totalArchived.toLocaleString()}</div>
        <div className="metric-label">Archived Records</div>
      </div>

      <div className="metric-card">
        <div className="metric-value">{stats.bufferSize}</div>
        <div className="metric-label">Buffer Size</div>
      </div>
    </div>
  );
};

export default MetricsDashboard;