import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RouteSegment {
    private Location start;
    private Location end;
    private double distance; 
    private int duration;    
    private int segmentIndex;
    private String instruction;
    private static final Random RANDOM = new Random();
    
    public RouteSegment(Location start, Location end, double distance, int duration, int segmentIndex) {
        this.start = start;
        this.end = end;
        this.distance = distance;
        this.duration = duration;
        this.segmentIndex = segmentIndex;
        this.instruction = generateInstruction();
    }
    
    public RouteSegment(Location start, Location end, double distance, int duration, 
                       int segmentIndex, String instruction) {
        this.start = start;
        this.end = end;
        this.distance = distance;
        this.duration = duration;
        this.segmentIndex = segmentIndex;
        this.instruction = (instruction != null && !instruction.trim().isEmpty()) 
                          ? instruction : generateInstruction();
    }
    
    private String generateInstruction() {
        // Fixed: Added proper initialization
        String[] directions = {
            "Continue straight",
            "Follow the road",
            "Keep going",
            "Stay on this route"
        };
        
        String[] turns = {
            "Turn left",
            "Turn right", 
            "Bear left",
            "Bear right",
            "Make a U-turn"
        };
        
        String[] highways = {
            "Merge onto highway",
            "Take the exit",
            "Follow highway signs"
        };
        
        // Fixed: Added null checks for safety
        if (start == null || end == null) {
            return "Proceed to destination";
        }
        
        if (segmentIndex == 0) {
            return "Start from " + (start.getAddress() != null ? start.getAddress() : "current location");
        }
        
        if (distance > 5000) { 
            int index = Math.abs(RANDOM.nextInt() % highways.length);
            return highways[index] + " toward " + (end.getAddress() != null ? end.getAddress() : "destination");
        }
        
        if (segmentIndex % 3 == 0) { 
            int index = Math.abs(RANDOM.nextInt() % turns.length);
            return turns[index] + " at " + (end.getAddress() != null ? end.getAddress() : "next intersection");
        }
        
        int index = Math.abs(RANDOM.nextInt() % directions.length);
        return directions[index];
    }
    
    // Getters
    public Location getStart() { return start; }
    public Location getEnd() { return end; }
    public double getDistance() { return distance; }
    public int getDuration() { return duration; }
    public int getSegmentIndex() { return segmentIndex; }
    public String getInstruction() { return instruction; }
    
    // Setters with validation
    public void setInstruction(String instruction) { 
        this.instruction = (instruction != null && !instruction.trim().isEmpty()) 
                          ? instruction : this.instruction;
    }
    
    public void setDuration(int duration) { 
        this.duration = Math.max(0, duration); 
    }
    
    public void setDistance(double distance) {
        this.distance = Math.max(0, distance); 
    }
    
    
    public double getDistanceInKm() {
        return distance / 1000.0;
    }
    
    public String getFormattedDistance() {
        if (distance < 1000) {
            return String.format("%.0f m", distance);
        } else {
            return String.format("%.1f km", getDistanceInKm());
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
    
    public boolean isLastSegment(int totalSegments) {
        return segmentIndex == totalSegments - 1;
    }
    
    public boolean isFirstSegment() {
        return segmentIndex == 0;
    }
    
    public double calculateProgress(int totalSegments) {
        if (totalSegments <= 0) return 0;
        return ((double) segmentIndex / Math.max(1, totalSegments - 1)) * 100;
    }
    
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (start != null) map.put("start", start.toMap());
        if (end != null) map.put("end", end.toMap());
        map.put("distance", distance);
        map.put("distanceKm", getDistanceInKm());
        map.put("formattedDistance", getFormattedDistance());
        map.put("duration", duration);
        map.put("formattedDuration", getFormattedDuration());
        map.put("segmentIndex", segmentIndex);
        map.put("instruction", instruction);
        map.put("isFirstSegment", isFirstSegment());
        map.put("coordinates", getCoordinates());
        map.put("requiresAttention", requiresAttention());
        return map;
    }
    
    public Map<String, Object> getCoordinates() {
        Map<String, Object> coords = new HashMap<>();
        if (start != null) {
            coords.put("startLat", start.getLatitude());
            coords.put("startLng", start.getLongitude());
        }
        if (end != null) {
            coords.put("endLat", end.getLatitude());
            coords.put("endLng", end.getLongitude());
        }
        return coords;
    }
    
    public String getSegmentSummary() {
        String startAddr = (start != null && start.getAddress() != null) ? start.getAddress() : "Unknown";
        String endAddr = (end != null && end.getAddress() != null) ? end.getAddress() : "Unknown";
        
        return String.format("Segment %d: %s → %s (%s, %s)", 
            segmentIndex + 1, 
            startAddr, 
            endAddr,
            getFormattedDistance(),
            getFormattedDuration());
    }
    
    @Override
    public String toString() {
        String startAddr = (start != null && start.getAddress() != null) ? start.getAddress() : "Unknown";
        String endAddr = (end != null && end.getAddress() != null) ? end.getAddress() : "Unknown";
        
        return String.format("RouteSegment[%d]: %s to %s (%.1f km, %d min) - %s", 
            segmentIndex, 
            startAddr, 
            endAddr, 
            getDistanceInKm(), 
            duration,
            instruction);
    }
    
    public String getNavigationPrompt() {
        return String.format("%d. %s", segmentIndex + 1, instruction);
    }
    
    public String getDetailedNavigation() {
        return String.format("%d. %s - Continue for %s (%s)", 
            segmentIndex + 1, 
            instruction, 
            getFormattedDistance(),
            getFormattedDuration());
    }
    
    public boolean requiresAttention() {
        if (instruction == null) return false;
        return instruction.contains("Turn") || 
               instruction.contains("Merge") || 
               instruction.contains("Exit") ||
               (distance > 0 && distance < 200); 
    }
    
    public int estimateDurationAtSpeed(double speedKmh) {
        if (speedKmh <= 0) return duration;
        double timeHours = getDistanceInKm() / speedKmh;
        return (int) Math.ceil(timeHours * 60); 
    }
    
    public RouteSegment copy() {
        return new RouteSegment(start, end, distance, duration, segmentIndex, instruction);
    }
    
    public boolean isConsecutiveWith(RouteSegment next) {
        if (this.end == null || next == null || next.start == null) {
            return false;
        }
        
        
        double tolerance = 0.000001; 
        return Math.abs(this.end.getLatitude() - next.start.getLatitude()) < tolerance && 
               Math.abs(this.end.getLongitude() - next.start.getLongitude()) < tolerance;
    }
    
    
    public boolean isValid() {
        return start != null && end != null && distance >= 0 && duration >= 0 && segmentIndex >= 0;
    }
    
    
    public double calculateBearing() {
        if (start == null || end == null) return 0;
        
        double lat1 = Math.toRadians(start.getLatitude());
        double lon1 = Math.toRadians(start.getLongitude());
        double lat2 = Math.toRadians(end.getLatitude());
        double lon2 = Math.toRadians(end.getLongitude());
        
        double dLon = lon2 - lon1;
        
        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - 
                  Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
        
        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (bearing + 360) % 360; 
    }
}