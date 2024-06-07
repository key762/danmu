package skiree.host.danmu.start;

import cn.hutool.core.date.DateUtil;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import skiree.host.danmu.data.Detail;

import java.util.Date;

public class DanMuTask extends QuartzJobBean {
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        Detail job = (Detail) context.getJobDetail().getJobDataMap().get("job");
        System.out.println(job.getDesc() + " 定时执行, 当前时间 : " + DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
    }
}
