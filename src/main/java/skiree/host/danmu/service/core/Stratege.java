package skiree.host.danmu.service.core;

import org.springframework.plugin.core.Plugin;
import skiree.host.danmu.model.engine.DanMu;

import java.util.List;
import java.util.Map;

public interface Stratege extends Plugin<String> {

    Map<Long, List<DanMu>> getDanMu(String url);

}