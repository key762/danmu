package skiree.host.danmu.service.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import skiree.host.danmu.dao.UserMapper;
import skiree.host.danmu.model.base.User;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public List<User> QueryUser() {
        return userMapper.QueryUser();
    }
}


