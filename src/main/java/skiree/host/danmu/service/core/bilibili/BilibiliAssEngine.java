package skiree.host.danmu.service.core.bilibili;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.vdurmont.emoji.EmojiParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import skiree.host.danmu.model.engine.DanMu;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BilibiliAssEngine {

    public static Map<Long, List<DanMu>> getDanMu(String url) {
        Map<Long, List<DanMu>> danMuMap = new HashMap<>();
        Matcher matcher = Pattern.compile("ep(\\d+)").matcher(url);
        if (matcher.find()) {
            String ep_id = matcher.group(1);
            String mgUrl = "https://api.bilibili.com/pgc/view/web/season?ep_id=" + ep_id;
            String result = HttpUtil.get(mgUrl, CharsetUtil.CHARSET_UTF_8);
            if (JSONUtil.parseObj(result).getInt("code") == 0) {
                JSONArray jsonArray = JSONUtil.parseArray(JSONUtil.parse(result).getByPath("$.result.episodes").toString());
                for (Object object : jsonArray) {
                    JSONObject data = JSONUtil.parseObj(object.toString());
                    String id = data.getStr("id");
                    if (id.equals(ep_id)) {
                        String xmlUrl = "https://comment.bilibili.com/" + data.getStr("cid") + ".xml";
                        String resultXml = HttpUtil.get(xmlUrl, CharsetUtil.CHARSET_UTF_8);
                        Document document = XmlUtil.parseXml(resultXml);
                        NodeList nodeList = document.getElementsByTagName("d");
                        for (int i = 0; i < nodeList.getLength(); i++) {
                            Node node = nodeList.item(i);
                            if (node.getNodeType() == Node.ELEMENT_NODE) {
                                Element element = (Element) node;
                                String content = element.getTextContent();
                                content = EmojiParser.removeAllEmojis(content);
                                if (!content.isEmpty()) {
                                    double dataStrDou = Double.parseDouble(element.getAttribute("p").split(",")[0]);
                                    Integer dataStrInt = Integer.parseInt(String.valueOf((int) (dataStrDou * 1000.0)));
                                    Long offset = Long.parseLong(String.valueOf(dataStrInt));
                                    if (offset < 1000L) {
                                        offset = 0L;
                                    } else {
                                        String offStr = String.valueOf(offset);
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
                                    if (danMuMap.containsKey(offset)) {
                                        danMuMap.get(offset).add(danMu);
                                    } else {
                                        List<DanMu> list = new ArrayList<>();
                                        list.add(danMu);
                                        danMuMap.put(offset, list);
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }
        return danMuMap;
    }

}