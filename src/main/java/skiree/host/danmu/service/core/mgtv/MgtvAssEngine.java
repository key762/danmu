package skiree.host.danmu.service.core.mgtv;

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

public class MgtvAssEngine {

    public static Map<Long, List<DanMu>> getDanMu(String url) {
        Map<Long, List<DanMu>> danMuMap = new HashMap<>();
        Matcher matcher = Pattern.compile("/(\\d+)/(\\d+)\\.html").matcher(url);
        if (matcher.find()) {
            String cid = matcher.group(1);
            String vid = matcher.group(2);
            String mgUrl = "https://pcweb.api.mgtv.com/video/info?cid=" + cid + "&vid=" + vid;
            String result = HttpUtil.get(mgUrl, CharsetUtil.CHARSET_UTF_8);
            String timeStr = JSONUtil.parseObj(result).getByPath("$.data.info.time", String.class);
            int allTime = (Integer.parseInt(timeStr.split(":")[0]) * 60) + Integer.parseInt(timeStr.split(":")[1]);
            for (Integer i = 0; i < allTime; i += 60) {
                String danMuUrl = "https://galaxy.bz.mgtv.com/rdbarrage?cid=" + cid + "&vid=" + vid + "&time=" + i * 1000;
                getExtraDanMu(danMuMap, danMuUrl);
            }
        }
        return danMuMap;
    }

    public static void getExtraDanMu(Map<Long, List<DanMu>> all, String url) {
        Map<Long, List<DanMu>> res = new HashMap<>();
        String result = HttpUtil.get(url, CharsetUtil.CHARSET_UTF_8);
        if (JSONUtil.parse(result).getByPath("$.data.items") == null) return;
        JSONArray jsonArray = JSONUtil.parseArray(JSONUtil.parse(result).getByPath("$.data.items").toString());
        for (Object object : jsonArray) {
            JSONObject data = JSONUtil.parseObj(object.toString());
            String content = data.getStr("content");
            content = EmojiParser.removeAllEmojis(content);
            if (!content.isEmpty()) {
                Long offset = data.getLong("time");
                if (offset < 1000L) {
                    offset = 0L;
                } else {
                    String offStr = data.getStr("time");
                    Long duoOff = Long.parseLong(offStr.substring(offStr.length() - 3));
                    if (duoOff > 400L) {
                        offset = (offset - duoOff) + 1000;
                    } else {
                        offset = offset - duoOff;
                    }
                }
                DanMu danMu = new DanMu();
                danMu.setOffset(offset);
                danMu.setScore(new BigDecimal(0));
                danMu.setContent(content);
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
