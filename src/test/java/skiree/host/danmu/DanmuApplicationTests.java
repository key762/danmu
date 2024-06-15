package skiree.host.danmu;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import skiree.host.danmu.dao.UserMapper;
import skiree.host.danmu.model.User;

import javax.annotation.Resource;

@SpringBootTest
class DanmuApplicationTests {

    @Resource
    private UserMapper userMapper;


    @Test
    void contextLoads() {
        User user = new User();
        user.setId(2);
        user.setName("betsy");
        userMapper.insert(user);
        System.out.println(userMapper.QueryUser());
    }

}
