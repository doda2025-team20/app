package frontend.ctrl;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import sms.VersionUtil;

@Controller
public class HelloWorldController {

    @GetMapping("/")
    @ResponseBody
    public String index() {
        return "Hello World!";
    }

    @GetMapping("/about")
    @ResponseBody
    public String about() {
        return "Version: " + VersionUtil.getVersion();
    }
}