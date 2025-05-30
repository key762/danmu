package skiree.host.danmu.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import skiree.host.danmu.model.base.ResultData;
import skiree.host.danmu.service.base.ExecuteService;

@Controller
@CrossOrigin
public class ExecuteApiController {

    @Autowired
    private ExecuteService executeService;

    @GetMapping("/execute/delete/{id}")
    @ResponseBody
    public ResultData delete(@PathVariable("id") String id) {
        return executeService.deleteData(id);
    }

    @GetMapping("/execute/record/{id}")
    @ResponseBody
    public ResultData record(@PathVariable("id") String id) {
        return executeService.recordData(id);
    }

    @GetMapping("/execute/list")
    @ResponseBody
    public ResultData list(@RequestParam("page") int page, @RequestParam("limit") int limit) {
        return executeService.pageList(page, limit);
    }

}