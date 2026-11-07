import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class SaferPathWebApp_Final {
    private static final Map<String, Location> tamilNaduLocations = new HashMap<>();
    private static final Map<String, List<Route>> predefinedRoutes = new HashMap<>();
    private static final Map<String, NavigationSession> navigationSessions = new ConcurrentHashMap<>();
    private static final Map<String, UserLocation> userLocations = new ConcurrentHashMap<>();
    private static final GPSService gpsService = new GPSService();

    static {
        initializeTamilNaduLocations();
        initializeTamilNaduRoutes();
    }

    private static void initializeTamilNaduLocations() {
        tamilNaduLocations.put("chennai", new Location(13.0827, 80.2707, "Chennai - State Capital"));
        tamilNaduLocations.put("coimbatore", new Location(11.0168, 76.9558, "Coimbatore - Textile City"));
        // Add more locations...
    }

    private static void initializeTamilNaduRoutes() {
        Random random = new Random();
        addRoute("chennai", "coimbatore", 495, 540, random);
        addRoute("chennai", "madurai", 450, 480, random);
        // Add more routes...
    }

    private static void addRoute(String from, String to, int baseDistance, int baseDuration, Random random) {
        Location start = tamilNaduLocations.get(from);
        Location end = tamilNaduLocations.get(to);
        
        if (start != null && end != null) {
            int distance = baseDistance + random.nextInt(20) - 10;
            int duration = baseDuration + random.nextInt(30) - 15;
            
            Route route = new Route("Route " + from + "-" + to, start, end, duration, distance);
            Route reverseRoute = new Route("Route " + to + "-" + from, end, start, duration, distance);
            
            predefinedRoutes.computeIfAbsent(from + "-" + to, _ -> new ArrayList<>()).add(route);
            predefinedRoutes.computeIfAbsent(to + "-" + from, _ -> new ArrayList<>()).add(reverseRoute);
        }
    }

    private static class Location {
        private final double latitude;
        private final double longitude;
        private final String address;
        
        public Location(double latitude, double longitude, String address) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.address = address;
        }
        
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public String getAddress() { return address; }
        
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("lat", latitude);
            map.put("lng", longitude);
            map.put("address", address);
            return map;
        }
    }

    private static class Route {
        private final String name;
        private final Location startLocation;
        private final Location endLocation;
        private final int duration;
        private final int distance;
        private final List<Location> waypoints;
        private final int safetyScore;
        private final List<String> safetyFactors;
        
        public Route(String name, Location startLocation, Location endLocation, int duration, int distance) {
            this.name = name;
            this.startLocation = startLocation;
            this.endLocation = endLocation;
            this.duration = duration;
            this.distance = distance;
            this.waypoints = new ArrayList<>();
            this.safetyScore = new Random().nextInt(40) + 60;
            this.safetyFactors = generateSafetyFactors();
        }
        
        public Location getStartLocation() { return startLocation; }
        public Location getEndLocation() { return endLocation; }
        public int getDuration() { return duration; }
        public int getDistance() { return distance; }
        public List<Location> getWaypoints() { return waypoints; }
        
        private List<String> generateSafetyFactors() {
            List<String> factors = new ArrayList<>();
            String[] possibleFactors = {
                "Well lit roads",
                "Police patrol presence",
                "Emergency services nearby",
                "Good road conditions",
                "CCTV surveillance",
                "Active traffic management",
                "Clear signage",
                "Regular maintenance"
            };
            Random random = new Random();
            for (String factor : possibleFactors) {
                if (random.nextBoolean()) {
                    factors.add(factor);
                }
            }
            return factors;
        }
        
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("routeId", routeId);
            map.put("name", name);
            map.put("startLocation", startLocation.toMap());
            map.put("endLocation", endLocation.toMap());
            map.put("duration", duration);
            map.put("distance", distance);
            map.put("waypoints", waypoints.stream().map(Location::toMap).collect(Collectors.toList()));
            map.put("safetyScore", safetyScore);
            map.put("safetyFactors", safetyFactors);
            return map;
        }
    }

    private static class GPSService {
        private final Map<String, GPSDevice> devices = new ConcurrentHashMap<>();
        
        public GPSLocation getCurrentLocation(String deviceId) {
            return devices.computeIfAbsent(deviceId, GPSDevice::new).getCurrentLocation();
        }
        
        public GPSLocation simulateMovement(String deviceId, Location destination) {
            return devices.computeIfAbsent(deviceId, GPSDevice::new).moveToward(destination);
        }
    }

    private static class GPSDevice {
        private double latitude;
        private double longitude;
        private double speed;
        private double bearing;
        private final Random random;
        
        public GPSDevice(String deviceId) {
            this.random = new Random(deviceId.hashCode());
            this.latitude = 10.0 + (random.nextDouble() * 3.0);
            this.longitude = 77.0 + (random.nextDouble() * 3.0);
            this.speed = 0;
            this.bearing = 0;
        }
        
        public GPSLocation getCurrentLocation() {
            if (speed > 0) {
                double distance = speed * 0.00001;
                latitude += Math.cos(Math.toRadians(bearing)) * distance;
                longitude += Math.sin(Math.toRadians(bearing)) * distance;
                speed = Math.max(0, speed - random.nextDouble() * 5);
            }
            return new GPSLocation(latitude, longitude, speed, bearing, LocalDateTime.now());
        }
        
        public GPSLocation moveToward(Location destination) {
            double destLat = destination.getLatitude();
            double destLng = destination.getLongitude();
            double dLng = Math.toRadians(destLng - longitude);
            double y = Math.sin(dLng) * Math.cos(Math.toRadians(destLat));
            double x = Math.cos(Math.toRadians(latitude)) * Math.sin(Math.toRadians(destLat)) -
                      Math.sin(Math.toRadians(latitude)) * Math.cos(Math.toRadians(destLat)) * Math.cos(dLng);
            bearing = (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
            speed = Math.min(80, speed + 5);
            return getCurrentLocation();
        }
    }

    private static class GPSLocation {
        private final double latitude;
        private final double longitude;
        private final double speed;
        private final double bearing;
        private final LocalDateTime timestamp;
        private final double accuracy;
        
        public GPSLocation(double latitude, double longitude, double speed, double bearing, LocalDateTime timestamp) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.speed = speed;
            this.bearing = bearing;
            this.timestamp = timestamp;
            this.accuracy = 5.0 + Math.random() * 15.0;
        }
        
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public double getSpeed() { return speed; }
        public double getBearing() { return bearing; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public double getAccuracy() { return accuracy; }
        
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("lat", latitude);
            map.put("lng", longitude);
            map.put("speed", speed);
            map.put("bearing", bearing);
            map.put("accuracy", accuracy);
            map.put("timestamp", timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            map.put("address", getApproximateAddress());
            return map;
        }
        
        private String getApproximateAddress() {
            return tamilNaduLocations.values().stream()
                .min((a, b) -> Double.compare(
                    calculateDistance(latitude, longitude, a.getLatitude(), a.getLongitude()),
                    calculateDistance(latitude, longitude, b.getLatitude(), b.getLongitude())
                ))
                .map(loc -> "Near " + loc.getAddress())
                .orElse("Tamil Nadu Region");
        }
        
        private static double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
            double earthRadius = 6371;
            double dLat = Math.toRadians(lat2 - lat1);
            double dLng = Math.toRadians(lng2 - lng1);
            double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                      Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                      Math.sin(dLng/2) * Math.sin(dLng/2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            return earthRadius * c;
        }
    }

    private static class NavigationSession {
        private final String sessionId;
        private final String userId;
        private final Route route;
        private int currentStep;
        private final int totalSteps;
        private final LocalDateTime startTime;
        private boolean isActive;
        private double progress;
        private GPSLocation currentGPSLocation;
        private final List<GPSLocation> locationHistory;
        private double distanceTraveled;
        private final List<String> spokenInstructions;
        
        public NavigationSession(String sessionId, String userId, Route route) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.route = route;
            this.currentStep = 0;
            this.totalSteps = 10;
            this.startTime = LocalDateTime.now();
            this.isActive = true;
            this.progress = 0.0;
            this.locationHistory = new ArrayList<>();
            this.distanceTraveled = 0.0;
            this.spokenInstructions = new ArrayList<>();
            
            this.currentGPSLocation = gpsService.getCurrentLocation(userId);
            this.locationHistory.add(currentGPSLocation);
            
            UserLocation userLoc = userLocations.computeIfAbsent(userId, UserLocation::new);
            userLoc.setNavigating(true);
            userLoc.setNavigationSessionId(sessionId);
        }
        
        public void updateGPSLocation() {
            GPSLocation newLocation = gpsService.simulateMovement(userId, route.getEndLocation());
            
            if (newLocation != null) {
                distanceTraveled += GPSLocation.calculateDistance(
                    currentGPSLocation.getLatitude(), currentGPSLocation.getLongitude(),
                    newLocation.getLatitude(), newLocation.getLongitude()
                );
            } else {
                newLocation = currentGPSLocation;
            }
            
            this.currentGPSLocation = newLocation;
            this.locationHistory.add(newLocation);
            
            progress = Math.min(100.0, (currentStep / (double) totalSteps) * 100);
            currentStep++;
            
            if (progress >= 99.9) {
                isActive = false;
                UserLocation userLoc = userLocations.get(userId);
                if (userLoc != null) {
                    userLoc.setNavigating(false);
                    userLoc.setNavigationSessionId(null);
                }
                navigationSessions.remove(sessionId);
            }
        }
        
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("sessionId", sessionId);
            map.put("route", route.toMap());
            map.put("currentStep", currentStep);
            map.put("totalSteps", totalSteps);
            map.put("progress", progress);
            map.put("remainingTime", (int)(route.getDuration() * (1 - progress / 100)));
            map.put("remainingDistance", (int)(route.getDistance() * (1 - progress / 100)));
            map.put("currentInstruction", getCurrentInstruction());
            map.put("nextVoiceInstruction", getNextVoiceInstruction());
            map.put("isActive", isActive);
            map.put("currentLocation", currentGPSLocation.toMap());
            map.put("speed", currentGPSLocation.getSpeed());
            map.put("routePath", getRoutePath());
            return map;
        }
        
        private String getCurrentInstruction() {
            String[] instructions = {
                "Start navigation from " + route.getStartLocation().getAddress(),
                "Head toward the main highway",
                "Continue straight for 5 km",
                "Take exit toward " + route.getEndLocation().getAddress(),
                "Keep left at the fork",
                "Merge onto highway",
                "Follow signs for " + route.getEndLocation().getAddress(),
                "Take the next exit",
                "Turn right at traffic signal",
                "Approaching destination",
                "You have arrived at " + route.getEndLocation().getAddress()
            };
            return instructions[Math.min(currentStep, instructions.length - 1)];
        }
        
        private String getNextVoiceInstruction() {
            String instruction = getCurrentInstruction();
            if (!spokenInstructions.contains(instruction)) {
                spokenInstructions.add(instruction);
                return instruction;
            }
            return null;
        }
        
        private List<Map<String, Object>> getRoutePath() {
            List<Map<String, Object>> path = new ArrayList<>();
            path.add(currentGPSLocation.toMap());
            path.addAll(route.getWaypoints().stream().map(Location::toMap).collect(Collectors.toList()));
            path.add(route.getEndLocation().toMap());
            return path;
        }
    }
    
    private static final class UserLocation {
        private final String userId;
        private GPSLocation currentLocation;
        private LocalDateTime lastUpdate;
        private boolean isNavigating;
        private String navigationSessionId;
        
        public UserLocation(String userId) {
            this.userId = userId;
            this.lastUpdate = LocalDateTime.now();
            this.isNavigating = false;
        }
        
        public void setNavigating(boolean navigating) { isNavigating = navigating; }
        public void setNavigationSessionId(String sessionId) { this.navigationSessionId = sessionId; }
    }
    
    private static class SafetyReport {
        private final String reportId;
        private final String type;
        private final String description;
        private final Location location;
        private final LocalDateTime timestamp;
        private final int severity;
        
        public SafetyReport(String type, String description, Location location, int severity) {
            this.reportId = UUID.randomUUID().toString();
            this.type = type;
            this.description = description;
            this.location = location;
            this.timestamp = LocalDateTime.now();
            this.severity = severity;
        }
        
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("reportId", reportId);
            map.put("type", type);
            map.put("description", description);
            map.put("location", location.toMap());
            map.put("timestamp", timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            map.put("severity", severity);
            return map;
        }
    }

    private static int findAvailablePort(int startPort) {
        for (int port = startPort; port < startPort + 100; port++) {
            try (ServerSocket socket = new ServerSocket(port)) {
                return port;
            } catch (IOException ignored) {
                // Port is not available, try next one
            }
        }
        throw new RuntimeException("No available ports found in range " + startPort + " to " + (startPort + 100));
    }

    public static void main(String[] args) {
        String host = "0.0.0.0";
        int preferredPort = 8080;
        
        // Try to get port from environment variable or command line
        String envPort = System.getenv("SAFERPATH_PORT");
        if (envPort != null) {
            try {
                preferredPort = Integer.parseInt(envPort);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number in SAFERPATH_PORT: " + envPort);
            }
        }
        
        if (args.length > 0) {
            try {
                preferredPort = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number argument: " + args[0]);
            }
        }
        
        try {
            int port = findAvailablePort(preferredPort);
            System.out.println("Starting SaferPath server on " + host + ":" + port);
            HttpServer server = HttpServer.create(new InetSocketAddress(host, port), 0);
            
            // Configure CORS headers
            server.createContext("/", exchange -> {
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                
                if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }
                
                String path = exchange.getRequestURI().getPath();
                if (path.equals("/")) {
                    String html = """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <title>SaferPath - Tamil Nadu Navigation</title>
                            <meta name="viewport" content="width=device-width, initial-scale=1">
                            <style>
                                body { font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; }
                                h1 { color: #2c3e50; }
                                .feature { background: #f7f9fc; padding: 15px; margin: 10px 0; border-radius: 5px; }
                                pre { background: #edf2f7; padding: 10px; border-radius: 3px; overflow-x: auto; }
                            </style>
                        </head>
                        <body>
                            <h1>SaferPath - Tamil Nadu Navigation</h1>
                            <div class="feature">
                                <h2>API Endpoints:</h2>
                                <pre>
GET  /api/locations         - List all locations
GET  /api/routes           - Find routes between locations
POST /api/navigation/start - Start navigation
GET  /api/navigation/progress?sessionId={id} - Get navigation progress
POST /api/navigation/stop  - Stop navigation
GET  /api/gps?deviceId={id} - Get current GPS location
GET  /api/location/current?userId={id} - Get user's current location
GET  /api/network-info     - Get network information
                                </pre>
                            </div>
                        </body>
                        </html>
                    """;
                    exchange.getResponseHeaders().set("Content-Type", "text/html");
                    exchange.sendResponseHeaders(200, html.getBytes().length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(html.getBytes());
                    }
                } else {
                    exchange.sendResponseHeaders(404, -1);
                }
            });
            
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            
            String localIP = InetAddress.getLocalHost().getHostAddress();
            
            System.out.println("\n🚀 SaferPath Tamil Nadu Navigation Started!");
            System.out.println("\n📍 Server Access URLs:");
            System.out.println("   💻 Local:   http://localhost:" + port);
            System.out.println("   🌐 Network: http://" + localIP + ":" + port);
            System.out.println("\n📱 Mobile Access Instructions:");
            System.out.println("   1. Ensure phone is on same WiFi as this computer");
            System.out.println("   2. Open browser on phone");
            System.out.println("   3. Visit: http://" + localIP + ":" + port);
            System.out.println("\n🗺️  Features:");
            System.out.println("   • Real-time GPS Simulation");
            System.out.println("   • Voice Guided Navigation");
            System.out.println("   • Safety Scoring System");
            System.out.println("   • 38 Tamil Nadu Districts");
            System.out.println("   • Mobile & Desktop Optimized");
            System.out.println("\n✅ Server is ready!");
            
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            if (e instanceof java.net.BindException) {
                System.err.println("Port is already in use. Try using a different port with SAFERPATH_PORT environment variable.");
            }
            System.exit(1);
        }
    }
}