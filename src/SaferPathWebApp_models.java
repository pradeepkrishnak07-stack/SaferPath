    static class Location {
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
    
    static class GPSLocation {
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
        
        private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
            double earthRadius = 6371; // kilometers
            double dLat = Math.toRadians(lat2 - lat1);
            double dLng = Math.toRadians(lng2 - lng1);
            double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                      Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                      Math.sin(dLng/2) * Math.sin(dLng/2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            return earthRadius * c;
        }
    }
    
    static class Route {
        private final String routeId;
        private final String name;
        private final Location startLocation;
        private final Location endLocation;
        private final int duration;
        private final int distance;
        private final List<Location> waypoints;
        private final int safetyScore;
        private final List<String> safetyFactors;
        
        public Route(String name, Location startLocation, Location endLocation, int duration, int distance) {
            this.routeId = UUID.randomUUID().toString();
            this.name = name;
            this.startLocation = startLocation;
            this.endLocation = endLocation;
            this.duration = duration;
            this.distance = distance;
            this.waypoints = new ArrayList<>();
            this.safetyScore = new Random().nextInt(40) + 60; // 60-100
            this.safetyFactors = generateSafetyFactors();
        }
        
        private List<String> generateSafetyFactors() {
            List<String> factors = new ArrayList<>();
            Random random = new Random();
            
            if (random.nextBoolean()) factors.add("Well lit roads");
            if (random.nextBoolean()) factors.add("Police patrol presence");
            if (random.nextBoolean()) factors.add("Emergency services nearby");
            if (random.nextBoolean()) factors.add("Good road conditions");
            if (random.nextBoolean()) factors.add("CCTV surveillance");
            if (random.nextBoolean()) factors.add("Active traffic management");
            if (random.nextBoolean()) factors.add("Clear signage");
            if (random.nextBoolean()) factors.add("Regular maintenance");
            
            return factors;
        }
        
        public String getRouteId() { return routeId; }
        public String getName() { return name; }
        public Location getStartLocation() { return startLocation; }
        public Location getEndLocation() { return endLocation; }
        public int getDuration() { return duration; }
        public int getDistance() { return distance; }
        public List<Location> getWaypoints() { return waypoints; }
        public int getSafetyScore() { return safetyScore; }
        public List<String> getSafetyFactors() { return safetyFactors; }
        
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
    
    static class SafetyReport {
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
    
    static class UserLocation {
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
        
        public void updateLocation(GPSLocation location) {
            this.currentLocation = location;
            this.lastUpdate = LocalDateTime.now();
        }
        
        public String getUserId() { return userId; }
        public GPSLocation getCurrentLocation() { return currentLocation; }
        public LocalDateTime getLastUpdate() { return lastUpdate; }
        public boolean isNavigating() { return isNavigating; }
        public void setNavigating(boolean navigating) { isNavigating = navigating; }
        public String getNavigationSessionId() { return navigationSessionId; }
        public void setNavigationSessionId(String sessionId) { this.navigationSessionId = sessionId; }
    }
    
    static class NavigationSession {
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
        
        private double calculateDistance(GPSLocation loc1, GPSLocation loc2) {
            double lat1 = loc1.getLatitude();
            double lon1 = loc1.getLongitude();
            double lat2 = loc2.getLatitude();
            double lon2 = loc2.getLongitude();
            
            double dLat = Math.toRadians(lat2 - lat1);
            double dLon = Math.toRadians(lon2 - lon1);
            double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                      Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                      Math.sin(dLon/2) * Math.sin(dLon/2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            return 6371 * c;
        }
        
        public void updateGPSLocation() {
            GPSLocation newLocation = gpsService.simulateMovement(userId, route.getEndLocation());
            
            if (newLocation != null) {
                distanceTraveled += calculateDistance(currentGPSLocation, newLocation);
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
        
        public int getRemainingTime() {
            return (int) (route.getDuration() * (1 - progress / 100));
        }
        
        public int getRemainingDistance() {
            return (int) (route.getDistance() * (1 - progress / 100));
        }
        
        public String getCurrentInstruction() {
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
        
        public String getNextVoiceInstruction() {
            String instruction = getCurrentInstruction();
            if (!spokenInstructions.contains(instruction)) {
                spokenInstructions.add(instruction);
                return instruction;
            }
            return null;
        }
        
        public List<Map<String, Object>> getRoutePath() {
            List<Map<String, Object>> path = new ArrayList<>();
            path.add(currentGPSLocation.toMap());
            path.addAll(route.getWaypoints().stream().map(Location::toMap).collect(Collectors.toList()));
            path.add(route.getEndLocation().toMap());
            return path;
        }
        
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("sessionId", sessionId);
            map.put("route", route.toMap());
            map.put("currentStep", currentStep);
            map.put("totalSteps", totalSteps);
            map.put("progress", progress);
            map.put("remainingTime", getRemainingTime());
            map.put("remainingDistance", getRemainingDistance());
            map.put("currentInstruction", getCurrentInstruction());
            map.put("nextVoiceInstruction", getNextVoiceInstruction());
            map.put("isActive", isActive);
            map.put("currentLocation", currentGPSLocation.toMap());
            map.put("speed", currentGPSLocation.getSpeed());
            map.put("routePath", getRoutePath());
            return map;
        }
    }
    
    static class GPSService {
        private final Map<String, GPSDevice> devices = new ConcurrentHashMap<>();
        
        public GPSLocation getCurrentLocation(String deviceId) {
            if (!devices.containsKey(deviceId)) {
                devices.put(deviceId, new GPSDevice(deviceId));
            }
            GPSDevice device = devices.get(deviceId);
            return device.getCurrentLocation();
        }
        
        public GPSLocation simulateMovement(String deviceId, Location destination) {
            GPSDevice device = devices.get(deviceId);
            if (device == null) {
                device = new GPSDevice(deviceId);
                devices.put(deviceId, device);
            }
            return device.moveToward(destination);
        }
    }
    
    static class GPSDevice {
        private final String deviceId;
        private double latitude;
        private double longitude;
        private double speed;
        private double bearing;
        private final Random random;
        
        public GPSDevice(String deviceId) {
            this.deviceId = deviceId;
            this.random = new Random(deviceId.hashCode());
            this.latitude = 10.0 + (random.nextDouble() * 3.0);
            this.longitude = 77.0 + (random.nextDouble() * 3.0);
            this.speed = 0;
            this.bearing = 0;
        }
        
        public GPSLocation getCurrentLocation() {
            if (speed > 0) {
                double distance = speed * 0.00001; // Rough conversion to coordinate units
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