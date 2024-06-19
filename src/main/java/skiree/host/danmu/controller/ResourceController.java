package skiree.host.danmu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import skiree.host.danmu.model.Resource;
import skiree.host.danmu.service.ResourceService;

import java.util.List;

@Controller
@CrossOrigin
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    @GetMapping("/resource2.html")
    public String resource2(Model model) {
        model.addAttribute("loginName", "Admin");
        model.addAttribute("resourceList", resourceService.selectAll());
        return "resource2";
    }

    @GetMapping("/resource.html")
    public String resource(Model model) {
        model.addAttribute("loginName", "Admin");
        model.addAttribute("resourceList", resourceService.selectAll());
        return "resource";
    }

    @GetMapping("/resource/list")
    @ResponseBody
    public List<Resource> list() {
        return resourceService.selectAll();
    }

    @GetMapping("/resource/delete/{resourceId}")
    public String delete(Model model, @PathVariable("resourceId") String resourceId) {
        resourceService.deleteById(resourceId);
        model.addAttribute("loginName", "Admin");
        model.addAttribute("resourceList", resourceService.selectAll());
        return "resource";
    }

    @PostMapping(value = "/resource/add")
    public Object save(Model model, @RequestParam("name") String name, @RequestParam("path") String path) {
        resourceService.saveData(name, path);
        model.addAttribute("loginName", "Admin");
        model.addAttribute("resourceList", resourceService.selectAll());
        return "resource";
    }

}
