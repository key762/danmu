package skiree.host.danmu.util.start;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Component;
import skiree.host.danmu.service.core.Stratege;
import skiree.host.danmu.model.task.Detail;
import skiree.host.danmu.model.task.Task;

import javax.annotation.Resource;

@Component
public class StartListener implements ApplicationRunner {

    @Resource
    private Task task;

    @Resource
    private PluginRegistry<Stratege, Detail> registry;

    private static final SchedulerFactory gSchedulerFactory = new StdSchedulerFactory();

    public void run(ApplicationArguments args) throws SchedulerException {
        Scheduler scheduler = gSchedulerFactory.getScheduler();
        if (task != null && !task.getDanmu().isEmpty()) {
            for (Detail detail : task.getDanmu()) {
                if (detail.isEnable()) {
                    if (detail.isImmediately()) {
                        Stratege stratege = registry.getRequiredPluginFor(detail);
                        stratege.doProcess(detail);
                    }
                    if (detail.isTimer()) {
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

}