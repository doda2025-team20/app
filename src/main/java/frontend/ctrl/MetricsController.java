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

    // Histogram buckets (0.1s, 0.3s, 1s, +Inf)
    private static final long[] durationBuckets = new long[4];
    private static final AtomicReference<Double> totalDuration = new AtomicReference<>(0.0);
    private static final AtomicLong durationCount = new AtomicLong(0);

    /**
     * Call this from your classification controller.
     */
    public static void recordClassification(boolean isSpam, double confidence, double durationSeconds) {

        if (isSpam) {
            spamCounter.incrementAndGet();
            lastSpamConfidence.set(confidence);
        } else {
            hamCounter.incrementAndGet();
            lastHamConfidence.set(confidence);
        }

        // Histogram bucket logic
        if (durationSeconds <= 0.1) durationBuckets[0]++;
        else if (durationSeconds <= 0.3) durationBuckets[1]++;
        else if (durationSeconds <= 1.0) durationBuckets[2]++;
        else durationBuckets[3]++;

        // Histogram summary
        durationCount.incrementAndGet();
        totalDuration.set(totalDuration.get() + durationSeconds);
    }

    @GetMapping(value = "/metrics", produces = "text/plain")
    public String metrics() {
        StringBuilder sb = new StringBuilder();

        // Counter metric
        sb.append("# HELP sms_requests_total Total number of SMS classification requests\n");
        sb.append("# TYPE sms_requests_total counter\n");
        sb.append("sms_requests_total{result=\"spam\"} ").append(spamCounter.get()).append("\n");
        sb.append("sms_requests_total{result=\"ham\"} ").append(hamCounter.get()).append("\n\n");

        // Gauge metric
        sb.append("# HELP sms_last_confidence Confidence score of last prediction\n");
        sb.append("# TYPE sms_last_confidence gauge\n");
        sb.append("sms_last_confidence{result=\"spam\"} ").append(lastSpamConfidence.get()).append("\n");
        sb.append("sms_last_confidence{result=\"ham\"} ").append(lastHamConfidence.get()).append("\n\n");

        // Histogram metric
        sb.append("# HELP sms_request_duration_seconds Histogram of request durations\n");
        sb.append("# TYPE sms_request_duration_seconds histogram\n");

        double totalDur = totalDuration.get();
        long count = durationCount.get();

        sb.append("sms_request_duration_seconds_bucket{le=\"0.1\"} ").append(durationBuckets[0]).append("\n");
        sb.append("sms_request_duration_seconds_bucket{le=\"0.3\"} ").append(durationBuckets[1]).append("\n");
        sb.append("sms_request_duration_seconds_bucket{le=\"1.0\"} ").append(durationBuckets[2]).append("\n");
        sb.append("sms_request_duration_seconds_bucket{le=\"+Inf\"} ").append(durationBuckets[3]).append("\n");
        sb.append("sms_request_duration_seconds_sum ").append(totalDur).append("\n");
        sb.append("sms_request_duration_seconds_count ").append(count).append("\n");

        return sb.toString();
    }
}