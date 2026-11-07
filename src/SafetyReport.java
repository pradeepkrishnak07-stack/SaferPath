import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SafetyReport {
    private String reportId;
    private Location location;
    private String type;
    private String description;
    private String reporterId;
    private LocalDateTime timestamp;
    
    public SafetyReport(Location location, String type, String description, String reporterId) {
        this.reportId = UUID.randomUUID().toString();
        this.location = location;
        this.type = type;
        this.description = description;
        this.reporterId = reporterId;
        this.timestamp = LocalDateTime.now();
    }
    
    public String getReportId() { return reportId; }
    public Location getLocation() { return location; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public String getReporterId() { return reporterId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("reportId", reportId);
        map.put("location", location.toMap());
        map.put("type", type);
        map.put("description", description);
        map.put("reporterId", reporterId);
        map.put("timestamp", timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return map;
    }
}