package skiree.host.danmu.service.core;

import org.springframework.plugin.core.Plugin;
import skiree.host.danmu.model.task.Detail;

public interface Stratege extends Plugin<Detail> {

    void doProcess(Detail detail);

}
