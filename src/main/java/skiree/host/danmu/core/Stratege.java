package skiree.host.danmu.core;

import org.springframework.plugin.core.Plugin;
import skiree.host.danmu.data.Detail;

public interface Stratege extends Plugin<Detail> {

    void doProcess(Detail detail);

}
