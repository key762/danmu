package skiree.host.danmu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import skiree.host.danmu.dao.ResourceMapper;
import skiree.host.danmu.model.Resource;

import java.util.List;

@Service
public class ResourceService {

    @Autowired
    private ResourceMapper resourceMapper;

    public List<Resource> selectAll() {
        return resourceMapper.selectList(null);
    }

    public void deleteById(String resourceId) {
        resourceMapper.deleteById(resourceId);
    }

    public void saveData(String name, String path) {
        Resource resource = new Resource();
        String uuid = Resource.randomId();
        while (resourceMapper.selectById(uuid) != null) {
            uuid = Resource.randomId();
        }
        resource.setId(uuid);
        resource.setName(name);
        resource.setPath(path);
        resourceMapper.insert(resource);
    }

}


