package skiree.host.danmu.model.tmdb;

import lombok.Data;

import java.util.List;


@Data
public class TvPath {
    private String name;
    private String protoName;
    private String path;
    private String tmdbId;
    private List<SeasonPath> seasonPaths;
}
