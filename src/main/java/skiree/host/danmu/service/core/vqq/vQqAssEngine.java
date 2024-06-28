package skiree.host.danmu.service.core.vqq;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.vdurmont.emoji.EmojiParser;
import skiree.host.danmu.model.engine.DanMu;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class vQqAssEngine {

    public static Map<Long, List<DanMu>> getDanMu(String v) {
        Map<Long, List<DanMu>> res = new HashMap<>();
        Matcher matcherName = Pattern.compile("/([a-zA-Z0-9]+)\\.html").matcher(v);
        if (matcherName.find()) {
            v = matcherName.group(1);
        }
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

}
