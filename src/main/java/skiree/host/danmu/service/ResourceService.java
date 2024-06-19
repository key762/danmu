package skiree.host.danmu.service;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import skiree.host.danmu.dao.ResourceMapper;
import skiree.host.danmu.dao.UserMapper;
import skiree.host.danmu.model.Resource;
import skiree.host.danmu.model.User;

import java.util.List;

@Service
public class ResourceService {

    @Autowired
    private ResourceMapper resourceMapper;

    public List<Resource> selectAll() {
        return resourceMapper.selectList(null);
    }

    public int deleteById(String resourceId) {
        return resourceMapper.deleteById(resourceId);
    }

    public void saveData(String name, String path) {
        Resource resource = new Resource();
        String uuid = UUID.randomUUID().toString();
        while ( resourceMapper.selectById(uuid) != null ){
            uuid = UUID.randomUUID().toString();
        }
        resource.setId(uuid);
        resource.setName(name);
        resource.setPath(path);
        resourceMapper.insert(resource);
    }
}


