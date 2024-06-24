package skiree.host.danmu.controller.view;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@CrossOrigin
public class ExecuteViewController {

    @GetMapping("/execute.html")
    public String execute() {
        return checkLogin("execute");
    }

    @GetMapping("/execute/add.html")
    public String executeAdd() {
        return checkLogin("/execute/add");
    }

    @GetMapping("/execute/update.html")
    public String executeUpdate() {
        return checkLogin("/execute/update");
    }

    private String checkLogin(String target) {
        if (StpUtil.isLogin()) {
            return target;
        }
        return "redirect:/login";
    }

}
