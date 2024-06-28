package skiree.host.danmu.service.core.mgtv;

import org.springframework.stereotype.Component;
import skiree.host.danmu.model.engine.DanMu;
import skiree.host.danmu.service.core.Stratege;
import skiree.host.danmu.service.core.iqiyi.iQiYiAssEngine;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class MgtvProcess implements Stratege {
    @Override
    public Map<Long, List<DanMu>> getDanMu(String url) {
        return MgtvAssEngine.getDanMu(url);
    }

    @Override
    public boolean supports(String string) {
        return string.contains("www.mgtv.com");
    }
}
