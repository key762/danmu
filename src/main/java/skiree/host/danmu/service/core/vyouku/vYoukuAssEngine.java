package skiree.host.danmu.service.core.vyouku;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.vdurmont.emoji.EmojiParser;
import okhttp3.*;
import skiree.host.danmu.model.engine.DanMu;

import java.math.BigDecimal;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class vYoukuAssEngine {


    final static Map<String, String> DIY_HEADER = new HashMap<>();

    static String cna = "";

    static {
        DIY_HEADER.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
    }

    public static Map<Long, List<DanMu>> getDanMu(String url) {
        Map<Long, List<DanMu>> res = new HashMap<>();
        Matcher matcher = Pattern.compile("/id_([^.]+)\\.html").matcher(url);
        if (matcher.find()) {
            CookieManager cookieManager = new CookieManager();
            try {
                String vid = matcher.group(1);
                // 全局的Cookie存储
                cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
                CookieHandler.setDefault(cookieManager);
//                cookieManager.getCookieStore().removeAll();
                // 前置的请求
                if (cna.isEmpty()) {
                    HttpUtil.createGet("https://log.mmstat.com/eg.js").headerMap(DIY_HEADER, true).execute().body();
                }
                HttpUtil.createGet("https://acs.youku.com/h5/mtop.com.youku.aplatform.weakget/1.0/?jsv=2.5.1&appKey=24679788").headerMap(DIY_HEADER, true).execute().body();
                // 详情信息
                String vidInfo = JSONUtil.parseObj(HttpUtil.createGet("https://openapi.youku.com/v2/videos/show.json?client_id=53e6cc67237fc59a&video_id=" + vid + "&package=com.huawei.hwvplayer.youku&ext=show")
                        .headerMap(DIY_HEADER, true).execute().body()).getStr("duration");
                for (int i = 0; i < (((int) (Double.parseDouble(vidInfo) / 60)) + 1); i++) {
                    String danMuUrl = "https://acs.youku.com/h5/mopen.youku.danmu.list/1.0/?jsv=2.5.6&appKey=24679788&t=";
                    Map<String, Object> msg = new LinkedHashMap<>();
                    msg.put("ctime", System.currentTimeMillis());
                    msg.put("ctype", 10004);
                    msg.put("cver", "v1.0");
                    if (cna.isEmpty()) {
                        cna = cookieManager.getCookieStore().getCookies().stream().filter(x -> x.getName().equals("cna")).findFirst().get().getValue();
                    }
                    msg.put("guid", cna);
                    msg.put("mat", i);
                    msg.put("mcount", 1);
                    msg.put("pid", 0);
                    msg.put("sver", "3.1.0");
                    msg.put("type", 1);
                    msg.put("vid", vid);
                    String msgJson = new String(Base64.getEncoder().encode(JSONUtil.toJsonStr(msg).replace(" ", "").getBytes()));
                    msg.put("msg", msgJson);
                    String signStr = SecureUtil.md5(msgJson + "MkmC9SoIw6xCkSKHhJ7b5D2r51kBiREr");
                    msg.put("sign", signStr);
                    long nowTime = System.currentTimeMillis();
                    String h5Tk = StrUtil.sub(cookieManager.getCookieStore().getCookies().stream().filter(x -> x.getName().equals("_m_h5_tk")).findFirst().get().getValue(), 0, 32);
                    String signHttp = signPost(h5Tk, String.valueOf(nowTime), "24679788", JSONUtil.toJsonStr(msg).replace(" ", ""));
                    danMuUrl += nowTime + "&sign=" + signHttp + "&api=mopen.youku.danmu.list&v=1.0&type=originaljson&timeout=20000&dataType=jsonp&jsonpIncPrefix=utility";
                    String cookieStr = "";
                    for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
                        if (cookieStr.isEmpty()) {
                            cookieStr += cookie.getName() + "=" + cookie.getValue();
                        } else {
                            cookieStr += "; " + cookie.getName() + "=" + cookie.getValue();
                        }
                    }
                    OkHttpClient client = new OkHttpClient().newBuilder().build();
                    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                    RequestBody body = RequestBody.create(mediaType, "data=" + JSONUtil.toJsonStr(msg).replace(" ", ""));
                    Request request = new Request.Builder().url(danMuUrl)
                            .method("POST", body)
                            .addHeader("Cookie", cookieStr)
                            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36")
                            .addHeader("Referer", "https://v.youku.com")
                            .addHeader("Content-Type", "application/x-www-form-urlencoded")
                            .build();
                    Response response = client.newCall(request).execute();
                    String resData = null;
                    if (response.body() != null) {
                        resData = response.body().string();
                    }
                    String resultStr = JSONUtil.parseObj(JSONUtil.parseObj(resData).getByPath("$.data.result", String.class)).toString();
                    JSONArray jsonArray = JSONUtil.parseArray(JSONUtil.parse(resultStr).getByPath("$.data.result").toString());
                    for (Object o : jsonArray) {
                        JSONObject dataDanMu = JSONUtil.parseObj(o.toString());
                        String content = dataDanMu.getStr("content");
                        content = EmojiParser.removeAllEmojis(content);
                        if (!content.isEmpty()) {
                            Long offset = dataDanMu.getLong("playat");
                            if (offset < 1000L) {
                                offset = 0L;
                            } else {
                                String offStr = dataDanMu.getStr("playat");
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
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    private static String signPost(String h5Tk, String s, String number, String msg) {
        String text = String.join("&", Arrays.asList(h5Tk, s, number, msg));
        return SecureUtil.md5(text);
    }

}