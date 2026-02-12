import React from 'react';
import { useWebSocket } from './hooks/useWebSocket';
import Header from './components/Header/Header';
import MetricsDashboard from './components/MetricsDashboard/MetricsDashboard';
import FleetMap from './components/FleetMap/FleetMap';
import AnomalyFeed from './components/AnomalyFeed/AnomalyFeed';
import './App.css';

const App: React.FC = () => {
  const { anomalies, fleetUpdates, isConnected } = useWebSocket();

  return (
    <div className="app">
      <Header isConnected={isConnected} />

      <main className="main-content">
        <MetricsDashboard />

        <div className="content-grid">
          <div className="map-section">
            <FleetMap liveUpdates={fleetUpdates} />
          </div>

          <div className="anomaly-section">
            <AnomalyFeed liveAnomalies={anomalies} />
          </div>
        </div>
      </main>

      <footer className="footer">
        <p>
          RailPulse v1.0 | Built with React, Spring Boot, Kafka Streams & PostgreSQL |{' '}
          <a
            href="https://github.com/Shorya-agarwal"
            target="_blank"
            rel="noopener noreferrer"
          >
            GitHub
          </a>
        </p>
      </footer>
    </div>
  );
};

export default App;