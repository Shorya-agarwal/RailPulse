package com.railpulse.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.locationtech.jts.geom.Point;

import java.io.IOException;

public class PointSerializer extends JsonSerializer<Point> {
    
    @Override
    public void serialize(Point point, JsonGenerator gen, SerializerProvider serializers) 
            throws IOException {
        if (point == null) {
            gen.writeNull();
            return;
        }
        
        gen.writeStartObject();
        gen.writeNumberField("lat", point.getY());
        gen.writeNumberField("lon", point.getX());
        gen.writeEndObject();
    }
}