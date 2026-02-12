import React from 'react';
import './Header.css';

interface HeaderProps {
  isConnected: boolean;
}

const Header: React.FC<HeaderProps> = ({ isConnected }) => {
  return (
    <header className="header">
      <div className="header-content">
        <h1>🚂 RailPulse</h1>
        <p className="subtitle">Real-Time Railway Safety Monitoring</p>
      </div>
      <div className="connection-status">
        <span className={`status-indicator ${isConnected ? 'connected' : 'disconnected'}`}>
          {isConnected ? '🟢 Live' : '🔴 Disconnected'}
        </span>
      </div>
    </header>
  );
};

export default Header;