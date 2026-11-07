import java.util.*;
import java.util.stream.Collectors;

public class Route {
    private String routeId;
    private String name;
    private Location startLocation;
    private Location endLocation;
    private int duration; 
    private double distance; 
    private int safetyScore;
    private List<String> safetyFactors;
    private SafetyLevel safetyLevel;
    private List<Location> waypoints;
    private static final Random RANDOM = new Random();
    
    public Route(String name, Location startLocation, Location endLocation, int duration, double distance) {
        this.routeId = UUID.randomUUID().toString().substring(0, 8);
        this.name = name;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.duration = duration;
        this.distance = distance;
        this.safetyFactors = new ArrayList<>();
        this.waypoints = generateWaypoints();
        calculateSafetyScore();
    }
    
    private List<Location> generateWaypoints() {
        List<Location> waypoints = new ArrayList<>();
        
        
        if (startLocation == null || endLocation == null) {
            return waypoints;
        }
        
        int numWaypoints = 2 + RANDOM.nextInt(3); 
        
        for (int i = 1; i <= numWaypoints; i++) {
            double progress = i / (double)(numWaypoints + 1);
            
            
            double lat = interpolate(startLocation.getLatitude(), endLocation.getLatitude(), progress);
            double lon = interpolate(startLocation.getLongitude(), endLocation.getLongitude(), progress);
            
            String[] waypointTypes = {"Intersection", "Highway Exit", "Landmark", "Traffic Signal", "Roundabout"};
            String type = waypointTypes[RANDOM.nextInt(waypointTypes.length)];
            String address = type + " at " + String.format("%.0f", progress * 100) + "% of journey";
            
            waypoints.add(new Location(lat, lon, address));
        }
        return waypoints;
    }
    
    private double interpolate(double start, double end, double progress) {
        return start + (end - start) * progress;
    }
    
    private void calculateSafetyScore() {
        
        int baseScore = 50;
        
        
        double distanceFactor = Math.max(0, 30 - (distance / 1000)); 
        baseScore += Math.min(distanceFactor, 30);
        
        
        int randomFactor = RANDOM.nextInt(20);
        baseScore += randomFactor;
        
        
        int timeFactor = RANDOM.nextInt(10);
        baseScore += timeFactor;
        
        this.safetyScore = Math.max(0, Math.min(100, baseScore)); 
        this.safetyLevel = SafetyLevel.fromScore(safetyScore);
        
        
        safetyFactors.clear();
        
        
        if (safetyScore >= 80) {
            safetyFactors.add("Well-lit highways");
            safetyFactors.add("Regular police patrols");
            safetyFactors.add("Emergency services available");
            safetyFactors.add("Low crime rate area");
        } else if (safetyScore >= 60) {
            safetyFactors.add("Moderate traffic conditions");
            safetyFactors.add("Adequate lighting");
            safetyFactors.add("Some emergency services");
            safetyFactors.add("Mixed residential/commercial area");
        } else if (safetyScore >= 40) {
            safetyFactors.add("Heavy traffic areas");
            safetyFactors.add("Limited lighting");
            safetyFactors.add("Use caution");
            safetyFactors.add("Industrial area");
        } else {
            safetyFactors.add("Poor lighting conditions");
            safetyFactors.add("High crime rate area");
            safetyFactors.add("Limited emergency services");
            safetyFactors.add("Avoid if possible");
        }
        
        
        if (distance > 10000) { 
            safetyFactors.add("Long distance - plan for breaks");
        }
    }
    
    
    public String getRouteId() { return routeId; }
    public String getName() { return name; }
    public Location getStartLocation() { return startLocation; }
    public Location getEndLocation() { return endLocation; }
    public int getDuration() { return duration; }
    public double getDistance() { return distance; }
    public int getSafetyScore() { return safetyScore; }
    public List<String> getSafetyFactors() { return new ArrayList<>(safetyFactors); } 
    public SafetyLevel getSafetyLevel() { return safetyLevel; }
    public List<Location> getWaypoints() { return new ArrayList<>(waypoints); } 
    
    
    public void setName(String name) { 
        this.name = (name != null && !name.trim().isEmpty()) ? name : "Unnamed Route";
    }
    
