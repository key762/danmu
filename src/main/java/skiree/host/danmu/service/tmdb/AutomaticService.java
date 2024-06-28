package skiree.host.danmu.service.tmdb;

import cn.hutool.core.io.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import skiree.host.danmu.dao.RoutineMapper;
import skiree.host.danmu.model.base.ResultData;
import skiree.host.danmu.model.base.Routine;
import skiree.host.danmu.model.tmdb.SeasonPath;
import skiree.host.danmu.model.tmdb.TvPath;
import skiree.host.danmu.service.base.BaseService;
import skiree.host.danmu.service.base.RoutineService;
import skiree.host.danmu.util.match.PlatUtil;

import java.io.File;
import java.util.*;

@Service
public class AutomaticService extends BaseService {

    @Autowired
    public RoutineService routineService;

    @Autowired
    public TMDBService tmdbService;

    @Autowired
    public DouBanService douBanService;

    @Autowired
    public RoutineMapper routineMapper;

    /**
     * 处理媒体路径
     * <p>
     * 1. path在进入此方法前应先检查其路径的有效性
     *
     * @param path 媒体根部路径
     */
    public void dealRootPath(String path, String resourceId) {
        List<TvPath> tvPathList = progenyFileInfo(new File(path));
        List<TvPath> tvValidList = tmdbService.matching(tvPathList);
        List<TvPath> tvDouBan = douBanService.matching(tvValidList);
        // 处理完成准备新增例程
        tvDouBan.forEach(tvPath -> {
            for (SeasonPath seasonPath : tvPath.getSeasonPaths()) {
                Routine routine = new Routine();
                routine.setName(tvPath.getName() + "S" + String.format("%02d", Integer.valueOf(seasonPath.getName())));
                routine.setRename(platName(seasonPath));
                routine.setTmdbId(tvPath.getTmdbId());
                routine.setDoubanId(seasonPath.getDoubanId());
                routine.setSeason(seasonPath.getName());
                routine.setResource(resourceId);
                routine.setPath(seasonPath.getPath());
                routine.setStart(nowTime());
                routine.setDelmark("");
                ResultData resultData = routineService.checkData(routine);
                if (resultData.status == 200) {
                    routineMapper.insert(routine);
                }
            }
        });
    }

    private String platName(SeasonPath seasonPath) {
        try {
            Collection<String> keySet = seasonPath.getDouBanLinkMap().values();
            String[] array = keySet.toArray(new String[0]);
            return PlatUtil.ckPlat(array[0], "弹幕");
        } catch (Exception ignore) {
        }
        return "DanMu弹幕";
    }

    /**
     * 获取根目录下的子目录和孙目录的信息
     *
     * @param file 媒体根部路径对象
     * @return List<TvPath> 处理后符合规范的对象集合
     */
    private List<TvPath> progenyFileInfo(File file) {
        List<TvPath> tvPathList = new ArrayList<>();
        File[] files = file.listFiles(File::isDirectory);
        if (files != null) {
            for (File tv : files) {
                List<SeasonPath> seasonPathList = progenySeasonInfo(tv);
                if (!seasonPathList.isEmpty()) {
                    String tvName = FileUtil.mainName(tv);
                    TvPath tvPath = new TvPath();
                    tvPath.setName(tvHandleName(tvName));
                    tvPath.setProtoName(tvName);
                    tvPath.setPath(tv.getAbsolutePath());
                    tvPath.setSeasonPaths(seasonPathList);
                    tvPathList.add(tvPath);
                }
            }
        }
        return tvPathList;
    }

    /**
     * 获取子目录下季信息
     *
     * @param tv 剧集路径对象
     * @return List<SeasonPath> 处理后符合规范的对象集合
     */
    private List<SeasonPath> progenySeasonInfo(File tv) {
        List<SeasonPath> seasonPathList = new ArrayList<>();
        File[] files = tv.listFiles(File::isDirectory);
        if (files != null) {
            for (File season : files) {
                String seasonName = FileUtil.mainName(season);
                String name = seasonHandleName(seasonName);
                if (name != null) {
                    SeasonPath sp = new SeasonPath();
                    sp.setName(name);
                    sp.setProtoName(seasonName);
                    sp.setPath(season.getAbsolutePath());
                    seasonPathList.add(sp);
                }
            }
        }
        return seasonPathList;
    }

    /**
     * 处理季名称
     * <p>
     * 1. 对季标识进行处理,只保留数字
     *
     * @param seasonName 季名称
     * @return String
     */
    private String seasonHandleName(String seasonName) {
        if (seasonName.startsWith("Season ")) {
            return seasonName.replace("Season ", "");
        }
        return null;
    }

    /**
     * 处理剧集名称
     * <p>
     * 1. 对剧集名进行处理,如去掉年份标识等
     *
     * @param tvName 剧集名称
     * @return String
     */
    private String tvHandleName(String tvName) {
        if (!tvName.contains(" ")) {
            return tvName;
        }
        String[] names = tvName.split(" ");
        String yearName = names[names.length - 1];
        if (yearName.startsWith("(") && yearName.endsWith(")")) {
            String yearMark = yearName.replace("(", "").replace(")", "");
            try {
                Long.parseLong(yearMark);
                return tvName.replace(" " + yearName, "");
            } catch (Exception ignore) {
            }
        }
        return tvName;
    }

}