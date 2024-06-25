package skiree.host.danmu;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import skiree.host.danmu.dao.UserMapper;

import javax.annotation.Resource;

@SpringBootTest
class DanmuApplicationTests {

    @Resource
    private UserMapper userMapper;


    @Test
    void contextLoads() {
    }

}