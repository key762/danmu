package skiree.host.danmu.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;
import skiree.host.danmu.model.Execute;

@Component
@Mapper
public interface ExecuteMapper extends BaseMapper<Execute> {

}