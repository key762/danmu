package skiree.host.danmu.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import skiree.host.danmu.service.base.ConfigService;

@Controller
@CrossOrigin
public class ConfigApiController {

    @Autowired
    private ConfigService configService;

    @PostMapping("/doConfig")
    public String doConfig(String danMuRow, String danMuSize, String danMuBG, String danMuFormat, String danMuSpeed) {
        configService.updateConfig(danMuRow, danMuSize, danMuBG, danMuFormat, danMuSpeed);
        configService.updateConfig();
        return "redirect:/show.html";
    }

}
