package skiree.host.danmu;

import cn.hutool.core.io.FileUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import skiree.host.danmu.model.ass.ASS;
import skiree.host.danmu.model.ass.AssConf;
import skiree.host.danmu.service.tmdb.AutomaticService;

import javax.annotation.Resource;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
class DanmuApplicationTests {

    @Resource
    private AutomaticService automaticService;

    @Test
    void contextLoads() {
        automaticService.dealRootPath("/Users/anorak/Downloads/TestFileDir","");
    }

}