package com.example.dronesim.util;

public class GeoUtils {
    public static double distanciaKm(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx*dx + dy*dy);
    }
}
