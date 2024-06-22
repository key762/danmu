package skiree.host.danmu.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import skiree.host.danmu.dao.ResourceMapper;
import skiree.host.danmu.dao.RoutineMapper;
import skiree.host.danmu.model.Resource;
import skiree.host.danmu.model.ResultData;
import skiree.host.danmu.model.Routine;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoutineService {

    @Autowired
    private RoutineMapper routineMapper;

    @Autowired
    private ResourceMapper resourceMapper;

    public ResultData deleteData(String id) {
        routineMapper.deleteById(id);
        return new ResultData(200, "OK");
    }

    public ResultData saveData(Routine routine) {
        ResultData data = checkData(routine);
        if (data.status != 200) return data;
        routine.setId(uniqueId());
        routineMapper.insert(routine);
        return new ResultData(200, "OK");
    }

    public ResultData pageList(int page, int limit) {
        Page<Routine> pageData = routineMapper.selectPage(new Page<>(page, limit), null);
        List<Routine> records = pageData.getRecords();
        QueryWrapper<Resource> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", records.stream().map(Routine::getResource).toArray());
        List<Resource> resources = resourceMapper.selectList(queryWrapper);
        Map<String, Resource> resourceMap = resources.stream()
                .collect(Collectors.toMap(Resource::getId, resource -> resource));
        records.forEach(routine -> {
            Resource resource = resourceMap.get(routine.getResource());
            routine.setResourceName(resource.getName());
            routine.setResourcePath(resource.getPath());
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
        ResultData resultData = defaultCheck(routine);
        if (resultData.status != 200) return resultData;
        QueryWrapper<Routine> wrapper = new QueryWrapper<>();
        wrapper.eq("name", routine.getName());
        if (routineMapper.selectCount(wrapper) > 0) {
            return new ResultData(406, "此名称已存在!");
        }
        return new ResultData(200, "OK");
    }


    private String checkSourceType(Routine routine) {
        switch (routine.getType()) {
            case "vqq":
                for (String s : JSONUtil.parseArray(routine.getSource()).toList(String.class)) {
                    if (!s.startsWith("https://v.qq.com")) {
                        return "腾讯视频,不支持此连接[" + s + "]";
                    }
                }
                return "";
            case "iqiyi":
                for (String s : JSONUtil.parseArray(routine.getSource()).toList(String.class)) {
                    if (!s.startsWith("https://www.iqiyi.com")) {
                        return "爱奇艺视频,不支持此连接[" + s + "]";
                    }
                }
                return "";
            default:
                return "未知平台,暂不支持!";
        }
    }

    public ResultData updateData(Routine routine) {
        ResultData resultData = defaultCheck(routine);
        if (resultData.status != 200) return resultData;
        QueryWrapper<Routine> wrapper = new QueryWrapper<>();
        wrapper.ne("id", routine.getId()).and(true, x -> x.eq("name", routine.getName()));
        if (routineMapper.selectCount(wrapper) > 0) {
            return new ResultData(406, "此名称已存在!");
        }
        routineMapper.updateById(routine);
        return new ResultData(200, "OK");
    }


    private ResultData defaultCheck(Routine routine) {
        Resource resource = resourceMapper.selectById(routine.getResource());
        routine.setPath(routine.getPath().replace(resource.getPath(), ""));
        if (StringUtils.isBlank(routine.getPath())) {
            return new ResultData(406, "资源路径不符合要求,当前路径[" + routine.getPath() + "]!");
        }
        if (StringUtils.isBlank(routine.getRename())) {
            return new ResultData(406, "重名必须设置!");
        }
        if (routine.getSource().isEmpty()) {
            return new ResultData(406, "播源必须设置!");
        }
        String mark = checkSourceType(routine);
        if (!mark.isEmpty()) {
            return new ResultData(406, mark);
        }
        return new ResultData(200, "OK");
    }

}