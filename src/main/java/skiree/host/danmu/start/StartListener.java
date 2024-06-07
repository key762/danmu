package skiree.host.danmu.start;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import skiree.host.danmu.core.ProcessEngine;
import skiree.host.danmu.data.ASS;
import skiree.host.danmu.data.Detail;
import skiree.host.danmu.data.Task;

import javax.annotation.Resource;

@Component
public class StartListener implements ApplicationRunner {

    @Resource
    private Task task;

    private static final SchedulerFactory gSchedulerFactory = new StdSchedulerFactory();

    public void run(ApplicationArguments args) throws SchedulerException {
        for (String publicAss : ASS.PUBLIC_ASS) {
            System.out.println(publicAss);
        }
        Scheduler scheduler = gSchedulerFactory.getScheduler();
        if (!task.getDanmu().isEmpty()) {
            for (Detail detail : task.getDanmu()) {
                if (detail.isEnable()) {
                    if (detail.isImmediately()) {
                        ProcessEngine.doProcess(detail);
                    }
                    try {
                        JobDataMap jobDataMap = new JobDataMap();
                        jobDataMap.put("job", detail);
                        JobDetail jobDetail = JobBuilder.newJob(DanMuTask.class)
                                .withIdentity(detail.getDesc())
                                .usingJobData(jobDataMap).build();
                        if (!scheduler.checkExists(jobDetail.getKey())) {
                            Trigger trigger = TriggerBuilder.newTrigger().withSchedule(CronScheduleBuilder.cronSchedule(detail.getCron()).withMisfireHandlingInstructionDoNothing()).build();
                            scheduler.scheduleJob(jobDetail, trigger);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (!scheduler.isStarted()) {
            scheduler.start();
        }
    }

}