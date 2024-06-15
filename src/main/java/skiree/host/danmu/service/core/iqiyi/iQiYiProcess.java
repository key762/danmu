package skiree.host.danmu.service.core.iqiyi;

import org.springframework.stereotype.Component;
import skiree.host.danmu.service.core.Stratege;
import skiree.host.danmu.model.task.Detail;

@Component
public class iQiYiProcess implements Stratege {
    @Override
    public void doProcess(Detail detail) {
        iQiYiEngine.doProcess(detail);
    }

    @Override
    public boolean supports(Detail detail) {
        return detail.type.equalsIgnoreCase("iqiyi");
    }
}
