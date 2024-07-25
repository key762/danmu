package skiree.host.danmu.service.base;

import cn.hutool.core.date.DateUtil;
import org.springframework.stereotype.Service;
import skiree.host.danmu.model.ass.ASS;
import skiree.host.danmu.model.ass.AssConf;
import skiree.host.danmu.model.ass.VideoInfo;
import skiree.host.danmu.model.engine.DanMu;
import skiree.host.danmu.model.engine.DanMuBar;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class BaseService {

    public static String nowTime() {
        return DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
    }

    public static Collection<String> calcDanMu(Map<Long, List<DanMu>> res, VideoInfo videoInfo) {
        List<String> daMus = new ArrayList<>();
        for (Long key : res.keySet()) {
            List<DanMu> value = res.get(key);
            if (value.size() > 3) {
                List<DanMu> topThree = value.stream()
                        .sorted(Comparator.comparing(DanMu::getScore).reversed())
                        .limit(3)
                        .collect(Collectors.toList());
                topThree.forEach(x -> x.setWidth(ASS.calculateWidth(x.getContent())));
                res.put(key, topThree);
            }
        }
        Map<Long, List<DanMu>> sortedRes = new TreeMap<>(res);
        DanMuBar[] danMuBars = new DanMuBar[AssConf.row];
        int bar = AssConf.size + 20;
        for (int i = 0; i < danMuBars.length; i++) {
            danMuBars[i] = new DanMuBar("{\\an4\\move(START_MARK," + (bar + i * bar) + ",END_MARK," + (bar + i * bar) + ",0,TIME_MARK)}");
        }
        Long maxed = Collections.max(sortedRes.keySet());
        Lock lock = new ReentrantLock();
        for (long i = 0; i < (maxed + 1); i++) {
            List<DanMu> danMus = sortedRes.get(i);
            if (danMus != null && !danMus.isEmpty()) {
                for (DanMu danMu : danMus) {
                    lock.lock();
                    for (DanMuBar muBar : danMuBars) {
                        if (muBar.mark && muBar.nTime <= 0) {
                            muBar.mark = false;
                            muBar.nTime = danMu.gkTime(videoInfo);
                            String template = muBar.pos;
                            template = template.replace("START_MARK", danMu.getStart(videoInfo));
                            template = template.replace("END_MARK", danMu.getOutPix());
                            template = template.replace("TIME_MARK", danMu.getTimeMark(videoInfo));
                            String str = "Dialogue: 3," + danMu.getDanMuTime() + "," + danMu.getDanMuTimeEnd(videoInfo) + ",Default-Box,atg1,0,0,0,,";
                            daMus.add(str + template + danMu.getStyle() + danMu.getContent());
                            break;
                        }
                    }
                    lock.unlock();
                }
            }
            Arrays.stream(danMuBars).forEach(DanMuBar::next);
        }
        return daMus;
    }

}