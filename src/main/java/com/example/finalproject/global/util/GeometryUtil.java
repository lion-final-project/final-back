package com.example.finalproject.global.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public class GeometryUtil {
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);
    private static final double EARTH_RADIUS = 6371000; // 미터 단위

    private GeometryUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Point를 생성합니다.
     * @param latitude 위도
     * @param longitude 경도
     * @return point
     */
    public static Point createPoint(Double longitude, Double latitude) {
        if (longitude == null || latitude == null) {
            return null;
        }
        validateCoordinates(longitude, latitude);
        // JTS는 (X, Y) 순서이므로 (경도, 위도) 순으로
        return GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
    }

    public static org.springframework.data.geo.Point createPointForRedis(Double longitude, Double latitude){
        if (longitude == null || latitude == null) {
            return null;
        }
        validateCoordinates(longitude, latitude);
        return new org.springframework.data.geo.Point(longitude, latitude);
    }

    /**
     * 좌표에서 경도(X)를 추출합니다.
     * @param point 좌표
     */
    public static Double getLongitude(Point point) {
        return point != null ? point.getX() : null;
    }

    /**
     * Point에서 위도(Y)를 추출합니다.
     * @param point 좌표
     */
    public static Double getLatitude(Point point) {
        return point != null ? point.getY() : null;
    }

    public static Double getLongitudeInRedis(org.springframework.data.geo.Point point){
        return point != null ? point.getX() : null;
    }

    public static Double getLatitudeInRedis(org.springframework.data.geo.Point point){
        return point != null ? point.getY() : null;
    }

    /**
     * 두 지점 사이의 실제 거리(m)를 계산 (Haversine Formula)
     * DB 조회가 아닌 메모리 상의 연산이 필요할 때 사용
     * @param lat1 위도1
     * @param lon1 경도1
     * @param lat2 위도1
     * @param lon2 경도2
     */
    public static double calculateDistance(double lon1, double lat1, double lon2, double lat2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    private static void validateCoordinates(Double longitude, Double latitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("위도(Latitude)는 -90에서 90 사이여야 합니다: " + latitude);
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("경도(Longitude)는 -180에서 180 사이여야 합니다: " + longitude);
        }
    }
}