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
        DanMuBar danMuBar1 = new DanMuBar("{\\move(1920,100,END_MARK,100)\\fs" + AssConf.size + "}");
        DanMuBar danMuBar2 = new DanMuBar("{\\move(1920,200,END_MARK,200)\\fs" + AssConf.size + "}");
        DanMuBar danMuBar3 = new DanMuBar("{\\move(1920,300,END_MARK,300)\\fs" + AssConf.size + "}");
        DanMuBar danMuBar4 = new DanMuBar("{\\move(1920,400,END_MARK,400)\\fs" + AssConf.size + "}");
        for (Map.Entry<Long, List<DanMu>> entry : sortedRes.entrySet()) {
            for (DanMu danMu : entry.getValue()) {
                String strD = "Dialogue: 3," + danMu.getDanMuTime() + "," + danMu.getEndDanMuTime() + ",Default-Box,atg1,0,0,0,,";
                if (danMuBar1.mark) {
                    danMuBar1.nTime = danMu.gkTime();
                    danMuBar1.mark = false;
                    daMus.add(strD + danMuBar1.pos.replace("END_MARK", danMu.getOutPix()) + danMu.getStyle() + danMu.getContent());
                } else if (danMuBar2.mark) {
                    danMuBar2.nTime = danMu.gkTime();
                    danMuBar2.mark = false;
                    daMus.add(strD + danMuBar2.pos.replace("END_MARK", danMu.getOutPix()) + danMu.getStyle() + danMu.getContent());
                } else if (danMuBar3.mark) {
                    danMuBar3.nTime = danMu.gkTime();
                    danMuBar3.mark = false;
                    daMus.add(strD + danMuBar3.pos.replace("END_MARK", danMu.getOutPix()) + danMu.getStyle() + danMu.getContent());
                } else if (danMuBar4.mark) {
                    danMuBar4.nTime = danMu.gkTime();
                    danMuBar4.mark = false;
                    daMus.add(strD + danMuBar4.pos.replace("END_MARK", danMu.getOutPix()) + danMu.getStyle() + danMu.getContent());
                }
            }
            danMuBar1.next();
            danMuBar2.next();
            danMuBar3.next();
            danMuBar4.next();
        }
        return daMus;
    }
}
