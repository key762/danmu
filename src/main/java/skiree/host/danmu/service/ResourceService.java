package skiree.host.danmu.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import skiree.host.danmu.dao.ResourceMapper;
import skiree.host.danmu.model.Resource;
import skiree.host.danmu.model.ResultData;

import java.util.List;

@Service
public class ResourceService {

    @Autowired
    private ResourceMapper resourceMapper;


    public ResultData selectAll() {
        List<Resource> resources = resourceMapper.selectList(null);
        ResultData resultData = new ResultData();
        resultData.setStatus(200);
        resultData.setMessage("查询成功");
//        resultData.setTotal(resources.size());
        resultData.setData(resources);
        return resultData;
    }

    public ResultData deleteData(String id) {
        resourceMapper.deleteById(id);
        return new ResultData(200, "OK");
    }

    public ResultData saveData(String name, String path) {
        resourceMapper.insert(new Resource(uniqueId(), name, path));
        return new ResultData(200, "OK");
    }

    public ResultData updateData(String id, String name, String path) {
        QueryWrapper<Resource> wrapper = new QueryWrapper<>();
        wrapper.ne("id", id).and(true, x -> x.eq("name", name).or().eq("path", path));
        if (resourceMapper.selectCount(wrapper) > 0) {
            return new ResultData(406, "此名称或路径已存在!");
        }
        resourceMapper.updateById(new Resource(id, name, path));
        return new ResultData(200, "OK");
    }

    public ResultData checkData(String name, String path) {
        QueryWrapper<Resource> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name).or().eq("path", path);
        if (resourceMapper.selectCount(wrapper) > 0) {
            return new ResultData(406, "此名称或路径已存在!");
        }
        return new ResultData(200, "OK");
    }

    public ResultData pageList(int page, int limit) {
        return new ResultData(200, "OK", resourceMapper.selectPage(new Page<>(page, limit), null));
    }

    private String uniqueId() {
        String uuid = Resource.randomId();
        while (resourceMapper.selectById(uuid) != null) {
            uuid = Resource.randomId();
        }
        return uuid;
    }

}