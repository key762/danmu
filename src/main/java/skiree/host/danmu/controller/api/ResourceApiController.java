package skiree.host.danmu.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import skiree.host.danmu.model.base.ResultData;
import skiree.host.danmu.service.base.ResourceService;

@Controller
@CrossOrigin
public class ResourceApiController {

    @Autowired
    private ResourceService resourceService;

    @PostMapping(value = "/resource/add")
    @ResponseBody
    public ResultData save(@RequestParam("name") String name, @RequestParam("path") String path) {
        return resourceService.saveData(name, path);
    }

    @GetMapping("/resource/delete/{id}")
    @ResponseBody
    public ResultData delete(@PathVariable("id") String id) {
        return resourceService.deleteData(id);
    }

    @GetMapping("/resource/analysis/{id}")
    @ResponseBody
    public ResultData analysis(@PathVariable("id") String id) {
        resourceService.analysisData(id);
        return new ResultData(200, "OK");
    }

    @PostMapping(value = "/resource/update")
    @ResponseBody
    public ResultData check(@RequestParam("id") String id, @RequestParam("name") String name) {
        return resourceService.updateData(id, name);
    }

    @GetMapping("/resource/list")
    @ResponseBody
    public ResultData list(@RequestParam("page") int page, @RequestParam("limit") int limit) {
        return resourceService.pageList(page, limit);
    }

}