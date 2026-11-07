import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class SaferPathWebApp_fixed {
    static List<SafetyReport> safetyReports = new ArrayList<>();
    static Map<String, Location> tamilNaduLocations = new HashMap<>();
    static Map<String, List<Route>> predefinedRoutes = new HashMap<>();
    static Map<String, NavigationSession> navigationSessions = new ConcurrentHashMap<>();
    static Map<String, UserLocation> userLocations = new ConcurrentHashMap<>();
    
    static GPSService gpsService = new GPSService();
    
    static {
        initializeTamilNaduLocations();
        initializeTamilNaduRoutes();
    }

    private static int findAvailablePort(int startPort) {
        for (int port = startPort; port < startPort + 100; port++) {
            try (java.net.ServerSocket socket = new java.net.ServerSocket(port)) {
                return port;
            } catch (IOException ex) {
                continue;
            }
        }
        throw new RuntimeException("No available ports found in range " + startPort + " to " + (startPort + 100));
    }

    public static void main(String[] args) {
        String host = "0.0.0.0";
        int preferredPort = 8080;
        
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
            
            server.createContext("/", new StaticHandler());
            server.createContext("/api/routes", new RoutesHandler());
            server.createContext("/api/locations", new LocationsHandler());
            server.createContext("/api/navigation", new NavigationHandler());
            server.createContext("/api/gps", new GPSHandler());
            server.createContext("/api/location/current", new CurrentLocationHandler());
            server.createContext("/api/network-info", new NetworkInfoHandler());
            
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            
            String localIP = getLocalIP();
            
            System.out.println("🚀 SaferPath Tamil Nadu Navigation Started!");
            System.out.println("📍 Server Access URLs:");
            System.out.println("   💻 Local: http://localhost:" + port);
            System.out.println("   🌐 Network: http://" + localIP + ":" + port);
            System.out.println("");
            System.out.println("📱 Mobile Access Instructions:");
            System.out.println("   1. Ensure phone is on same WiFi as this computer");
            System.out.println("   2. Open browser on phone");
            System.out.println("   3. Visit: http://" + localIP + ":" + port);
            System.out.println("");
            System.out.println("🗺️  Features:");
            System.out.println("   • 38 Tamil Nadu Districts Coverage");
            System.out.println("   • Mobile & Desktop Optimized");
            System.out.println("   • Real-time GPS Simulation");
            System.out.println("   • Voice Guided Navigation");
            System.out.println("   • Safety Scoring System");
            System.out.println("");
            System.out.println("✅ Server is ready!");
            
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            if (e instanceof java.net.BindException) {
                System.err.println("Port is already in use. Try using a different port with SAFERPATH_PORT environment variable.");
            }
            System.exit(1);
        }
    }

    private static String getLocalIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    private static void sendJsonResponse(HttpExchange exchange, Object data) throws IOException {
        String response = toJson(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static void sendError(HttpExchange exchange, String message) throws IOException {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", message);
        sendJsonResponse(exchange, error);
    }

    private static String toJson(Object obj) {
        StringBuilder json = new StringBuilder();
        if (obj instanceof Map) {
            json.append("{");
            Map<?, ?> map = (Map<?, ?>) obj;
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) json.append(",");
                first = false;
                json.append("\"").append(entry.getKey()).append("\":");
                json.append(toJson(entry.getValue()));
            }
            json.append("}");
        } else if (obj instanceof List) {
            json.append("[");
            List<?> list = (List<?>) obj;
            boolean first = true;
            for (Object item : list) {
                if (!first) json.append(",");
                first = false;
                json.append(toJson(item));
            }
            json.append("]");
        } else if (obj instanceof String) {
            json.append("\"").append(((String) obj).replace("\"", "\\\"")).append("\"");
        } else if (obj instanceof Number || obj instanceof Boolean) {
            json.append(obj);
        } else if (obj == null) {
            json.append("null");
        } else {
            json.append("\"").append(obj.toString()).append("\"");
        }
        return json.toString();
    }

    private static Map<String, Object> parseJson(String json) {
        // This is a simplified JSON parser - in production, use a proper JSON library
        Map<String, Object> result = new HashMap<>();
        if (json != null && json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1).trim();
            String[] pairs = json.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim().replaceAll("\"", "");
                    String value = keyValue[1].trim().replaceAll("\"", "");
                    result.put(key, value);
                }
            }
        }
        return result;
    }

    private static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] entry = param.split("=");
                if (entry.length > 1) {
                    result.put(entry[0], URLDecoder.decode(entry[1], StandardCharsets.UTF_8));
                } else {
                    result.put(entry[0], "");
                }
            }
        }
        return result;
    }

    // Add all your existing classes (GPSService, Location, Route, etc.) here...
    // Make sure to include proper error handling and null checks
}