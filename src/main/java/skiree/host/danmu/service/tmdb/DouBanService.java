package skiree.host.danmu.service.tmdb;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.stereotype.Service;
import skiree.host.danmu.model.tmdb.SeasonPath;
import skiree.host.danmu.model.tmdb.TvPath;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class DouBanService {

    public List<TvPath> matching(List<TvPath> tvPathList) {
        if (tvPathList == null || tvPathList.isEmpty()) return tvPathList;
        tvPathList.parallelStream().forEach(this::buildPlayInfo);
        tvPathList.forEach(x -> {
            List<SeasonPath> paths = x.getSeasonPaths().stream()
                    .filter(seasonPath -> !seasonPath.getDouBanLinkMap().isEmpty())
                    .collect(Collectors.toList());
            x.setSeasonPaths(paths);
        });
        return tvPathList;
    }

    /**
     * 补充豆瓣信息
     * @param tvPath 剧集信息对象
     */
    private void buildPlayInfo(TvPath tvPath) {
        if (tvPath.getSeasonPaths() == null || tvPath.getSeasonPaths().isEmpty()) return;
        for (SeasonPath seasonPath : tvPath.getSeasonPaths()) {
            Map<Integer, String> douBanLink = douBanLink(seasonPath, tvPath.getName(), seasonPath.getName());
            seasonPath.getDouBanLinkMap().putAll(douBanLink);
        }
    }

    /**
     * 获取豆瓣剧集列表等信息
     *
     * @param seasonPath 剧集季信息对象
     * @param name  剧集名称
     * @param tvNum 剧集季数
     * @return 集/播放地址 Map
     */
    private Map<Integer, String> douBanLink(SeasonPath seasonPath, String name, String tvNum) {
        JSONObject data = null;
        Map<Integer, String> douBanMap = new HashMap<>();
        if (!name.equalsIgnoreCase(tvNum)) {
            Matcher matcher = Pattern.compile("([0-9]+)").matcher(tvNum);
            if (matcher.find()) {
                tvNum = Convert.numberToChinese(Double.parseDouble(matcher.group(0)), false);
                data = doubanSelect(name, tvNum);
            }
        }
        if (data != null) {
            String url = data.getStr("url");
            // 提取豆瓣ID
            Matcher mDouBanId = Pattern.compile("https://movie.douban.com/subject/(\\d+)/").matcher(url);
            if (mDouBanId.find()) {
                seasonPath.setDoubanId(mDouBanId.group(1));
            }
            String result = HttpUtil.get(url, CharsetUtil.CHARSET_UTF_8);
            Matcher matcher = Pattern.compile("\\{play_link(.*?)}").matcher(result);
            while (matcher.find()) {
                JSONObject link = JSONUtil.parseObj(matcher.group());
                Integer ep = Integer.parseInt(link.getStr("ep"));
                String playLink = link.getStr("play_link").toLowerCase();
                playLink = URLDecoder.decode(playLink, CharsetUtil.CHARSET_UTF_8);
                playLink = playLink.replace("https://www.douban.com/link2/?url=", "");
                playLink = playLink.split(".html")[0] + ".html";
                douBanMap.put(ep, playLink);
            }
        }
        return douBanMap;
    }

    /**
     * 从豆瓣获取剧集信息
     *
     * @param name   剧集名称
     * @param tv_num 剧集季数
     * @return
     */
    private JSONObject doubanSelect(String name, String tv_num) {
        String result = HttpUtil.get("https://search.douban.com/movie/subject_search?search_text=" + name + "&cat=1002", CharsetUtil.CHARSET_UTF_8);
        Matcher matcher = Pattern.compile("window.__DATA__ = (.*?);", Pattern.DOTALL).matcher(result);
        if (matcher.find()) {
            JSONArray jsonArray = JSONUtil.parseObj(matcher.group(1)).getJSONArray("items");
            for (Object o : jsonArray) {
                JSONObject data = JSONUtil.parseObj(o);
                List<String> labelList = data.getJSONArray("labels").stream().map(label -> ((JSONObject) label).getStr("text")).filter(Objects::nonNull).collect(Collectors.toList());
                if (!labelList.isEmpty() && labelList.contains("可播放")) {
                    String d_name = data.getStr("title");
                    String d_tv_num = "";
                    Matcher matcherName = Pattern.compile("第(.*?)季").matcher(d_name);
                    if (matcherName.find()) {
                        d_tv_num = matcherName.group(1);
                    } else {
                        matcherName = Pattern.compile(name + "(\\d+)").matcher(d_name);
                        if (matcherName.find()) {
                            d_tv_num = matcherName.group(1);
                        } else {
                            List<String> romanNum = new ArrayList<>(Arrays.asList("", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"));
                            matcherName = Pattern.compile(name + "([ " + String.join("|", romanNum) + "]+)").matcher(d_name);
                            if (matcherName.find()) {
                                int index = romanNum.indexOf(matcherName.group(1));
                                if (index != -1) {
                                    d_tv_num = String.valueOf(index);
                                }
                            } else {
                                d_tv_num = "一";
                            }
                        }
                    }
                    try {
                        long d_tv_num_ps = Long.parseLong(d_tv_num);
                        d_tv_num = Convert.numberToChinese(d_tv_num_ps, false);
                    } catch (Exception ignore) {
                    }
                    if (d_name.contains(name.split(" ")[0]) && StrUtil.containsAny(tv_num, name, d_tv_num)) {
                        return data;
                    }
                }
            }
        }
        return null;
    }

}