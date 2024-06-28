package skiree.host.danmu.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import skiree.host.danmu.model.base.Execute;
import skiree.host.danmu.model.base.ResultData;
import skiree.host.danmu.model.base.Routine;
import skiree.host.danmu.service.base.ExecuteService;
import skiree.host.danmu.service.base.RoutineService;

@Controller
@CrossOrigin
public class RoutineApiController {

    @Autowired
    private RoutineService routineService;

    @Autowired
    private ExecuteService executeService;

    @PostMapping(value = "/routine/add")
    @ResponseBody
    public ResultData save(@RequestBody Routine routine) {
        return routineService.saveData(routine);
    }

    @GetMapping("/routine/delete/{id}")
    @ResponseBody
    public ResultData delete(@PathVariable("id") String id) {
        return routineService.deleteData(id);
    }

    @GetMapping("/routine/execute/{id}")
    @ResponseBody
    public ResultData execute(@PathVariable("id") String id) {
        Execute execute = executeService.executeDoPre(id);
        executeService.executeDo(execute);
        return new ResultData(200, "执行编号: " + execute.getId() + " ");
    }

    @PostMapping(value = "/routine/update")
    @ResponseBody
    public ResultData update(@RequestBody Routine routine) {
        return routineService.updateData(routine);
    }

    @GetMapping("/routine/list")
    @ResponseBody
    public ResultData list(@RequestParam("page") int page, @RequestParam("limit") int limit) {
        return routineService.pageList(page, limit);
    }

}