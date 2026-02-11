-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- Anomalies table
CREATE TABLE anomalies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    train_id VARCHAR(50) NOT NULL,
    anomaly_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    description TEXT,
    detected_at TIMESTAMP NOT NULL DEFAULT NOW(),
    location GEOMETRY(POINT, 4326),  -- PostGIS spatial column
    speed_kmh FLOAT,
    vibration_level FLOAT,
    INDEX idx_train_time (train_id, detected_at DESC),
    INDEX idx_location USING GIST (location)
);

-- Fleet status table
CREATE TABLE fleet_status (
    train_id VARCHAR(50) PRIMARY KEY,
    last_seen TIMESTAMP NOT NULL,
    current_speed FLOAT,
    current_location GEOMETRY(POINT, 4326),
    vibration_level FLOAT,
    engine_temp FLOAT,
    status VARCHAR(20) DEFAULT 'MOVING',
    INDEX idx_location USING GIST (current_location)
);

-- Track topology (geofences for sharp curves)
CREATE TABLE track_geofences (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    zone_type VARCHAR(50),  -- 'SHARP_CURVE', 'TUNNEL', etc.
    geometry GEOMETRY(POLYGON, 4326),
    max_safe_speed FLOAT,
    INDEX idx_geometry USING GIST (geometry)
);

-- Insert sample geofence (sharp curve near Austin, TX)
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
);