package frontend.ctrl;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import frontend.data.Sms;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(path = "/sms")
public class FrontendController {

    private String modelHost;
    private RestTemplateBuilder rest;

    public FrontendController(RestTemplateBuilder rest, Environment env) {
        this.rest = rest;
        this.modelHost = env.getProperty("MODEL_HOST");
        assertModelHost();
    }

    private void assertModelHost() {
        if (modelHost == null || modelHost.strip().isEmpty()) {
            System.err.println("ERROR: ENV variable MODEL_HOST is null or empty");
            System.exit(1);
        }
        modelHost = modelHost.strip();
        if (modelHost.indexOf("://") == -1) {
            var m = "ERROR: ENV variable MODEL_HOST is missing protocol, like \"http://...\" (was: \"%s\")\n";
            System.err.printf(m, modelHost);
            System.exit(1);
        } else {
            System.out.printf("Working with MODEL_HOST=\"%s\"\n", modelHost);
        }
    }

    @GetMapping("")
    public String redirectToSlash(HttpServletRequest request) {
        // relative REST requests in JS will end up on / and not on /sms
        return "redirect:" + request.getRequestURI() + "/";
    }

    @GetMapping("/")
    public String index(Model m) {
        m.addAttribute("hostname", modelHost);
        return "sms/index";
    }

    @PostMapping({ "", "/" })
    @ResponseBody
    public Sms predict(@RequestBody Sms sms) {
        if (sms.bulk != null && !sms.bulk.isEmpty()) {
            long start = System.nanoTime(); // Start latency timer

            String[] results = getBulkPredictions(sms);
            sms.bulkResults = java.util.Arrays.asList(results);

            long end = System.nanoTime(); // End timer
            double durationSeconds = (end - start) / 1_000_000_000.0;
            double avgDuration = durationSeconds / sms.bulk.size();

            for (String res : results) {
                MetricsController.recordClassification(res.equalsIgnoreCase("spam"), 0.5, avgDuration);
                MetricsController.recordMode(true);
            }

            return sms;
        }
        
        long start = System.nanoTime(); // Start latency timer

        System.out.printf("Requesting prediction for \"%s\" ...\n", sms.sms);
        
        // Perform prediction via model-service
        sms.result = getPrediction(sms);
        System.out.printf("Prediction: %s\n", sms.result);
        
        long end = System.nanoTime();
        double durationSeconds = (end - start) / 1_000_000_000.0;
        
        boolean isSpam = sms.result.equalsIgnoreCase("spam");
        
        // Use real confidence from model (or default to 0.5 if not available)
        double confidence = (sms.confidence > 0) ? sms.confidence : 0.5;
        
        // Record metrics
        MetricsController.recordClassification(isSpam, confidence, durationSeconds);
        MetricsController.recordMode(false);

        return sms;
    }

    private String getPrediction(Sms sms) {
        try {
            var url = new URI(modelHost + "/predict");
            var c = rest.build().postForEntity(url, sms, Sms.class);
            return c.getBody().result.trim();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] getBulkPredictions(Sms sms) {
        try {
            var url = new URI(modelHost + "/bulk");
            var c = rest.build().postForEntity(url, sms, Sms[].class);
            Sms[] smsArray = c.getBody();
            
            String[] results = new String[smsArray.length];
            for (int i = 0; i < smsArray.length; i++) {
                results[i] = smsArray[i].result;
            }
            return results;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}