package skiree.host.danmu.service.core.iqiyi;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import skiree.host.danmu.service.core.iqiyi.modle.Node;
import skiree.host.danmu.service.core.vqq.vQqAssEngine;
import skiree.host.danmu.model.ass.ASS;
import skiree.host.danmu.model.task.Detail;
import skiree.host.danmu.model.engine.DanMu;
import skiree.host.danmu.util.substitutor.CustomSubstitutor;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class iQiYiEngine {

    public static void doProcess(Detail detail) {
        ckAssFile(detail);
        Map<Integer, String> idName = idName(detail.getPath());
        if (idName.isEmpty()) {
            return;
        }
        Map<Integer, Node> idMap = idMap(detail);
        Map<Integer, Node> jobMap = idMap.entrySet().stream()
                .filter(entry -> idName.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        System.out.println("扫描到 " + jobMap.size() + " 个视频,即将开始抓取弹幕。");
        jobMap.forEach((k, v) -> {
            Map<Long, List<DanMu>> res = iQiYiAssEngine.getDanMu(v);
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

    public static Map<Integer, Node> idMap(Detail detail) {
        if (CollectionUtils.isEmpty(detail.getSource())) {
            throw new RuntimeException("必须配置source属性");
        }
        String videoIds = "";
        while (StringUtils.isBlank(videoIds)) {
            try {
                videoIds = HttpUtil.get(detail.getSource().get(0), CharsetUtil.CHARSET_UTF_8);
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
        return idUrl;
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
