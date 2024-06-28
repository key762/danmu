package skiree.host.danmu.model.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import skiree.host.danmu.model.tmdb.SeasonPath;
import skiree.host.danmu.model.tmdb.TvPath;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDo {
    public Execute execute;
    public Routine routine;
    public TvPath tvPath;
    public SeasonPath seasonPath;
}
