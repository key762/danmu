package skiree.host.danmu.service.core.vqq;

import org.springframework.stereotype.Component;
import skiree.host.danmu.service.core.Stratege;
import skiree.host.danmu.model.task.Detail;

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
