package skiree.host.danmu.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import skiree.host.danmu.model.ResultData;
import skiree.host.danmu.service.ResourceService;

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

    @PostMapping(value = "/resource/update")
    @ResponseBody
    public ResultData check(@RequestParam("id") String id, @RequestParam("name") String name, @RequestParam("path") String path) {
        return resourceService.updateData(id, name, path);
    }

    @PostMapping(value = "/resource/check")
    @ResponseBody
    public ResultData check(@RequestParam("name") String name, @RequestParam("path") String path) {
        return resourceService.checkData(name, path);
    }

    @GetMapping("/resource/list")
    @ResponseBody
    public ResultData list(@RequestParam("page") int page, @RequestParam("limit") int limit) {
        return resourceService.pageList(page, limit);
    }

    @GetMapping("/resource/select")
    @ResponseBody
    public ResultData select() {
        return resourceService.selectData();
    }

}