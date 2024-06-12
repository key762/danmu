package skiree.host.danmu.core.iqiyi;

import org.springframework.stereotype.Component;
import skiree.host.danmu.core.Stratege;
import skiree.host.danmu.data.Detail;

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
