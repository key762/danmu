package skiree.host.danmu.service.base;

import cn.hutool.core.date.DateUtil;
import org.springframework.stereotype.Service;
import skiree.host.danmu.model.ass.AssConf;
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

    public static Collection<String> calcDanMu(Map<Long, List<DanMu>> res) {
        List<String> daMus = new ArrayList<>();
        for (Long key : res.keySet()) {
            List<DanMu> value = res.get(key);
            if (value.size() > 3) {
                List<DanMu> topThree = value.stream()
                        .sorted(Comparator.comparing(DanMu::getScore).reversed())
                        .limit(3)
                        .collect(Collectors.toList());
                res.put(key, topThree);
            }
        }
        Map<Long, List<DanMu>> sortedRes = new TreeMap<>(res);
        DanMuBar[] danMuBars = new DanMuBar[4];
        for (int i = 0; i < danMuBars.length; i++) {
            danMuBars[i] = new DanMuBar("{\\move(START_MARK," + (80 + i * 80) + ",END_MARK," + (80 + i * 80) + ",0,TIME_MARK)\\fs" + AssConf.size + "}");
        }
        Long maxed = Collections.max(sortedRes.keySet());
        Lock lock = new ReentrantLock();
        for (long i = 0; i < (maxed + 1); i++) {
            List<DanMu> danMus = sortedRes.get(i);
            if (danMus != null && !danMus.isEmpty()) {
                for (DanMu danMu : danMus) {
                    lock.lock();
                    for (DanMuBar muBar : danMuBars) {
                        if (muBar.mark) {
                            muBar.mark = false;
                            muBar.nTime = danMu.gkTime();
                            muBar.pos = muBar.pos.replace("START_MARK", danMu.getStart());
                            muBar.pos = muBar.pos.replace("END_MARK", danMu.getOutPix());
                            muBar.pos = muBar.pos.replace("TIME_MARK", danMu.getTimeMark());
                            String str = "Dialogue: 3," + danMu.getDanMuTime() + "," + danMu.getDanMuTimeEnd() + ",Default-Box,atg1,0,0,0,,";
                            daMus.add(str + muBar.pos + danMu.getStyle() + danMu.getContent());
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