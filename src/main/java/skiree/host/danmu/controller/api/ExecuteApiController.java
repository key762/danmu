package skiree.host.danmu.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import skiree.host.danmu.model.ResultData;
import skiree.host.danmu.service.ExecuteService;

@Controller
@CrossOrigin
public class ExecuteApiController {

    @Autowired
    private ExecuteService executeService;

    @GetMapping("/execute/delete/{id}")
    @ResponseBody
    public ResultData execute(@PathVariable("id") String id) {
        return executeService.deleteData(id);
    }

    @GetMapping("/execute/list")
    @ResponseBody
    public ResultData list(@RequestParam("page") int page, @RequestParam("limit") int limit) {
        return executeService.pageList(page, limit);
    }

}