import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup, CircleMarker } from 'react-leaflet';
import L from 'leaflet';
import { api } from '../../services/api';
import { FleetStatus } from '../../types';
import 'leaflet/dist/leaflet.css';
import './FleetMap.css';

// Fix Leaflet default icon issue with Webpack
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: require('leaflet/dist/images/marker-icon-2x.png'),
  iconUrl: require('leaflet/dist/images/marker-icon.png'),
  shadowUrl: require('leaflet/dist/images/marker-shadow.png'),
});

interface FleetMapProps {
  liveUpdates: FleetStatus[];
}

const FleetMap: React.FC<FleetMapProps> = ({ liveUpdates }) => {
  const [fleetData, setFleetData] = useState<FleetStatus[]>([]);
  const [selectedTrain, setSelectedTrain] = useState<string | null>(null);

  // Initial fetch
  useEffect(() => {
    const fetchFleet = async () => {
      try {
        const data = await api.getFleetStatus();
        setFleetData(data);
      } catch (error) {
        console.error('Failed to fetch fleet:', error);
      }
    };

    fetchFleet();
    const interval = setInterval(fetchFleet, 10000); // Refresh every 10s

    return () => clearInterval(interval);
  }, []);

  // Merge live updates with existing data
  useEffect(() => {
    if (liveUpdates.length > 0) {
      setFleetData((prev) => {
        const updated = [...prev];
        liveUpdates.forEach((liveUpdate) => {
          const index = updated.findIndex((train) => train.trainId === liveUpdate.trainId);
          if (index !== -1) {
            updated[index] = liveUpdate;
          } else {
            updated.push(liveUpdate);
          }
        });
        return updated;
      });
    }
  }, [liveUpdates]);

  // Extract coordinates from PostGIS geometry
  const getCoordinates = (train: FleetStatus): [number, number] | null => {
    if (!train.currentLocation) return null;
    
    // Check if it's the new serialized format {lat, lon}
    if (typeof train.currentLocation === 'object' && 'lat' in train.currentLocation && 'lon' in train.currentLocation) {
      const location = train.currentLocation as { lat: number; lon: number };
      return [location.lat, location.lon];
    }
    
    // Fallback for old format (shouldn't happen but safe)
    if ('coordinates' in train.currentLocation && Array.isArray((train.currentLocation as any).coordinates)) {
      const [lon, lat] = (train.currentLocation as any).coordinates;
      return [lat, lon];
    }
    
    return null;
  };

  // Get marker color based on status
  const getMarkerColor = (status: string): string => {
    switch (status) {
      case 'DANGER':
        return '#f56565';
      case 'STOPPED':
        return '#ed8936';
      case 'MOVING':
        return '#48bb78';
      default:
        return '#4299e1';
    }
  };

  // Filter trains with valid coordinates
  const trainsWithLocation = fleetData.filter((train) => getCoordinates(train) !== null);

  // Default center (Austin, TX area)
  const defaultCenter: [number, number] = [30.2672, -97.7431];

  return (
    <div className="fleet-map-container">
      <div className="map-header">
        <h2>🗺️ Live Fleet Map</h2>
        <div className="map-legend">
          <span className="legend-item">
            <span className="legend-dot moving"></span> Moving
          </span>
          <span className="legend-item">
            <span className="legend-dot stopped"></span> Stopped
          </span>
          <span className="legend-item">
            <span className="legend-dot danger"></span> Danger
          </span>
        </div>
      </div>

      <MapContainer
        center={defaultCenter}
        zoom={10}
        style={{ height: '600px', width: '100%' }}
        className="leaflet-map"
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {trainsWithLocation.map((train) => {
          const coords = getCoordinates(train);
          if (!coords) return null;

          return (
            <CircleMarker
              key={train.trainId}
              center={coords}
              radius={8}
              fillColor={getMarkerColor(train.status)}
              color="#fff"
              weight={2}
              opacity={1}
              fillOpacity={0.8}
              eventHandlers={{
                click: () => setSelectedTrain(train.trainId),
              }}
            >
              <Popup>
                <div className="train-popup">
                  <h3>{train.trainId}</h3>
                  <div className="popup-detail">
                    <strong>Status:</strong>{' '}
                    <span className={`status-badge ${train.status.toLowerCase()}`}>
                      {train.status}
                    </span>
                  </div>
                  <div className="popup-detail">
                    <strong>Speed:</strong> {train.currentSpeed.toFixed(1)} km/h
                  </div>
                  <div className="popup-detail">
                    <strong>Vibration:</strong> {train.vibrationLevel.toFixed(2)}
                  </div>
                  <div className="popup-detail">
                    <strong>Engine Temp:</strong> {train.engineTemp.toFixed(1)}°C
                  </div>
                  <div className="popup-detail">
                    <strong>Last Seen:</strong>{' '}
                    {new Date(train.lastSeen).toLocaleTimeString()}
                  </div>
                </div>
              </Popup>
            </CircleMarker>
          );
        })}
      </MapContainer>

      {trainsWithLocation.length === 0 && (
        <div className="no-data-overlay">
          <p>⏳ Waiting for train location data...</p>
        </div>
      )}
    </div>
  );
};

export default FleetMap;