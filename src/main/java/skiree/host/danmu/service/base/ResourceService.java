package skiree.host.danmu.service.base;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import skiree.host.danmu.dao.ResourceMapper;
import skiree.host.danmu.dao.RoutineMapper;
import skiree.host.danmu.model.base.Resource;
import skiree.host.danmu.model.base.ResultData;
import skiree.host.danmu.model.base.Routine;
import skiree.host.danmu.service.tmdb.AutomaticService;

import java.util.List;

@Service
public class ResourceService extends BaseService {

    @Autowired
    public ResourceMapper resourceMapper;

    @Autowired
    public RoutineMapper routineMapper;

    @Autowired
    public AutomaticService automaticService;

    @Autowired
    public RoutineService routineService;


    public ResultData deleteData(String id) {
        // 先删除与其先关联的例程
        QueryWrapper<Routine> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("resource", id);
        List<Routine> routines = routineMapper.selectList(queryWrapper);
        routines.forEach(routine -> routineService.deleteData(routine.id));
        // 删除资源
        resourceMapper.deleteById(id);
        return new ResultData(200, "OK");
    }

    public ResultData saveData(String name, String path) {
        // 先清除前后空字符
        name = name.trim();
        path = path.trim();
        // 先校验路径是否有效
        if (!FileUtil.exist(path)) {
            return new ResultData(406, "服务端不存在此路径");
        }
        // 检查名称是否已经重复
        QueryWrapper<Resource> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        if (resourceMapper.selectCount(wrapper) > 0) {
            return new ResultData(406, "此名称已存在!");
        }
        // 检查路径是否已经重复
        wrapper.clear();
        wrapper.eq("path", path);
        if (resourceMapper.selectCount(wrapper) > 0) {
            return new ResultData(406, "此路径已存在!");
        }
        // 新增资源
        Resource resource = new Resource(uniqueId(), name, path, nowTime());
        resourceMapper.insert(resource);
        // 新增结束直接解析
        analysisData(resource.getId());
        return new ResultData(200, "OK");
    }

    @Async
    public void analysisData(String id) {
        // 先删除与其先关联的例程
        QueryWrapper<Routine> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("resource", id);
        List<Routine> routines = routineMapper.selectList(queryWrapper);
        routines.forEach(routine -> routineService.deleteData(routine.id));
        // 解析资源
        Resource resource = resourceMapper.selectById(id);
        // 处理媒体库
        automaticService.dealRootPath(resource.getPath(), resource.getId());
    }

    public ResultData updateData(String id, String name) {
        // 检查名称是否已经存在
        QueryWrapper<Resource> wrapper = new QueryWrapper<>();
        wrapper.ne("id", id).and(true, x -> x.eq("name", name));
        if (resourceMapper.selectCount(wrapper) > 0) {
            return new ResultData(406, "此名称已存在!");
        }
        // 更新资源
        Resource resource = resourceMapper.selectById(id);
        resource.setId(id);
        resource.setName(name);
        resource.setStart(nowTime());
        resourceMapper.updateById(resource);
        return new ResultData(200, "OK");
    }

    public ResultData pageList(int page, int limit) {
        QueryWrapper<Resource> queryExecuteWrapper = new QueryWrapper<>();
        queryExecuteWrapper.orderByDesc("start");
        return new ResultData(200, "OK", resourceMapper.selectPage(new Page<>(page, limit), queryExecuteWrapper));
    }

    private String uniqueId() {
        String uuid = Resource.randomId();
        while (resourceMapper.selectById(uuid) != null) {
            uuid = Resource.randomId();
        }
        return uuid;
    }

}