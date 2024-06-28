package skiree.host.danmu.service.base;

import cn.hutool.core.date.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import skiree.host.danmu.dao.LogMapper;
import skiree.host.danmu.model.base.Log;
import skiree.host.danmu.model.base.Resource;
import skiree.host.danmu.model.base.TaskDo;

import java.util.Date;

@Service
public class LogService {

    @Autowired
    private LogMapper logMapper;

    public synchronized void recordLog(TaskDo taskDo, String content) {
        Log log = new Log();
        log.setId(uniqueId());
        log.setExecute(taskDo.execute.id);
        log.setTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        log.setContent(content);
        log.setOrderNum(String.valueOf(System.currentTimeMillis()));
        logMapper.insert(log);
        try {
            Thread.sleep(1L);
        } catch (Exception ignore) {
        }
    }

    private String uniqueId() {
        String uuid = Log.randomId();
        while (logMapper.selectById(uuid) != null) {
            uuid = Resource.randomId();
        }
        return uuid;
    }

}