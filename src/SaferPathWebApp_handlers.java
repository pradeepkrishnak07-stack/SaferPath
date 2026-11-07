    static class LocationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                List<Map<String, Object>> locations = new ArrayList<>();
                for (Map.Entry<String, Location> entry : tamilNaduLocations.entrySet()) {
                    Map<String, Object> locationData = entry.getValue().toMap();
                    locationData.put("id", entry.getKey());
                    locations.add(locationData);
                }
                sendJsonResponse(exchange, locations);
            } else {
                exchange.sendResponseHeaders(405, -1); // Method not allowed
            }
        }
    }
    
    static class RoutesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
                String from = params.get("from");
                String to = params.get("to");
                
                if (from == null || to == null) {
                    sendError(exchange, "Missing 'from' or 'to' parameters");
                    return;
                }
                
                List<Route> routes = findConnectingRoutes(from, to);
                List<Map<String, Object>> routeData = new ArrayList<>();
                
                for (Route route : routes) {
                    routeData.add(route.toMap());
                }
                
                sendJsonResponse(exchange, routeData);
            } else {
                exchange.sendResponseHeaders(405, -1); // Method not allowed
            }
        }
        
        private List<Route> findConnectingRoutes(String from, String to) {
            List<Route> connectingRoutes = new ArrayList<>();
            if (tamilNaduLocations.containsKey(from) && tamilNaduLocations.containsKey(to)) {
                String directKey = from + "-" + to;
                if (predefinedRoutes.containsKey(directKey)) {
                    connectingRoutes.addAll(predefinedRoutes.get(directKey));
                }
            }
            return connectingRoutes;
        }
    }
    
    static class NavigationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            
            try {
                if ("/api/navigation/start".equals(path) && "POST".equals(method)) {
                    handleNavigationStart(exchange);
                } else if ("/api/navigation/progress".equals(path) && "GET".equals(method)) {
                    handleNavigationProgress(exchange);
                } else if ("/api/navigation/stop".equals(path) && "POST".equals(method)) {
                    handleNavigationStop(exchange);
                } else {
                    exchange.sendResponseHeaders(404, -1);
                }
            } catch (Exception e) {
                System.err.println("Navigation error: " + e.getMessage());
                e.printStackTrace();
                sendError(exchange, "Navigation error: " + e.getMessage());
            }
        }
        
        private void handleNavigationStart(HttpExchange exchange) throws IOException {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, Object> navData = parseJson(requestBody);
            
            String routeId = (String) navData.get("routeId");
            String userId = (String) navData.getOrDefault("userId", "user_" + System.currentTimeMillis());
            
            Route selectedRoute = null;
            for (List<Route> routes : predefinedRoutes.values()) {
                for (Route route : routes) {
                    if (route.getRouteId().equals(routeId)) {
                        selectedRoute = route;
                        break;
                    }
                }
                if (selectedRoute != null) break;
            }
            
            if (selectedRoute == null) {
                sendError(exchange, "Route not found");
                return;
            }
            
            String sessionId = UUID.randomUUID().toString();
            NavigationSession session = new NavigationSession(sessionId, userId, selectedRoute);
            navigationSessions.put(sessionId, session);
            
            Map<String, Object> response = new HashMap<>();
            response.put("sessionId", sessionId);
            response.put("status", "navigation_started");
            response.put("message", "Navigation started to: " + selectedRoute.getEndLocation().getAddress());
            response.put("session", session.toMap());
            
            sendJsonResponse(exchange, response);
        }
        
        private void handleNavigationProgress(HttpExchange exchange) throws IOException {
            Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
            String sessionId = params.get("sessionId");
            
            NavigationSession session = navigationSessions.get(sessionId);
            if (session == null) {
                sendError(exchange, "Navigation session not found");
                return;
            }
            
            session.updateGPSLocation();
            sendJsonResponse(exchange, session.toMap());
        }
        
        private void handleNavigationStop(HttpExchange exchange) throws IOException {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, Object> data = parseJson(requestBody);
            String sessionId = (String) data.get("sessionId");
            
            NavigationSession session = navigationSessions.remove(sessionId);
            if (session != null) {
                UserLocation userLoc = userLocations.get(session.userId);
                if (userLoc != null) {
                    userLoc.setNavigating(false);
                    userLoc.setNavigationSessionId(null);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Navigation stopped");
            sendJsonResponse(exchange, response);
        }
    }
    
    static class GPSHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
                String deviceId = params.get("deviceId");
                
                if (deviceId == null) {
                    sendError(exchange, "Missing deviceId parameter");
                    return;
                }
                
                GPSLocation location = gpsService.getCurrentLocation(deviceId);
                sendJsonResponse(exchange, location.toMap());
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }
    
    static class CurrentLocationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
                String userId = params.get("userId");
                
                if (userId == null) {
                    sendError(exchange, "Missing userId parameter");
                    return;
                }
                
                UserLocation userLocation = userLocations.computeIfAbsent(userId, UserLocation::new);
                GPSLocation currentLocation = userLocation.getCurrentLocation();
                
                if (currentLocation == null) {
                    currentLocation = gpsService.getCurrentLocation(userId);
                    userLocation.updateLocation(currentLocation);
                }
                
                Map<String, Object> response = new HashMap<>();
                response.put("location", currentLocation.toMap());
                response.put("isNavigating", userLocation.isNavigating());
                response.put("navigationSessionId", userLocation.getNavigationSessionId());
                
                sendJsonResponse(exchange, response);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }
    
    static class NetworkInfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                Map<String, Object> info = new HashMap<>();
                try {
                    info.put("localIP", InetAddress.getLocalHost().getHostAddress());
                    info.put("hostname", InetAddress.getLocalHost().getHostName());
                    info.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                } catch (Exception e) {
                    info.put("error", e.getMessage());
                }
                sendJsonResponse(exchange, info);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String htmlContent = getHTMLContent();
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, htmlContent.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(htmlContent.getBytes(StandardCharsets.UTF_8));
            }
        }
        
        private String getHTMLContent() {
            return "<!DOCTYPE html>\n" +
                   "<html>\n" +
                   "<head>\n" +
                   "    <title>SaferPath - Tamil Nadu Navigation</title>\n" +
                   "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                   "</head>\n" +
                   "<body>\n" +
                   "    <h1>SaferPath - Tamil Nadu Navigation</h1>\n" +
                   "    <p>Server is running. Use the mobile app to access navigation features.</p>\n" +
                   "</body>\n" +
                   "</html>";
        }
    }