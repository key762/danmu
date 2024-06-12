package skiree.host.danmu.core.vqq;

import org.springframework.stereotype.Component;
import skiree.host.danmu.core.Stratege;
import skiree.host.danmu.data.Detail;

@Component
public class vQqProcess implements Stratege {
    @Override
    public void doProcess(Detail detail) {
        vQqEngine.doProcess(detail);
    }

    @Override
    public boolean supports(Detail detail) {
        return detail.type.equalsIgnoreCase("vqq");
    }
}
