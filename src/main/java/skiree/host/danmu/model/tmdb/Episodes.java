package skiree.host.danmu.model.tmdb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Episodes {
    private Integer number;
    private String name;
    private String season;
}