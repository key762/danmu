package skiree.host.danmu.util.start;

import com.alibaba.dcm.DnsCacheManipulator;
import org.bytedeco.ffmpeg.global.avutil;
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
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
        avutil.av_log_set_level(avutil.AV_LOG_ERROR);
    }
}
