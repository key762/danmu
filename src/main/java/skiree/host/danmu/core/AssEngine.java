package skiree.host.danmu.core;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.vdurmont.emoji.EmojiParser;
import skiree.host.danmu.data.AssConf;
import skiree.host.danmu.data.TxDanMu;
import skiree.host.danmu.data.TxDanMuBar;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class AssEngine {

    public static Map<Long, List<TxDanMu>> getDanMu(String v) {
        Map<Long, List<TxDanMu>> res = new HashMap<>();
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

    public static void getExtraDanMu(Map<Long, List<TxDanMu>> all, String url) {
        Map<Long, List<TxDanMu>> res = new HashMap<>();
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
                TxDanMu txDanMu = new TxDanMu();
                txDanMu.setOffset(offset);
                txDanMu.setContent(content);
                txDanMu.setStyle(style);
                txDanMu.setScore(score);
                txDanMu.setTime(time);
                if (res.containsKey(offset)) {
                    res.get(offset).add(txDanMu);
                } else {
                    List<TxDanMu> list = new ArrayList<>();
                    list.add(txDanMu);
                    res.put(offset, list);
                }
            }
        }
        all.putAll(res);
    }


    public static Collection<String> calcDanMu(Map<Long, List<TxDanMu>> res) {
        List<String> daMus = new ArrayList<>();
        for (Long key : res.keySet()) {
            List<TxDanMu> value = res.get(key);
            if (value.size() > 3) {
                List<TxDanMu> topThree = value.stream()
                        .sorted(Comparator.comparing(TxDanMu::getScore).reversed())
                        .limit(3)
                        .collect(Collectors.toList());
                res.put(key, topThree);
            }
        }
        Map<Long, List<TxDanMu>> sortedRes = new TreeMap<>(res);
        TxDanMuBar TxDanMuBar1 = new TxDanMuBar("{\\move(1920,100,END_MARK,100)\\fs" + AssConf.size + "}");
        TxDanMuBar TxDanMuBar2 = new TxDanMuBar("{\\move(1920,200,END_MARK,200)\\fs" + AssConf.size + "}");
        TxDanMuBar TxDanMuBar3 = new TxDanMuBar("{\\move(1920,300,END_MARK,300)\\fs" + AssConf.size + "}");
        TxDanMuBar TxDanMuBar4 = new TxDanMuBar("{\\move(1920,400,END_MARK,400)\\fs" + AssConf.size + "}");
        for (Map.Entry<Long, List<TxDanMu>> entry : sortedRes.entrySet()) {
            for (TxDanMu txDanMu : entry.getValue()) {
                String strD = "Dialogue: 3," + txDanMu.getDanMuTime() + "," + txDanMu.getEndDanMuTime() + ",Default-Box,atg1,0,0,0,,";
                if (TxDanMuBar1.mark) {
                    TxDanMuBar1.nTime = txDanMu.gkTime();
                    TxDanMuBar1.mark = false;
                    daMus.add(strD + TxDanMuBar1.pos.replace("END_MARK", txDanMu.getOutPix()) + txDanMu.getStyle() + txDanMu.getContent());
                } else if (TxDanMuBar2.mark) {
                    TxDanMuBar2.nTime = txDanMu.gkTime();
                    TxDanMuBar2.mark = false;
                    daMus.add(strD + TxDanMuBar2.pos.replace("END_MARK", txDanMu.getOutPix()) + txDanMu.getStyle() + txDanMu.getContent());
                } else if (TxDanMuBar3.mark) {
                    TxDanMuBar3.nTime = txDanMu.gkTime();
                    TxDanMuBar3.mark = false;
                    daMus.add(strD + TxDanMuBar3.pos.replace("END_MARK", txDanMu.getOutPix()) + txDanMu.getStyle() + txDanMu.getContent());
                } else if (TxDanMuBar4.mark) {
                    TxDanMuBar4.nTime = txDanMu.gkTime();
                    TxDanMuBar4.mark = false;
                    daMus.add(strD + TxDanMuBar4.pos.replace("END_MARK", txDanMu.getOutPix()) + txDanMu.getStyle() + txDanMu.getContent());
                }
            }
            TxDanMuBar1.next();
            TxDanMuBar2.next();
            TxDanMuBar3.next();
            TxDanMuBar4.next();
        }
        return daMus;
    }
}
