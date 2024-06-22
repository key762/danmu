package skiree.host.danmu.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import skiree.host.danmu.model.ResultData;
import skiree.host.danmu.model.Routine;
import skiree.host.danmu.service.RoutineService;

@Controller
@CrossOrigin
public class RoutineApiController {

    @Autowired
    private RoutineService routineService;

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

    @PostMapping(value = "/routine/update")
    @ResponseBody
    public ResultData update(@RequestBody Routine routine) {
        return routineService.updateData(routine);
    }

    @PostMapping(value = "/routine/check")
    @ResponseBody
    public ResultData check(@RequestBody Routine routine) {
        return routineService.checkData(routine);
    }

    @GetMapping("/routine/list")
    @ResponseBody
    public ResultData list(@RequestParam("page") int page, @RequestParam("limit") int limit) {
        return routineService.pageList(page, limit);
    }

}