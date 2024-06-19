package skiree.host.danmu.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class BasicController {

    private static final SchedulerFactory gSchedulerFactory = new StdSchedulerFactory();

    @Value("${danmu.user}")
    private String user;

    @Value("${danmu.password}")
    private String fortPassword;

    @GetMapping("/show.html")
    public String show() {
        return "show";
    }

    @GetMapping({"/", "/home"})
    public String home() {
        if (StpUtil.isLogin()){
            return "home";
        }
        return "redirect:/login";
    }

    @GetMapping({"/login"})
    public String login() {
        if (StpUtil.isLogin()){
            return "redirect:/home";
        }
        return "login";
    }

    @GetMapping("/doLogout")
    public String doLogout() {
        StpUtil.logout("Admin");
        return "redirect:/login";
    }

    @PostMapping("/doLogin")
    public String doLogin(Model model, String username, String password) {
        if (user.equals(username) && fortPassword.equals(password)) {
            model.addAttribute("loginName", "Admin");
            StpUtil.login("Admin");
            return "redirect:/home";
        }
        return "loginFailed";
    }

    // http://127.0.0.1:8091/job
    @ResponseBody
    @RequestMapping("/job")
    public Object job() throws SchedulerException {
        List<Map<String, Object>> mapList = new ArrayList<>();
        Scheduler scheduler = gSchedulerFactory.getScheduler();
        try {
            GroupMatcher<JobKey> matcher = GroupMatcher.anyJobGroup();
            Set<JobKey> jobKeys = scheduler.getJobKeys(matcher);
            for (JobKey jobKey : jobKeys) {
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                for (Trigger trigger : triggers) {
                    Map<String, Object> temp = new HashMap<>();
                    temp.put("jobName", ((CronTriggerImpl) trigger).getJobName());
                    temp.put("startTime", DateUtil.formatDateTime(trigger.getStartTime()));
                    temp.put("nextFireTime", DateUtil.formatDateTime(trigger.getNextFireTime()));
                    Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                    temp.put("jobStatus", triggerState.name());
                    CronTrigger cronTrigger = (CronTrigger) trigger;
                    String cronExpression = cronTrigger.getCronExpression();
                    temp.put("jobTime", cronExpression);
                    mapList.add(temp);
                }
            }
        } catch (SchedulerException ignore) {
        }
        return mapList;
    }

}