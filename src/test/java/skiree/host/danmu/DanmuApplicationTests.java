package skiree.host.danmu;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import skiree.host.danmu.service.tmdb.AutomaticService;

import javax.annotation.Resource;

@SpringBootTest
class DanmuApplicationTests {

    @Resource
    private AutomaticService automaticService;

    @Test
    void contextLoads() {
        automaticService.dealRootPath("/Users/anorak/Downloads/TestFileDir","");
    }

}