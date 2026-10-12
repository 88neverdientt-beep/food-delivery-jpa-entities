package vn.edu.ptit.fooddelivery.util;

/**
 * Tính khoảng cách theo công thức Haversine (Earth radius = 6 371 km).
 */
public final class HaversineUtil {

    private static final double EARTH_RADIUS_KM = 6_371.0;

    private HaversineUtil() {}

    /**
     * @return khoảng cách tính bằng km, làm tròn 2 chữ số thập phân.
     */
    public static double calculate(double lat1, double lon1,
                                   double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double raw = EARTH_RADIUS_KM * c;

        // Làm tròn 2 chữ số thập phân
        return Math.round(raw * 100.0) / 100.0;
    }
}
