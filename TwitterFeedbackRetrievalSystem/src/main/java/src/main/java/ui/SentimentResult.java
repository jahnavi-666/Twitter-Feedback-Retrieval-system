package src.main.java.ui;

public class SentimentResult {
    private String sentiment;
    private double confidence;

    public SentimentResult(String sentiment, double confidence) {
        this.sentiment = sentiment;
        this.confidence = confidence;
    }

    public String getSentiment() {
        return sentiment;
    }

    public double getConfidence() {
        return confidence;
    }
}
