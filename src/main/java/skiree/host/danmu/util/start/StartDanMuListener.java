package skiree.host.danmu.util.start;

import com.alibaba.dcm.DnsCacheManipulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import skiree.host.danmu.service.base.ConfigService;

@Component
public class StartDanMuListener implements ApplicationRunner {

    @Autowired
    private ConfigService configService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        configService.updateConfig();
        DnsCacheManipulator.setDnsCache("api.themoviedb.org", "13.32.50.105");
    }
}
