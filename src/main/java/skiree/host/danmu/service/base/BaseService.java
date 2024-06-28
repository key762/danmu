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
            danMuBars[i] = new DanMuBar("{\\move(2880," + (80 + i * 80) + ",END_MARK," + (80 + i * 80) + ",TIME_MARK)\\fs" + AssConf.size + "}");
        }
        for (Map.Entry<Long, List<DanMu>> entry : sortedRes.entrySet()) {
            Lock lock = new ReentrantLock();
            for (DanMu danMu : entry.getValue()) {
                if (danMu.getOffset() < 6000L) {
                    lock.lock();
                    // 前六秒处理
                    String strD1 = "Dialogue: 3," + danMu.getDanMuTime() + "," + danMu.getDanMuTime(500L) + ",Default-Box,atg1,0,0,0,,";
                    String strD2 = "Dialogue: 3," + danMu.getDanMuTime(500L) + "," + danMu.getEndDanMuTime() + ",Default-Box,atg1,0,0,0,,";
                    for (DanMuBar muBar : danMuBars) {
                        if (muBar.mark) {
                            muBar.mark = false;
                            muBar.nTime = danMu.gkTime();
                            daMus.add(strD1 + muBar.pos.replace("END_MARK", "1920").replace("TIME_MARK", "0,500") + danMu.getStyle() + danMu.getContent());
                            daMus.add(strD2 + muBar.pos.replace("2880", "1920").replace("END_MARK", danMu.getOutPix()).replace("TIME_MARK", "0,18000") + danMu.getStyle() + danMu.getContent());
                            break;
                        }
                    }
                    lock.unlock();
                } else {
                    // 六秒后处理
                    String strD = "Dialogue: 3," + danMu.getDanMuTime() + "," + danMu.getEndDanMuTime() + ",Default-Box,atg1,0,0,0,,";
                    lock.lock();
                    String timeMark = "0,24000";
                    for (DanMuBar muBar : danMuBars) {
                        if (muBar.mark) {
                            muBar.mark = false;
                            muBar.nTime = danMu.gkTime();
                            daMus.add(strD + muBar.pos.replace("END_MARK", danMu.getOutPix()).replace("TIME_MARK", timeMark) + danMu.getStyle() + danMu.getContent());
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
