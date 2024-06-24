package skiree.host.danmu.service.core.vqq;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class vQqEngine {

    public static Map<Integer, Object> idMap(List<String> path) {
        return collIdMap(path.get(0));
    }

    private static Map<Integer, Object> collIdMap(String input) {
        Map<Integer, Object> sortedMap = new HashMap<>();
        try {
            Matcher matcher = Pattern.compile("/([a-zA-Z0-9]+)/([a-zA-Z0-9]+)\\.html$").matcher(input);
            if (matcher.find()) {
                String url = "https://m.v.qq.com/x/m/play?cid=" + matcher.group(1) + "&vid=" + matcher.group(2) + "&mobile=1";
                Document doc = Jsoup.connect(url).timeout(10000).get();
                Elements allElements = doc.getElementsByClass("pl-episode__container");
                Elements elements = allElements.get(0).getElementsByAttribute("data-vid");
                for (Element element : elements) {
                    String key = element.attributes().get("data-vid");
                    if (!element.getElementsByClass("video-item-rect__title").get(0).text().contains("彩蛋")) {
                        Integer index = Integer.valueOf(element.getElementsByClass("video-item-rect__title").get(0).text());
                        Elements ts = element.getElementsByClass("corner-wrap corner-mark--small corner-mark--rightTop");
                        if (!ts.isEmpty()) {
                            String mark = ts.get(0).text();
                            if (!mark.contains("预")) {
                                sortedMap.put(index, key);
                            }
                        } else {
                            sortedMap.put(index, key);
                        }
                    }
                }
                return sortedMap;
            } else {
                System.out.println("No match found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sortedMap;
    }

}