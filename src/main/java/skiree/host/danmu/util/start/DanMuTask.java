package skiree.host.danmu.util.start;

import cn.hutool.core.date.DateUtil;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.scheduling.quartz.QuartzJobBean;
import skiree.host.danmu.service.core.Stratege;
import skiree.host.danmu.model.task.Detail;

import javax.annotation.Resource;
import java.util.Date;

public class DanMuTask extends QuartzJobBean {

    @Resource
    private PluginRegistry<Stratege, Detail> registry;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        Detail job = (Detail) context.getJobDetail().getJobDataMap().get("job");
        Stratege stratege = registry.getRequiredPluginFor(job);
        stratege.doProcess(job);
        System.out.println(job.getDesc() + " 定时执行, 当前时间 : " + DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
    }
}
