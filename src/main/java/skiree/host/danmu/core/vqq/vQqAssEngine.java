package skiree.host.danmu.core.vqq;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.vdurmont.emoji.EmojiParser;
import skiree.host.danmu.data.AssConf;
import skiree.host.danmu.data.DanMu;
import skiree.host.danmu.data.DanMuBar;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class vQqAssEngine {

    public static Map<Long, List<DanMu>> getDanMu(String v) {
        Map<Long, List<DanMu>> res = new HashMap<>();
        List<String> baseUrl = getBaseUrl("https://dm.video.qq.com/barrage/base/" + v);
        for (String u : baseUrl) {
            getExtraDanMu(res, "https://dm.video.qq.com/barrage/segment/" + v + "/" + u);
        }
        return res;
    }

    public static List<String> getBaseUrl(String url) {
        List<String> list = new ArrayList<>();
        String result = HttpUtil.get(url, CharsetUtil.CHARSET_UTF_8);
        JSONObject jsonObject = JSONUtil.parseObj(JSONUtil.parse(result).getByPath("segment_index"));
        for (String key : jsonObject.keySet()) {
            JSONObject segmentObject = jsonObject.getJSONObject(key);
            String segmentName = segmentObject.getStr("segment_name");
            list.add(segmentName);
        }
        return list;
    }

    public static void getExtraDanMu(Map<Long, List<DanMu>> all, String url) {
        Map<Long, List<DanMu>> res = new HashMap<>();
        String result = HttpUtil.get(url, CharsetUtil.CHARSET_UTF_8);
        JSONArray jsonArray = JSONUtil.parseArray(JSONUtil.parse(result).getByPath("barrage_list").toString());
        for (Object object : jsonArray) {
            JSONObject data = JSONUtil.parseObj(object.toString());
            String content = data.getStr("content");
            content = EmojiParser.removeAllEmojis(content);
            if (!content.isEmpty()) {
                Long offset = data.getLong("time_offset");
                String style = data.getStr("content_style");
                BigDecimal score = data.getBigDecimal("content_score");
                Date time = new Date(data.getLong("create_time") * 1000);
                DanMu danMu = new DanMu();
                danMu.setOffset(offset);
                danMu.setContent(content);
                danMu.setStyle(style);
                danMu.setScore(score);
                danMu.setTime(time);
                if (res.containsKey(offset)) {
                    res.get(offset).add(danMu);
                } else {
                    List<DanMu> list = new ArrayList<>();
                    list.add(danMu);
                    res.put(offset, list);
                }
            }
        }
        all.putAll(res);
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
