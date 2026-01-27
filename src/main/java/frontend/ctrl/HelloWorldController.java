package frontend.ctrl;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

import sms.VersionUtil;

@Controller
public class HelloWorldController {

    @GetMapping("/")
    public RedirectView index() {
        return new RedirectView("/sms");
    }

    @GetMapping("/about")
    @ResponseBody
    public String about() {
        return "<html><body><h1>About SMS Checker</h1><p>Using LibVersion v" + VersionUtil.getVersion() + ".</p><a href=\"/sms\">&larr; Back to SMS Checker</a></body></html>";
    }
}