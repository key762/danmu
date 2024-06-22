package skiree.host.danmu.controller.view;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@CrossOrigin
public class RoutineViewController {

    @GetMapping("/routine.html")
    public String routine() {
        return checkLogin("routine");
    }

    @GetMapping("/routine/add.html")
    public String routineAdd() {
        return checkLogin("/routine/add");
    }

    @GetMapping("/routine/update.html")
    public String routineUpdate() {
        return checkLogin("/routine/update");
    }

    private String checkLogin(String target) {
        if (StpUtil.isLogin()) {
            return target;
        }
        return "redirect:/login";
    }

}
