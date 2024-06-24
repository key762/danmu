package skiree.host.danmu.service.core.iqiyi;

import org.springframework.stereotype.Component;
import skiree.host.danmu.model.TaskDo;
import skiree.host.danmu.model.engine.DanMu;
import skiree.host.danmu.service.core.Stratege;
import skiree.host.danmu.service.core.iqiyi.modle.Node;

import java.util.List;
import java.util.Map;

@Component
public class iQiYiProcess implements Stratege {

    @Override
    public Map<Integer, Object> idMap(TaskDo taskDo) {
        return iQiYiEngine.idMap(taskDo.routine.sources);
    }

    @Override
    public Map<Long, List<DanMu>> getDanMu(Object object) {
        return iQiYiAssEngine.getDanMu((Node) object);
    }

    @Override
    public boolean supports(TaskDo taskDo) {
        return taskDo.routine.type.equalsIgnoreCase("iqiyi");
    }
}