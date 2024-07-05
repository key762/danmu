package skiree.host.danmu.service.tmdb;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.stereotype.Service;
import skiree.host.danmu.model.tmdb.Episodes;
import skiree.host.danmu.model.tmdb.SeasonPath;
import skiree.host.danmu.model.tmdb.TvPath;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class TMDBService {

    final static String API_KEY = "a89b8e8ac71914ae998fbcf80bdb1232";

    private static final int THREAD_POOL_SIZE = 16;

    private static final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public void matching(List<TvPath> tvPathList) {
        if (tvPathList == null || tvPathList.isEmpty()) return;
        tvPathList.parallelStream().forEach(this::buildTvId);
        for (TvPath tvPath : tvPathList) {
            executorService.execute(() -> {
                buildTvId(tvPath);
            });
        }
        // 关闭线程池
        executorService.shutdown();
        tvPathList = tvPathList.stream()
                .filter(tvPath -> StrUtil.isNotEmpty(tvPath.getTmdbId()))
                .collect(Collectors.toList());
//        tvPathList.parallelStream().forEach(this::buildSeason);
//        tvPathList.forEach(x -> {
//            List<SeasonPath> paths = x.getSeasonPaths().stream()
//                    .filter(seasonPath -> !seasonPath.getEpisodesMap().isEmpty())
//                    .collect(Collectors.toList());
//            x.setSeasonPaths(paths);
//        });
//        tvPathList = tvPathList.stream()
//                .filter(tvPath -> !tvPath.getSeasonPaths().isEmpty())
//                .collect(Collectors.toList());
//        return tvPathList;
    }

    private void buildSeason(TvPath tvPath) {
        if (tvPath.getSeasonPaths() == null || tvPath.getSeasonPaths().isEmpty()) return;
        for (SeasonPath seasonPath : tvPath.getSeasonPaths()) {
            String url = "https://api.themoviedb.org/3/tv/" + tvPath.getTmdbId() + "/season/" + seasonPath.getName() + "?language=zh&append_to_response=credits&api_key=" + API_KEY;
            String result = HttpUtil.get(url, CharsetUtil.CHARSET_UTF_8);
            JSONArray jsonArray = JSONUtil.parseObj(result).getJSONArray("episodes");
            for (Object object : jsonArray) {
                JSONObject data = JSONUtil.parseObj(object);
                Integer number = data.getInt("episode_number");
                String name = data.getStr("name");
                String season = data.getStr("season_number");
                Episodes episodes = new Episodes(number, name, season);
                seasonPath.getEpisodesMap().put(number, episodes);
            }
        }
    }


    /**
     * 根据tmid和季对象获取集信息
     *
     * @param seasonPath 季对象
     * @param tmdbId     tmdbid
     * @return 集信息
     */
    public Map<Integer, String> buildSeason(SeasonPath seasonPath, String tmdbId) {
        Map<Integer, String> seasonMap = new HashMap<>();
        String tvName = getTvName(tmdbId);
        String url = "https://api.themoviedb.org/3/tv/" + tmdbId + "/season/" + seasonPath.getName() + "?language=zh&append_to_response=credits&api_key=" + API_KEY;
        String result = HttpUtil.get(url, CharsetUtil.CHARSET_UTF_8);
        JSONArray jsonArray = JSONUtil.parseObj(result).getJSONArray("episodes");
        for (Object object : jsonArray) {
            JSONObject data = JSONUtil.parseObj(object);
            Integer number = data.getInt("episode_number");
            String name = data.getStr("name");
            String season = data.getStr("season_number");
            Episodes episodes = new Episodes(number, name, season);
            seasonPath.getEpisodesMap().put(number, episodes);
            String epName = tvName + " - S" +
                    String.format("%02d", Integer.valueOf(season))
                    + "E" +
                    String.format("%02d", number)
                    + " - " + name;
            seasonMap.put(number, epName);
        }
        return seasonMap;
    }

    /**
     * 根据tvID获取剧集名
     *
     * @param tmdbId id
     * @return 剧集名
     */
    private String getTvName(String tmdbId) {
        String url = "https://api.themoviedb.org/3/tv/" + tmdbId + "?language=zh&append_to_response=translations&api_key=" + API_KEY;
        String result = HttpUtil.get(url, CharsetUtil.CHARSET_UTF_8);
        return JSONUtil.parseObj(result).getStr("name");
    }

    /**
     * 在TMDB中搜索对应剧集并设置id
     *
     * @param tvPath 剧集对象
     */
    public void buildTvId(TvPath tvPath) {
        String url = "https://api.themoviedb.org/3/search/tv?query=" + tvPath.getName() + "&page=1&language=zh&include_adult=false&api_key=" + API_KEY;
        String result = HttpUtil.get(url, CharsetUtil.CHARSET_UTF_8);
        JSONArray jsonArray = JSONUtil.parseObj(result).getJSONArray("results");
        for (Object object : jsonArray) {
            JSONObject data = JSONUtil.parseObj(object);
            if (data.getStr("name").equals(tvPath.getName())) {
                tvPath.setTmdbId(data.getStr("id"));
                return;
            }
        }
    }

}