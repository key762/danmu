package skiree.host.danmu.controller.view;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@CrossOrigin
public class ResourceViewController {

    @GetMapping("/resource.html")
    public String resource() {
        return checkLogin("resource");
    }

    @GetMapping("/resource/add.html")
    public String resourceAdd() {
        return checkLogin("/resource/add");
    }

    @GetMapping("/resource/update.html")
    public String resourceUpdate() {
        return checkLogin("/resource/update");
    }

    private String checkLogin(String target) {
        if (StpUtil.isLogin()) {
            return target;
        }
        return "redirect:/login";
    }

}
