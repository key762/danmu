package skiree.host.danmu.core.vqq;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import skiree.host.danmu.data.ASS;
import skiree.host.danmu.data.Detail;
import skiree.host.danmu.data.DanMu;
import skiree.host.danmu.substitutor.CustomSubstitutor;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class vQqEngine {


    public static void doProcess(Detail detail) {
        ckAssFile(detail);
        Map<Integer, String> idName = idName(detail.getPath());
        if (idName.isEmpty()) {return;}
        Map<Integer, String> idMap = idMap(detail.getSource());
        Map<Integer, String> jobMap = idMap.entrySet().stream()
                .filter(entry -> idName.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        System.out.println("扫描到 " + jobMap.size() + " 个视频,即将开始抓取弹幕。");
        jobMap.forEach((k, v) -> {
            Map<Long, List<DanMu>> res = vQqAssEngine.getDanMu(v);
            List<String> temp = new ArrayList<>(ASS.PUBLIC_ASS);
            temp.addAll(vQqAssEngine.calcDanMu(res));
            HashMap<String, String> objectObjectHashMap = new HashMap<>();
            objectObjectHashMap.put("time", DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
            String newName = CustomSubstitutor.getInstance().replace(detail.getReName(), objectObjectHashMap);
            newName = idName.get(k) + "." + newName;
            if (FileUtil.containsInvalid(newName)) {
                newName = FileUtil.cleanInvalid(newName);
            }
            String filePath = detail.getPath() + "/" + newName + ".ass";
            if (FileUtil.exist(filePath)) {
                FileUtil.del(filePath);
            }
            FileUtil.writeLines(temp, filePath, "UTF-8");
            System.out.println("处理完成 : " + newName);
        });
    }

    public static Map<Integer, String> idMap(List<String> path) {
        Map<Integer, String> idMap = new HashMap<>();
        path.forEach(pathElement -> {
            IdMapByUrl(pathElement, idMap);
        });
        return idMap;
    }

    public static void IdMapByUrl(String url, Map<Integer, String> idMap) {
        Map<Integer, String> sortedMap = Collections.emptyMap();
        try {
            Map<Integer, String> res = new HashMap<>();
            Document doc = Jsoup.connect(url)
                    .header("pragma", "no-cache")
                    .header("Accept", "*/*")
                    .header("Host", "v.qq.com")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .timeout(10000)
                    .get();
            Elements allElements = doc.getElementsByClass("playlist-rect__container");
            Elements elements = allElements.get(0).getElementsByAttribute("data-vid");
            for (Element element : elements) {
                String key = element.attributes().get("data-vid");
                if (!element.getElementsByClass("playlist-item-rect__title").get(0).text().equals("彩蛋")) {
                    Integer index = Integer.valueOf(element.getElementsByClass("playlist-item-rect__title").get(0).text());
                    Elements ts = element.getElementsByClass("b-imgtag2 b-imgtag2--right-top b-imgtag2--small");
                    if (!ts.isEmpty()) {
                        String mark = ts.get(0).text();
                        if (!mark.equals("预")) {
                            res.put(index, key);
                        }
                    } else {
                        res.put(index, key);
                    }
                }
            }
            sortedMap = new TreeMap<>(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        idMap.putAll(sortedMap);
    }

    public static Map<Integer, String> idName(String path) {
        Map<Integer, String> idName = new HashMap<>();
        List<String> fileName = new ArrayList<>();
        for (File file : FileUtil.ls(path)) {
            if (FileUtil.extName(file).equals("mkv") || FileUtil.extName(file).equals("mp4")) {
                fileName.add(FileUtil.mainName(file));
            }
        }
        for (String input : fileName) {
            String pattern = "E(\\d+)";
            Matcher m = Pattern.compile(pattern).matcher(input);
            if (m.find()) {
                Integer episodeNumber = Integer.parseInt(m.group(1));
                idName.put(episodeNumber, input);
            }
        }
        return idName;
    }

    public static void ckAssFile(Detail detail) {
        if (detail.getDelMark() != null && !detail.getDelMark().isEmpty()) {
            for (File file : FileUtil.ls(detail.getPath())) {
                if (FileUtil.extName(file).equals("ass")) {
                    if (FileUtil.mainName(file).contains(detail.getDelMark())) {
                        FileUtil.del(file);
                    }
                }
            }
        }
    }

}
