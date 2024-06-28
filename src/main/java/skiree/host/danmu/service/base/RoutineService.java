package skiree.host.danmu.service.base;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import skiree.host.danmu.dao.ExecuteMapper;
import skiree.host.danmu.dao.LogMapper;
import skiree.host.danmu.dao.ResourceMapper;
import skiree.host.danmu.dao.RoutineMapper;
import skiree.host.danmu.model.base.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoutineService extends BaseService {

    @Autowired
    public ResourceMapper resourceMapper;

    @Autowired
    public RoutineMapper routineMapper;

    @Autowired
    public ExecuteMapper executeMapper;

    @Autowired
    public LogMapper logMapper;

    public ResultData deleteData(String id) {
        routineMapper.deleteById(id);
        QueryWrapper<Execute> queryExecuteWrapper = new QueryWrapper<>();
        queryExecuteWrapper.eq("routine", id);
        List<Execute> executes = executeMapper.selectList(queryExecuteWrapper);
        for (Execute execute : executes) {
            QueryWrapper<Log> queryLogWrapper = new QueryWrapper<>();
            queryLogWrapper.eq("execute", execute.getId());
            logMapper.delete(queryLogWrapper);
        }
        executeMapper.delete(queryExecuteWrapper);
        return new ResultData(200, "OK");
    }

    public ResultData saveData(Routine routine) {
        // 先清除前后空字符
        routine.setName(routine.getName().trim());
        routine.setSeason(routine.getSeason().trim());
        routine.setTmdbId(routine.getTmdbId().trim());
        routine.setDoubanId(routine.getDoubanId().trim());
        routine.setRename(routine.getRename().trim());
        routine.setDelmark(routine.getDelmark());
        routine.setPath(routine.getPath().trim());
        // 先校验路径是否有效
        if (!FileUtil.exist(routine.getPath())) {
            return new ResultData(406, "服务端不存在此路径");
        }
        // 检查名称是否已经重复
        QueryWrapper<Routine> wrapper = new QueryWrapper<>();
        wrapper.eq("name", routine.getName());
        if (routineMapper.selectCount(wrapper) > 0) {
            return new ResultData(406, "此名称已存在!");
        }
        // 检查路径是否已经重复
        wrapper.clear();
        wrapper.eq("path", routine.getPath());
        if (routineMapper.selectCount(wrapper) > 0) {
            return new ResultData(406, "此路径已存在!");
        }
        // 检查季,tmdb和douban
        try {
            Long.parseLong(routine.getSeason());
        } catch (Exception e) {
            return new ResultData(406, "此季标识不符合规范!");
        }
        try {
            Long.parseLong(routine.getTmdbId());
        } catch (Exception e) {
            return new ResultData(406, "此TMDB标识ID不符合规范!");
        }
        try {
            Long.parseLong(routine.getDoubanId());
        } catch (Exception e) {
            return new ResultData(406, "此豆瓣标识ID不符合规范!");
        }
        // 新增资源
        routine.setId(uniqueId());
        routine.setResource("");
        routine.setStart(nowTime());
        routineMapper.insert(routine);
        return new ResultData(200, "OK");
    }

    public ResultData pageList(int page, int limit) {
        QueryWrapper<Routine> queryRoutineWrapper = new QueryWrapper<>();
        queryRoutineWrapper.orderByDesc("start");
        Page<Routine> pageData = routineMapper.selectPage(new Page<>(page, limit), queryRoutineWrapper);
        List<Routine> records = pageData.getRecords();
        QueryWrapper<Resource> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", records.stream().map(Routine::getResource).toArray());
        List<Resource> resources = resourceMapper.selectList(queryWrapper);
        Map<String, Resource> resourceMap = resources.stream()
                .collect(Collectors.toMap(Resource::getId, resource -> resource));
        records.forEach(routine -> {
            Resource resource = resourceMap.get(routine.getResource());
            if (resource != null) {
                routine.setResourceName(resource.getName());
            } else {
                routine.setResourceName("手动录入");
            }
        });
        return new ResultData(200, "OK", pageData);
    }

    private String uniqueId() {
        String uuid = Routine.randomId();
        while (routineMapper.selectById(uuid) != null) {
            uuid = Resource.randomId();
        }
        return uuid;
    }

    public ResultData checkData(Routine routine) {
        QueryWrapper<Routine> wrapper = new QueryWrapper<>();
        wrapper.eq("name", routine.getName());
        if (routineMapper.selectCount(wrapper) > 0) {
            return new ResultData(406, "此名称已存在!");
        }
        return new ResultData(200, "OK");
    }

    public ResultData updateData(Routine routine) {
        // 先清除前后空字符
        routine.setName(routine.getName().trim());
        routine.setSeason(routine.getSeason().trim());
        routine.setTmdbId(routine.getTmdbId().trim());
        routine.setDoubanId(routine.getDoubanId().trim());
        routine.setRename(routine.getRename().trim());
        routine.setDelmark(routine.getDelmark().trim());
        // 检查名称是否已经重复
        QueryWrapper<Routine> wrapper = new QueryWrapper<>();
        wrapper.ne("id", routine.getId()).and(true, x -> x.eq("name", routine.getName()));
        if (routineMapper.selectCount(wrapper) > 0) {
            return new ResultData(406, "此名称已存在!");
        }
        // 检查季,tmdb和douban
        try {
            Long.parseLong(routine.getSeason());
        } catch (Exception e) {
            return new ResultData(406, "此季标识不符合规范!");
        }
        try {
            Long.parseLong(routine.getTmdbId());
        } catch (Exception e) {
            return new ResultData(406, "此TMDB标识ID不符合规范!");
        }
        try {
            Long.parseLong(routine.getDoubanId());
        } catch (Exception e) {
            return new ResultData(406, "此豆瓣标识ID不符合规范!");
        }
        // 更新例程
        routine.setStart(nowTime());
        routineMapper.updateById(routine);
        return new ResultData(200, "OK");
    }

}