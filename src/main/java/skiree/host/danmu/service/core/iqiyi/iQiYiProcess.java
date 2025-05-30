package skiree.host.danmu.service.core.iqiyi;

import org.springframework.stereotype.Component;
import skiree.host.danmu.model.engine.DanMu;
import skiree.host.danmu.service.core.Stratege;

import java.util.List;
import java.util.Map;

@Component
public class iQiYiProcess implements Stratege {

    @Override
    public Map<Long, List<DanMu>> getDanMu(String url) {
        return iQiYiAssEngine.getDanMu(url);
    }

    @Override
    public boolean supports(String string) {
        return string.contains("www.iqiyi.com");
    }
}