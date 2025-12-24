package frontend.ctrl;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@RestController
public class MetricsController {

    // Counter metric: how many SMS checks
    private static final AtomicLong spamCounter = new AtomicLong(0);
    private static final AtomicLong hamCounter  = new AtomicLong(0);

    // Gauge metric: last confidence score
    private static final AtomicReference<Double> lastSpamConfidence = new AtomicReference<>(0.0);
    private static final AtomicReference<Double> lastHamConfidence  = new AtomicReference<>(0.0);

    // Histogram buckets
    private static final AtomicLong bucket_01 = new AtomicLong(0);  // <= 0.1s
    private static final AtomicLong bucket_03 = new AtomicLong(0);  // <= 0.3s
    private static final AtomicLong bucket_10 = new AtomicLong(0);  // <= 1.0s
    private static final AtomicLong bucket_inf = new AtomicLong(0); // <= +Inf (all requests)
    
    private static final AtomicReference<Double> totalDuration = new AtomicReference<>(0.0);
    private static final AtomicLong durationCount = new AtomicLong(0);

    /**
     * Call this from your classification controller.
     */
    public static void recordClassification(boolean isSpam, double confidence, double durationSeconds) {
        // Update counters
        if (isSpam) {
            spamCounter.incrementAndGet();
            lastSpamConfidence.set(confidence);
        } else {
            hamCounter.incrementAndGet();
            lastHamConfidence.set(confidence);
        }

        // Each bucket includes all requests up to that threshold
        if (durationSeconds <= 0.1) {
            bucket_01.incrementAndGet();
        }
        if (durationSeconds <= 0.3) {
            bucket_03.incrementAndGet();
        }
        if (durationSeconds <= 1.0) {
            bucket_10.incrementAndGet();
        }
        // +Inf bucket includes all requests
        bucket_inf.incrementAndGet();

        // Histogram summary
        durationCount.incrementAndGet();
        totalDuration.updateAndGet(v -> v + durationSeconds);
    }

    @GetMapping(value = "/metrics", produces = "text/plain")
    public String metrics() {
        StringBuilder sb = new StringBuilder();

        // Counter metric with labels
        sb.append("# HELP sms_requests_total Total number of SMS classification requests\n");
        sb.append("# TYPE sms_requests_total counter\n");
        sb.append("sms_requests_total{result=\"spam\"} ").append(spamCounter.get()).append("\n");
        sb.append("sms_requests_total{result=\"ham\"} ").append(hamCounter.get()).append("\n\n");

        // Gauge metric with labels
        sb.append("# HELP sms_last_confidence Confidence score of last prediction\n");
        sb.append("# TYPE sms_last_confidence gauge\n");
        sb.append("sms_last_confidence{result=\"spam\"} ").append(lastSpamConfidence.get()).append("\n");
        sb.append("sms_last_confidence{result=\"ham\"} ").append(lastHamConfidence.get()).append("\n\n");

        // Histogram metric (cumulative buckets)
        sb.append("# HELP sms_request_duration_seconds Histogram of request durations\n");
        sb.append("# TYPE sms_request_duration_seconds histogram\n");
        sb.append("sms_request_duration_seconds_bucket{le=\"0.1\"} ").append(bucket_01.get()).append("\n");
        sb.append("sms_request_duration_seconds_bucket{le=\"0.3\"} ").append(bucket_03.get()).append("\n");
        sb.append("sms_request_duration_seconds_bucket{le=\"1.0\"} ").append(bucket_10.get()).append("\n");
        sb.append("sms_request_duration_seconds_bucket{le=\"+Inf\"} ").append(bucket_inf.get()).append("\n");
        sb.append("sms_request_duration_seconds_sum ").append(totalDuration.get()).append("\n");
        sb.append("sms_request_duration_seconds_count ").append(durationCount.get()).append("\n");

        return sb.toString();
    }
}