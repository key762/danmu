package skiree.host.danmu.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;
import skiree.host.danmu.model.base.User;

import java.util.List;

@Component
@Mapper
public interface UserMapper extends BaseMapper<User> {

    List<User> QueryUser();

}