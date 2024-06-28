package skiree.host.danmu.model.tmdb;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class SeasonPath {
    private String name;
    private String protoName;
    private String path;
    private String doubanId;
    private Map<Integer, Episodes> episodesMap = new HashMap<>();
    private Map<Integer, String> douBanLinkMap = new HashMap<>();
}
