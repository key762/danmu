package skiree.host.danmu.service.base;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import skiree.host.danmu.dao.ExecuteMapper;
import skiree.host.danmu.dao.LogMapper;
import skiree.host.danmu.dao.ResourceMapper;
import skiree.host.danmu.dao.RoutineMapper;
import skiree.host.danmu.model.ass.ASS;
import skiree.host.danmu.model.ass.VideoInfo;
import skiree.host.danmu.model.base.*;
import skiree.host.danmu.model.engine.DanMu;
import skiree.host.danmu.model.task.Task;
import skiree.host.danmu.model.tmdb.SeasonPath;
import skiree.host.danmu.model.tmdb.TvPath;
import skiree.host.danmu.service.core.Stratege;
import skiree.host.danmu.service.tmdb.AutomaticService;
import skiree.host.danmu.service.tmdb.DouBanService;
import skiree.host.danmu.service.tmdb.TMDBService;
import skiree.host.danmu.util.match.PlatUtil;
import skiree.host.danmu.util.substitutor.CustomSubstitutor;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ExecuteService extends BaseService {

    @Autowired
    public ResourceMapper resourceMapper;

    @Autowired
    public RoutineMapper routineMapper;

    @Autowired
    public ExecuteMapper executeMapper;

    @Autowired
    private TMDBService tmdbService;

    @Autowired
    public LogMapper logMapper;

    @Autowired
    public LogService logService;

    @Autowired
    private PluginRegistry<Stratege, String> registry;
    @Autowired
    private Task task;

    public ResultData deleteData(String id) {
        QueryWrapper<Log> queryLogWrapper = new QueryWrapper<>();
        queryLogWrapper.eq("execute", id);
        logMapper.delete(queryLogWrapper);
        executeMapper.deleteById(id);
        return new ResultData(200, "OK");
    }

    public Execute executeDoPre(String id) {
        Execute execute = new Execute(uniqueId(), id, "已就绪");
        execute.setStart(nowTime());
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
        TaskDo taskDo = new TaskDo();
        taskDo.setExecute(execute);
        try {
            taskDo = buildTaskDo(execute);
            logService.recordLog(taskDo, "初始化完成");
            execute.setStatus("排队中");
            execute.setEnd(nowTime());
            executeMapper.updateById(execute);
            // 准备执行了
            boolean mark = doBullet(taskDo);
            // 执行结束了
            if (mark) {
                execute.setStatus("已成功");
                logService.recordLog(taskDo, "执行结束");
            } else {
                execute.setStatus("已失败");
                logService.recordLog(taskDo, "执行失败");
            }
        } catch (Exception e) {
            logService.recordLog(taskDo, "执行失败,Exception : " + e.getMessage());
            execute.setStatus("已失败");
            execute.setEnd(nowTime());
            executeMapper.updateById(execute);
        } finally {
            execute.setEnd(nowTime());
            executeMapper.updateById(execute);
        }
    }

    private synchronized boolean doBullet(TaskDo taskDo) {
        AtomicBoolean mark = new AtomicBoolean(true);
        // 状态更新
        taskDo.execute.setStatus("执行中");
        taskDo.execute.setEnd(nowTime());
        executeMapper.updateById(taskDo.execute);
        // 先删除旧的ass文件
        delOldAss(taskDo);
        // 统计本地的视频文件
        Map<Integer, String> idName = tmdbFileMap(taskDo);
        if (idName.isEmpty()) {
            mark.set(false);
            logService.recordLog(taskDo, "TMDB集数统计数量为[0],执行结束");
            return mark.get();
        }
        logService.recordLog(taskDo, "TMDB集数统计数量为[" + idName.size() + "]");
        // 统计弹幕平台的视频信息
        Map<Integer, String> idMap = doubanFileMap(taskDo);
        if (idMap.isEmpty()) {
            mark.set(false);
            logService.recordLog(taskDo, "豆瓣集数统计为[0],执行结束");
            return mark.get();
        }
        logService.recordLog(taskDo, "豆瓣集数统计为[" + idMap.size() + "]");
        // 计算本地视频和弹幕平台视频的交集
        Map<Integer, String> jobMap = idMap.entrySet().stream()
                .filter(entry -> idName.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (jobMap.isEmpty()) {
            mark.set(false);
            logService.recordLog(taskDo, "符合匹配集数为[0],执行结束");
            return mark.get();
        }
        logService.recordLog(taskDo, "符合匹配集数为[" + jobMap.size() + "]");
        // 要考虑本地文件
        Map<Integer, VideoInfo> localFile = localFileMap(taskDo);
        // 计算本地视频和弹幕平台视频的交集
        Map<Integer, String> coreMap = jobMap.entrySet().stream()
                .filter(entry -> localFile.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (coreMap.isEmpty()) {
            mark.set(false);
            logService.recordLog(taskDo, "本地符合匹配集数为[0],执行结束");
            return mark.get();
        }
        logService.recordLog(taskDo, "本地符合匹配集数为[" + coreMap.size() + "]");
        coreMap.forEach((k, v) -> {
            Stratege stratege = null;
            try {
                stratege = registry.getRequiredPluginFor(v);
            } catch (Exception ignore) {
            }
            if (stratege == null) {
                mark.set(false);
                logService.recordLog(taskDo, "暂时未兼容此平台[" + PlatUtil.basePlat(v) + "]");
                return;
            }
            // 先更新重名标志
            taskDo.routine.rename = PlatUtil.ckPlat(v, "弹幕");
            routineMapper.updateById(taskDo.routine);
            // 获取数据
            Map<Long, List<DanMu>> res = stratege.getDanMu(v);
            if (!res.isEmpty()) {
                VideoInfo videoInfo = localFile.get(k);
                Collection<String> danMuColl = calcDanMu(res, videoInfo);
                List<String> temp = new ArrayList<>(ASS.PUBLIC_ASS1);
                temp.add("PlayResX: "+videoInfo.getWidth());
                temp.add("PlayResY: "+videoInfo.getHeight());
                temp.addAll(ASS.PUBLIC_ASS2);
                temp.addAll(danMuColl);
                String assPath = assFilePath(taskDo, idName, k);
                String filePath = taskDo.routine.path + "/" + assPath + ".ass";
                if (FileUtil.exist(filePath)) {
                    FileUtil.del(filePath);
                }
                FileUtil.writeLines(temp, filePath, "UTF-8");
                String msg = "加载弹幕[" + danMuColl.size() + "]条,文件[" + assPath + "]".replaceAll(" ", "");
                logService.recordLog(taskDo, msg);
            } else {
                String msg = "加载弹幕失败,视频[" + idName.get(k) + "]-(" + v + ")".replaceAll(" ", "");
                logService.recordLog(taskDo, msg);
            }
        });
        return mark.get();
    }

    private Map<Integer, VideoInfo> localFileMap(TaskDo taskDo) {
        Map<Integer, VideoInfo> idName = new HashMap<>();
        try {
            Pattern pattern = Pattern.compile("E(\\d+)");
            for (File file : FileUtil.ls(taskDo.routine.path)) {
                String extName = FileUtil.extName(file);
                if (StrUtil.containsAnyIgnoreCase(extName, "mkv", "mp4")) {
                    String mainName = FileUtil.mainName(file);
                    Matcher matcher = pattern.matcher(mainName);
                    if (matcher.find()) {
                        Integer episodeNumber = Integer.parseInt(matcher.group(1));
                        VideoInfo videoInfo = getVideoInfo(file);
                        if ( videoInfo != null ) {
                            idName.put(episodeNumber, videoInfo);
                        }
                    }
                }
            }
            return idName;
        } catch (Exception e) {
            logService.recordLog(taskDo, "统计本地视频文件时异常: " + e.getMessage());
            throw e;
        }
    }

    private Map<Integer, String> doubanFileMap(TaskDo taskDo) {
        Map<Integer, String> idMap = new HashMap<>();
        String url = "https://movie.douban.com/subject/" + taskDo.routine.getDoubanId() + "/";
        String result = HttpUtil.get(url, CharsetUtil.CHARSET_UTF_8);
        Matcher matcher = Pattern.compile("\\{play_link(.*?)}").matcher(result);
        while (matcher.find()) {
            JSONObject link = JSONUtil.parseObj(matcher.group());
            Integer ep = Integer.parseInt(link.getStr("ep"));
            String playLink = link.getStr("play_link");
            playLink = URLDecoder.decode(playLink, CharsetUtil.CHARSET_UTF_8);
            playLink = playLink.replace("https://www.douban.com/link2/?url=", "");
            playLink = playLink.split(".html")[0] + ".html";
            playLink = playLink.replace("http:", "https:");
            idMap.put(ep, playLink);
        }
        return idMap;
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

    private Map<Integer, String> tmdbFileMap(TaskDo taskDo) {
        return tmdbService.buildSeason(taskDo.seasonPath, taskDo.routine.getTmdbId());
    }

    private void delOldAss(TaskDo taskDo) {
        try {
            String delMark = taskDo.routine.delmark;
            String fullPath = taskDo.routine.path;
            if ((delMark != null && !delMark.isEmpty()) || taskDo.resource.getName().endsWith("Daa")) {
                int num = 0;
                for (File file : FileUtil.ls(fullPath)) {
                    if (FileUtil.extName(file).equals("ass")) {
                        if (FileUtil.mainName(file).contains(delMark)) {
                            FileUtil.del(file);
                            num += 1;
                        } else if (taskDo.resource != null && taskDo.resource.getName().endsWith("Daa")) {
                            FileUtil.del(file);
                            num += 1;
                        }
                    }
                }
                if (!taskDo.resource.getName().endsWith("Daa") && delMark != null && !delMark.isEmpty()) {
                    logService.recordLog(taskDo, "已配置删标[" + delMark + "],已删带此标的ass字幕个数[" + num + "]");
                } else {
                    logService.recordLog(taskDo, "未配置删标但资源后缀为Daa,将删除所有的ass字幕个数[" + num + "]");
                }
            } else {
                logService.recordLog(taskDo, "未配置删标,将不会删除任何ass字幕");
            }
        } catch (Exception e) {
            logService.recordLog(taskDo, "删除旧的ass字幕时异常: " + e.getMessage());
            throw e;
        }
    }

    @Autowired
    private AutomaticService automaticService;

    @Autowired
    private DouBanService douBanService;

    private TaskDo buildTaskDo(Execute execute) {
        TaskDo taskDo = new TaskDo();
        taskDo.setExecute(execute);
        // 补充TvPath
        Routine routine = routineMapper.selectById(execute.getRoutine());
        Resource resource = resourceMapper.selectById(routine.getResource());
        // 剧集
        TvPath tvPath = new TvPath();
        tvPath.setName(automaticService.tvHandleName2(routine.getName()));
        tvPath.setProtoName(routine.getName());
        // tmId处理
        if (routine.getTmdbId() == null) {
            tmdbService.buildTvId(tvPath);
            if (tvPath.getTmdbId() != null) {
                routine.setTmdbId(tvPath.getTmdbId());
                routineMapper.updateById(routine);
            }
        } else {
            tvPath.setTmdbId(routine.getTmdbId());
        }
        // 季
        SeasonPath seasonPath = new SeasonPath();
        seasonPath.setName(routine.getSeason());
        seasonPath.setProtoName(routine.getSeason());
        seasonPath.setPath(routine.getPath());
        // douBanId处理
        if (routine.getDoubanId() == null) {
            douBanService.douBanLink(seasonPath, tvPath.getName(), routine.getSeason());
            if (tvPath.getTmdbId() != null) {
                routine.setDoubanId(seasonPath.getDoubanId());
                routineMapper.updateById(routine);
            }
        } else {
            seasonPath.setDoubanId(routine.getDoubanId());
        }
        // 设置
        taskDo.setResource(resource);
        taskDo.setSeasonPath(seasonPath);
        taskDo.setTvPath(tvPath);
        // 补充Routine
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
        queryLogWrapper.eq("execute", id).orderByDesc("order_num");
        return new ResultData(200, "OK", logMapper.selectList(queryLogWrapper));
    }

    public void buildShow(Model model) {
        model.addAttribute("resourceNum", resourceMapper.selectCount(null));
        model.addAttribute("routineNum", routineMapper.selectCount(null));
        model.addAttribute("executeNum", executeMapper.selectCount(null));
        model.addAttribute("okNum", executeMapper.selectCount(new QueryWrapper<Execute>().eq("status", "已成功")));
    }

    public static VideoInfo getVideoInfo(File file) {
        VideoInfo videoInfo = new VideoInfo();
        FFmpegFrameGrabber grabber = null;
        try {
            grabber = new FFmpegFrameGrabber(file);
            grabber.start();
            videoInfo.setWidth(grabber.getImageWidth());
            videoInfo.setHeight(grabber.getImageHeight());
            return videoInfo;
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (grabber != null) {
                    grabber.stop();
                    grabber.release();
                }
            } catch (FFmpegFrameGrabber.Exception ignore) {}
        }
    }

}