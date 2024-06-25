package skiree.host.danmu.controller.view;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import skiree.host.danmu.service.ExecuteService;

@Controller
@CrossOrigin
public class BasicViewController {

    @Autowired
    private ExecuteService executeService;

    @Value("${danmu.user}")
    private String user;

    @Value("${danmu.password}")
    private String fortPassword;

    @GetMapping({"/", "/home"})
    public String home() {
        return checkLogin("home");
    }

    @GetMapping({"/login"})
    public String login() {
        if (StpUtil.isLogin()) {
            return "redirect:/home";
        }
        return "login";
    }

    @GetMapping("/doLogout")
    public String doLogout() {
        StpUtil.logout("Admin");
        return "redirect:/login";
    }

    @PostMapping("/doLogin")
    public String doLogin(Model model, String username, String password) {
        if (user.equals(username) && fortPassword.equals(password)) {
            model.addAttribute("loginName", "Admin");
            StpUtil.login("Admin");
            return "redirect:/home";
        }
        return "redirect:/login";
    }

    @GetMapping("/show.html")
    public String show(Model model) {
        executeService.buildShow(model);
        return checkLogin("show");
    }

    private String checkLogin(String target) {
        if (StpUtil.isLogin()) {
            return target;
        }
        return "redirect:/login";
    }

}