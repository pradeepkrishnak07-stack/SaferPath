public enum SafetyLevel {
    VERY_SAFE(80, 100, "🟢", "Very Safe"),
    SAFE(60, 79, "🟡", "Safe"),
    MODERATE(40, 59, "🟠", "Moderate"),
    UNSAFE(0, 39, "🔴", "Unsafe");
    
    private final int minScore;
    private final int maxScore;
    private final String emoji;
    private final String displayName;
    
    SafetyLevel(int minScore, int maxScore, String emoji, String displayName) {
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.emoji = emoji;
        this.displayName = displayName;
    }
    
    public static SafetyLevel fromScore(int score) {
        for (SafetyLevel level : values()) {
            if (score >= level.minScore && score <= level.maxScore) {
                return level;
            }
        }
        return UNSAFE;
    }
    
    public String getEmoji() { return emoji; }
    public String getDisplayName() { return displayName; }
    public int getMinScore() { return minScore; }
    public int getMaxScore() { return maxScore; }
}