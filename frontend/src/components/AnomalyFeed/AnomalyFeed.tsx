import React, { useEffect, useState } from 'react';
import { api } from '../../services/api';
import { Anomaly } from '../../types';
import './AnomalyFeed.css';

interface AnomalyFeedProps {
  liveAnomalies: Anomaly[];
}

const AnomalyFeed: React.FC<AnomalyFeedProps> = ({ liveAnomalies }) => {
  const [historicalAnomalies, setHistoricalAnomalies] = useState<Anomaly[]>([]);

  useEffect(() => {
    const fetchAnomalies = async () => {
      try {
        const data = await api.getRecentAnomalies(10);
        setHistoricalAnomalies(data);
      } catch (error) {
        console.error('Failed to fetch anomalies:', error);
      }
    };

    fetchAnomalies();
  }, []);

  // Merge live and historical, remove duplicates
  const allAnomalies = [...liveAnomalies, ...historicalAnomalies]
    .filter((anomaly, index, self) => 
      index === self.findIndex((a) => a.id === anomaly.id)
    )
    .slice(0, 20);

  const getSeverityClass = (severity: string) => {
    return severity.toLowerCase() === 'critical' ? 'critical' : 'warning';
  };

  return (
    <div className="anomaly-feed">
      <h2>🚨 Live Anomalies</h2>
      <div className="anomaly-list">
        {allAnomalies.length === 0 ? (
          <div className="no-anomalies">✅ No anomalies detected</div>
        ) : (
          allAnomalies.map((anomaly) => (
            <div key={anomaly.id} className={`anomaly-card ${getSeverityClass(anomaly.severity)}`}>
              <div className="anomaly-header">
                <span className="train-id">{anomaly.trainId}</span>
                <span className={`severity ${getSeverityClass(anomaly.severity)}`}>
                  {anomaly.severity}
                </span>
              </div>
              <div className="anomaly-type">{anomaly.anomalyType.replace(/_/g, ' ')}</div>
              <div className="anomaly-description">{anomaly.description}</div>
              <div className="anomaly-details">
                <span>Speed: {anomaly.speedKmh.toFixed(1)} km/h</span>
                <span>Vibration: {anomaly.vibrationLevel.toFixed(2)}</span>
              </div>
              <div className="anomaly-time">
                {new Date(anomaly.detectedAt).toLocaleString()}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default AnomalyFeed;