    public void setDuration(int duration) { 
        this.duration = Math.max(0, duration);
    }
    
    public void setDistance(double distance) { 
        this.distance = Math.max(0, distance);
    }
    
    public void updateSafetyScore(int newScore) {
        this.safetyScore = Math.max(0, Math.min(100, newScore));
        this.safetyLevel = SafetyLevel.fromScore(safetyScore);
        calculateSafetyScore(); 
    }
    
    
    public String getFormattedDistance() {
        if (distance < 1000) {
            return String.format("%.0f m", distance);
        } else {
            return String.format("%.1f km", distance / 1000.0);
        }
    }
    
    public String getFormattedDuration() {
        if (duration < 60) {
            return String.format("%d min", duration);
        } else {
            int hours = duration / 60;
            int minutes = duration % 60;
            if (minutes == 0) {
                return String.format("%d hour%s", hours, hours > 1 ? "s" : "");
            }
            return String.format("%d h %d min", hours, minutes);
        }
    }
    
    public double getAverageSpeed() {
        if (duration == 0) return 0;
        return (distance / 1000.0) / (duration / 60.0); // km/h
    }
    
    public boolean isSafeRoute() {
        return safetyScore >= 60;
    }
    
    public boolean isVerySafeRoute() {
        return safetyScore >= 80;
    }
    
    public void addSafetyFactor(String factor) {
        if (factor != null && !factor.trim().isEmpty()) {
            safetyFactors.add(factor);
        }
    }
    
    public void removeSafetyFactor(String factor) {
        safetyFactors.remove(factor);
    }
    
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("routeId", routeId);
        map.put("name", name);
        map.put("startLocation", startLocation != null ? startLocation.toMap() : null);
        map.put("endLocation", endLocation != null ? endLocation.toMap() : null);
        map.put("duration", duration);
        map.put("formattedDuration", getFormattedDuration());
        map.put("distance", distance);
        map.put("formattedDistance", getFormattedDistance());
        map.put("safetyScore", safetyScore);
        map.put("safetyFactors", new ArrayList<>(safetyFactors));
        map.put("safetyLevel", safetyLevel != null ? safetyLevel.name() : "UNKNOWN");
        map.put("safetyEmoji", safetyLevel != null ? safetyLevel.getEmoji() : "❓");
        map.put("waypoints", waypoints.stream()
                .map(Location::toMap)
                .collect(Collectors.toList()));
        map.put("isSafe", isSafeRoute());
        map.put("averageSpeed", String.format("%.1f km/h", getAverageSpeed()));
        return map;
    }
    
    @Override
    public String toString() {
        return String.format("Route[%s]: %s → %s (%s, %s, Safety: %d/100)",
                routeId,
                startLocation != null ? startLocation.getAddress() : "Unknown",
                endLocation != null ? endLocation.getAddress() : "Unknown",
                getFormattedDistance(),
                getFormattedDuration(),
                safetyScore);
    }
    
    public String getRouteSummary() {
        return String.format("%s: %s to %s | %s | %s | Safety: %s",
                name,
                startLocation != null ? startLocation.getAddress() : "Start",
                endLocation != null ? endLocation.getAddress() : "End",
                getFormattedDistance(),
                getFormattedDuration(),
                safetyLevel != null ? safetyLevel.getDisplayName() : "Unknown");
    }
    
    
    public boolean isShorterThan(Route other) {
        return this.distance < other.distance;
    }
    
    public boolean isSaferThan(Route other) {
        return this.safetyScore > other.safetyScore;
    }
    
    public boolean isFasterThan(Route other) {
        return this.duration < other.duration;
    }
    
    
    public static Route findSafestRoute(List<Route> routes) {
        return routes.stream()
                .max(Comparator.comparingInt(Route::getSafetyScore))
                .orElse(null);
    }
    
    public static Route findShortestRoute(List<Route> routes) {
        return routes.stream()
                .min(Comparator.comparingDouble(Route::getDistance))
                .orElse(null);
    }
    
    public static Route findFastestRoute(List<Route> routes) {
        return routes.stream()
                .min(Comparator.comparingInt(Route::getDuration))
                .orElse(null);
    }
}