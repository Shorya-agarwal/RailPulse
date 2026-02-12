-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- Anomalies table
CREATE TABLE IF NOT EXISTS anomalies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    train_id VARCHAR(50) NOT NULL,
    anomaly_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    description TEXT,
    detected_at TIMESTAMP NOT NULL DEFAULT NOW(),
    location GEOMETRY(POINT, 4326),
    speed_kmh FLOAT,
    vibration_level FLOAT
);

CREATE INDEX IF NOT EXISTS idx_train_time ON anomalies(train_id, detected_at DESC);
CREATE INDEX IF NOT EXISTS idx_location ON anomalies USING GIST(location);

-- Fleet status table
CREATE TABLE IF NOT EXISTS fleet_status (
    train_id VARCHAR(50) PRIMARY KEY,
    last_seen TIMESTAMP NOT NULL,
    current_speed FLOAT,
    current_location GEOMETRY(POINT, 4326),
    vibration_level FLOAT,
    engine_temp FLOAT,
    status VARCHAR(20) DEFAULT 'MOVING'
);

CREATE INDEX IF NOT EXISTS idx_fleet_location ON fleet_status USING GIST(current_location);

-- Track topology (geofences)
CREATE TABLE IF NOT EXISTS track_geofences (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    zone_type VARCHAR(50),
    geometry GEOMETRY(POLYGON, 4326),
    max_safe_speed FLOAT
);

CREATE INDEX IF NOT EXISTS idx_geometry ON track_geofences USING GIST(geometry);

-- Insert sample geofence
INSERT INTO track_geofences (name, zone_type, geometry, max_safe_speed)
VALUES (
    'Austin Sharp Curve',
    'SHARP_CURVE',
    ST_GeomFromText('POLYGON((
        -97.75 30.25,
        -97.74 30.25,
        -97.74 30.26,
        -97.75 30.26,
        -97.75 30.25
    ))', 4326),
    60.0
) ON CONFLICT DO NOTHING;