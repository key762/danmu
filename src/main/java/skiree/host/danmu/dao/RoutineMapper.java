package skiree.host.danmu.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;
import skiree.host.danmu.model.Routine;

@Component
@Mapper
public interface RoutineMapper extends BaseMapper<Routine> {

}