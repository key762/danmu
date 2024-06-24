package skiree.host.danmu.service.core.iqiyi;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import skiree.host.danmu.service.core.iqiyi.modle.Node;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class iQiYiEngine {


    public static Map<Integer, Object> idMap(List<String> sources) {
        if (CollectionUtils.isEmpty(sources)) {
            throw new RuntimeException("必须配置source属性");
        }
        String videoIds = "";
        while (StringUtils.isBlank(videoIds)) {
            try {
                String resultInfo = HttpUtil.get(sources.get(0), CharsetUtil.CHARSET_UTF_8);
                Matcher matcher2 = Pattern.compile("window\\.QiyiPlayerProphetData=\\{\"tvid\":(\\d+),").matcher(resultInfo);
                if (matcher2.find()) {
                    videoIds = matcher2.group(1);
                }
            } catch (Exception e) {
                videoIds = "";
            }
        }
        String device_id = "886da22f802790939404d609e17d421e";
        String app_version = "12.61.16237";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String url = "https://mesh.if.iqiyi.com/tvg/v2/lw/base_info";
        String sign = DigestUtil.md5Hex("ad_param=&app_version=" + app_version + "&auth_cookie=&conduit_id=&device_id=" + device_id + "&entity_id=" + videoIds + "&ext=&os=&src=pca_tvg&timestamp=" + timestamp + "&user_id=0&vip_status=0&vip_type=-1&secret_key=howcuteitis").toUpperCase();
        String temp = "entity_id=" + videoIds + "&device_id=" + device_id + "&auth_cookie=&user_id=0&vip_type=-1&vip_status=0&conduit_id=&app_version=" + app_version + "&ext=&timestamp=" + timestamp + "&src=pca_tvg&os=&ad_param=&sign=" + sign;
        String result = HttpUtil.get(url + "?" + temp, CharsetUtil.CHARSET_UTF_8);
        Map<String, List<Object>> object = (Map<String, List<Object>>) JSONUtil.getByPath(JSONUtil.parse(result), "$.data.template.tabs[0].blocks[2].data.data[0].videos.feature_paged");
        Map<Integer, Node> idUrl = new HashMap<>();
        object.forEach((k, v) -> {
            JSONArray array = JSONUtil.parseArray(v);
            for (Object o : array) {
                JSON del = JSONUtil.parse(o);
                if (del.getByPath("content_type").toString().equals("1")) {
                    Node node = new Node();
                    node.setUrl(del.getByPath("page_url").toString());
                    node.setIndex(Integer.parseInt(del.getByPath("album_order").toString()));
                    idUrl.put(node.getIndex(), node);
                }
            }
        });
        return new HashMap<>(idUrl);
    }

}