package skiree.host.danmu.service.core.vqq;

import org.springframework.stereotype.Component;
import skiree.host.danmu.model.TaskDo;
import skiree.host.danmu.model.engine.DanMu;
import skiree.host.danmu.service.core.Stratege;

import java.util.List;
import java.util.Map;

@Component
public class vQqProcess implements Stratege {

    @Override
    public Map<Integer, Object> idMap(TaskDo taskDo) {
        return vQqEngine.idMap(taskDo.routine.sources);
    }

    @Override
    public Map<Long, List<DanMu>> getDanMu(Object object) {
        return vQqAssEngine.getDanMu(object.toString());
    }

    @Override
    public boolean supports(TaskDo taskDo) {
        return taskDo.routine.type.equalsIgnoreCase("vqq");
    }
}