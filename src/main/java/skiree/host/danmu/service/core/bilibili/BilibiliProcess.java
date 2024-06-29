package skiree.host.danmu.service.core.bilibili;

import org.springframework.stereotype.Component;
import skiree.host.danmu.model.engine.DanMu;
import skiree.host.danmu.service.core.Stratege;

import java.util.List;
import java.util.Map;

@Component
public class BilibiliProcess implements Stratege {


    @Override
    public Map<Long, List<DanMu>> getDanMu(String url) {
        return BilibiliAssEngine.getDanMu(url);
    }

    @Override
    public boolean supports(String string) {
        return string.contains("bilibili.com");
    }

}