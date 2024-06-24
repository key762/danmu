package skiree.host.danmu.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import skiree.host.danmu.dao.ExecuteMapper;
import skiree.host.danmu.dao.LogMapper;
import skiree.host.danmu.dao.ResourceMapper;
import skiree.host.danmu.dao.RoutineMapper;
import skiree.host.danmu.model.*;
import skiree.host.danmu.model.ass.ASS;
import skiree.host.danmu.model.engine.DanMu;
import skiree.host.danmu.service.core.Stratege;
import skiree.host.danmu.service.core.vqq.vQqAssEngine;
import skiree.host.danmu.util.substitutor.CustomSubstitutor;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ExecuteService {

    @Autowired
    private ExecuteMapper executeMapper;

    @Autowired
    private RoutineMapper routineMapper;

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private LogService logService;

    @Autowired
    private PluginRegistry<Stratege, TaskDo> registry;

    @Autowired
    private LogMapper logMapper;

    public ResultData deleteData(String id) {
        QueryWrapper<Log> queryLogWrapper = new QueryWrapper<>();
        queryLogWrapper.eq("execute", id);
        logMapper.delete(queryLogWrapper);
        executeMapper.deleteById(id);
        return new ResultData(200, "OK");
    }

    public Execute executeDoPre(String id) {
        Execute execute = new Execute(uniqueId(), id, "已就绪");
        execute.setStart(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        executeMapper.insert(execute);
        return execute;
    }

    public ResultData pageList(int page, int limit) {
        QueryWrapper<Execute> queryExecuteWrapper = new QueryWrapper<>();
        queryExecuteWrapper.orderByDesc("start");
        Page<Execute> pageData = executeMapper.selectPage(new Page<>(page, limit), queryExecuteWrapper);
        List<Execute> records = pageData.getRecords();
        QueryWrapper<Routine> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", records.stream().map(Execute::getRoutine).toArray());
        List<Routine> routines = routineMapper.selectList(queryWrapper);
        Map<String, Routine> routineMap = routines.stream()
                .collect(Collectors.toMap(Routine::getId, routine -> routine));
        records.forEach(execute -> {
            Routine routine = routineMap.get(execute.getRoutine());
            if (routine == null) {
                execute.setRoutineName("资源不存在");
            } else {
                execute.setRoutineName(routine.getName());
            }
        });
        return new ResultData(200, "OK", pageData);
    }

    @Async
    public void executeDo(Execute execute) {
        TaskDo taskDo = buildTaskDo(execute);
        logService.recordLog(taskDo, "初始化完成");
        try {
            execute.setStatus("处理中");
            execute.setEnd(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
            executeMapper.updateById(execute);
            // 准备执行了
            doBullet(taskDo);
            // 执行结束了
            logService.recordLog(taskDo, "执行结束");
            execute.setStatus("已成功");
        } catch (Exception e) {
            logService.recordLog(taskDo, "执行失败");
            execute.setStatus("已失败");
            execute.setEnd(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
            executeMapper.updateById(execute);
        } finally {
            execute.setEnd(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
            executeMapper.updateById(execute);
        }
    }

    private void doBullet(TaskDo taskDo) {
        // 先删除旧的ass文件
        delOldAss(taskDo);
        // 统计本地的视频文件
        Map<Integer, String> idName = localFileMap(taskDo);
        if (idName.isEmpty()) {
            logService.recordLog(taskDo, "本地视频文件统计数量为[0],执行结束");
            return;
        }
        logService.recordLog(taskDo, "本地视频文件统计数量为[" + idName.size() + "]");
        // 统计弹幕平台的视频信息
        Stratege stratege = registry.getRequiredPluginFor(taskDo);
        Map<Integer, Object> idMap = stratege.idMap(taskDo);
        if (idMap.isEmpty()) {
            logService.recordLog(taskDo, "弹幕平台视频数量为[0],执行结束");
            return;
        }
        logService.recordLog(taskDo, "弹幕平台视频数量为[" + idMap.size() + "]");
        // 计算本地视频和弹幕平台视频的交集
        Map<Integer, Object> jobMap = idMap.entrySet().stream()
                .filter(entry -> idName.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (jobMap.isEmpty()) {
            logService.recordLog(taskDo, "符合弹幕抓取的视频数量为[0],执行结束");
            return;
        }
        logService.recordLog(taskDo, "符合弹幕抓取的视频数量为[" + jobMap.size() + "]");
        jobMap.forEach((k, v) -> {
            Map<Long, List<DanMu>> res = stratege.getDanMu(v);
            Collection<String> danMuColl = vQqAssEngine.calcDanMu(res);
            List<String> temp = new ArrayList<>(ASS.PUBLIC_ASS);
            temp.addAll(danMuColl);
            String assPath = assFilePath(taskDo, idName, k);
            String filePath = taskDo.routine.fullPath + "/" + assPath + ".ass";
            if (FileUtil.exist(filePath)) {
                FileUtil.del(filePath);
            }
            FileUtil.writeLines(temp, filePath, "UTF-8");
            logService.recordLog(taskDo, "弹幕处理完成,已转载弹幕[" + danMuColl.size() + "],视频弹幕名称[" + assPath + "]");
        });
    }

    private String assFilePath(TaskDo taskDo, Map<Integer, String> idName, Integer key) {
        HashMap<String, String> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("time", DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        String newName = CustomSubstitutor.getInstance().replace(taskDo.routine.rename, objectObjectHashMap);
        newName = idName.get(key) + "." + newName;
        if (FileUtil.containsInvalid(newName)) {
            newName = FileUtil.cleanInvalid(newName);
        }
        return newName;
    }

    private Map<Integer, String> localFileMap(TaskDo taskDo) {
        Map<Integer, String> idName = new HashMap<>();
        try {
            Pattern pattern = Pattern.compile("E(\\d+)");
            for (File file : FileUtil.ls(taskDo.routine.fullPath)) {
                String extName = FileUtil.extName(file);
                if (StrUtil.containsAnyIgnoreCase(extName, "mkv", "mp4")) {
                    String mainName = FileUtil.mainName(file);
                    Matcher matcher = pattern.matcher(mainName);
                    if (matcher.find()) {
                        Integer episodeNumber = Integer.parseInt(matcher.group(1));
                        idName.put(episodeNumber, mainName);
                    }
                }
            }
            return idName;
        } catch (Exception e) {
            logService.recordLog(taskDo, "统计本地视频文件时异常: " + e.getMessage());
            throw e;
        }
    }

    private void delOldAss(TaskDo taskDo) {
        try {
            String delMark = taskDo.routine.delmark;
            String fullPath = taskDo.routine.fullPath;
            if (delMark != null && !delMark.isEmpty()) {
                int num = 0;
                for (File file : FileUtil.ls(fullPath)) {
                    if (FileUtil.extName(file).equals("ass")) {
                        if (FileUtil.mainName(file).contains(delMark)) {
                            FileUtil.del(file);
                            num += 1;
                        }
                    }
                }
                logService.recordLog(taskDo, "已配置DelMark[" + delMark + "],已删除旧的Ass文件个数[" + num + "]");
            } else {
                logService.recordLog(taskDo, "未配置DelMark,将不会删除旧的Ass文件");
            }
        } catch (Exception e) {
            logService.recordLog(taskDo, "删除旧的ass文件时异常: " + e.getMessage());
            throw e;
        }
    }

    private TaskDo buildTaskDo(Execute execute) {
        TaskDo taskDo = new TaskDo();
        taskDo.setExecute(execute);
        // 补充Routine
        Routine routine = routineMapper.selectById(execute.getRoutine());
        Resource resource = resourceMapper.selectById(routine.getResource());
        routine.setFullPath(resource.getPath() + routine.getPath());
        routine.setResourcePath(resource.getPath());
        routine.setResourceName(resource.getName());
        routine.setSources(JSONUtil.parseArray(routine.getSource()).toList(String.class));
        // 设置Routine
        taskDo.setRoutine(routine);
        return taskDo;
    }

    private String uniqueId() {
        String uuid = Execute.randomId();
        while (executeMapper.selectById(uuid) != null) {
            uuid = Resource.randomId();
        }
        return uuid;
    }

    public ResultData recordData(String id) {
        QueryWrapper<Log> queryLogWrapper = new QueryWrapper<>();
        queryLogWrapper.eq("execute", id).orderByAsc("order_num");
        return new ResultData(200, "OK", logMapper.selectList(queryLogWrapper));
    }

    public void buildShow(Model model) {
        model.addAttribute("resourceNum", resourceMapper.selectCount(null));
        model.addAttribute("routineNum", routineMapper.selectCount(null));
        model.addAttribute("executeNum", executeMapper.selectCount(null));
        model.addAttribute("okNum", executeMapper.selectCount(new QueryWrapper<Execute>().eq("status","已成功")));
    }
}