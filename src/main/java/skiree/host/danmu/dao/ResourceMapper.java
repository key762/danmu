package skiree.host.danmu.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;
import skiree.host.danmu.model.Resource;
import java.util.List;

@Component
@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {

    List<Resource> selectAll();

}


